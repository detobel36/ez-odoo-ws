EZ Odoo Web Service
===
### Odoo 8.x WS的Scala实现

 =======================================================

#功能

1. 实现Odoo WS 的接口（除流程外）
1. 支持Scala对象映射
1. 使用简单（Odoo WS的设计太笨重了……）

#使用

## Step1 引用包（以Maven为例）

    <dependency>
        <groupId>com.ecfront</groupId>
        <artifactId>ez-odoo-ws</artifactId>
        <version>0.3.7</version>
    </dependency>

## Step2 定义模型（如使用原始的Map可忽略此步骤）

所有模型都继承`com.ecfront.odoows.OdooModel`，此父类包含了 `id` `display_name` `create_uid` `create_date` `write_uid` `write_date` ，这几个字段无需手工赋值

所有模型都使用`@Odoo(name = "模型名称", label = "模型显示名称")`注解，自定义模型（新模型）都要以`x_` 开头

*可以是新的模型也可以是已存在的，如果是已存在的模型请确保Odoo中所有字段（compute类型，store为false的可加可不加）都被映射*

###映射规则如下：

支持的类型 | Odoo对应的类型 | 示例
-------------------------- | -------------------------- | ----------------------------------------
String/Char | char |  @BeanProperty var x_name: String
String+@OText | text |   @BeanProperty @OText var x_desc: String
Any+@OSelection | selection | @BeanProperty @OSelection var state: String
Int/Integer/Long/Short | integer | @BeanProperty var x_age: Int
Boolean/bool | boolean | @BeanProperty var x_enable: Boolean
Double（不支持Float） | float | @BeanProperty var x_fh: Double
Date | datetime | @BeanProperty var create_date: util.Date
Date+@ODate | date | @BeanProperty @ODate var start_date: Date
Int+@OManyToOne | many2one |  @BeanProperty @OManyToOne var responsible_id: Int
List[Int]+@OOneToMany | one2many | @BeanProperty @OOneToMany var session_ids:List[Int]
List[Int]+@OManyToMany | many2many | @BeanProperty @OManyToMany var attendee_ids: List[Int]
Any+@OTransient | (不会被映射) | @BeanProperty @OTransient var tmp: String

* 所有字段都要加上@BeanProperty，否则无法序列化；
* 自定义字段（新模型的所有映射字段）都要以`x_` 开头；
* OneToMany 字段只用于获取值不能用于赋值，即通过此字段可以获取到关联的ids，但保存时此字段的关系不会被持久化，因为它不是关系维护端，需要保存此关系请到另一端（ManyToOne）处理。

### 示例（此模型对应Odoo Tutorials中的openacademy.session）

    @Odoo(name = "openacademy.session", label = "选课")
    case class Session() extends OdooModel {
      @BeanProperty var name: String = _
      @BeanProperty
      @ODate var start_date: Date = _
      @BeanProperty
      @ODate var end_date: Date = _
      @BeanProperty var duration: Double = _
      @BeanProperty var seats: Int = _
      @BeanProperty var active: Boolean = _
      @BeanProperty var color: Int = _
      @BeanProperty
      @OManyToOne var instructor_id: Int = _
      @BeanProperty
      @OManyToOne var course_id: Int = _
      @BeanProperty
      @OManyToMany var attendee_ids: List[Int] = _
      @BeanProperty var taken_seats: Double = _
      @BeanProperty var hours: Double = _
      @BeanProperty var attendees_count: Int = _
      @BeanProperty
      @OSelection var state: String = _
    }

## Step3 连接&登录

    //地址+要连接的数据库 -> 连接实例
    val ws = OdooWS("http://127.0.0.1:8069", "odoo")
    //用户名+密码 -> 用户Id（当token用）
    val uid = ws.common.login("admin", "123456")

## Step4 各类操作

操作有三大类：
1. 针对模型，命名空间为 `ws.model`
1. 针对记录，命名空间为 `ws.record` ，此操作的每个方法一般都提供使用Scala模型映射和直接使用Map类型的两种实现
1. 通用，命名空间为 `ws.common`

    //创建模型
    ws.model.create(classOf[模型], uid)

    //删除所有记录
    ws.record.deleteAll(classOf[模型], uid) 或 ws.record.deleteAll(模型名称, uid)
     //删除单条记录
    ws.record.delete(id,classOf[模型], uid) 或 ws.record.delete(id,模型名称, uid)

    //添加记录
    ws.record.create(模型实例, classOf[模型], uid) 或 ws.record.create(Map实例, 模型名称, uid)

    //更新记录
    ws.record.update(模型实例, classOf[模型], uid) 或 ws.record.update(id,Map实例, 模型名称, uid)

    //获取单条记录
    ws.record.get(id, classOf[模型], uid) 或 ws.record.get(id,模型名称, uid)

    //获取记录数
    ws.record.count(条件, classOf[模型], uid) 或 ws.record.count(条件,模型名称, uid)

    //分页
    ws.record.page(第几页（从1开始）,每页几条,条件, classOf[模型], uid) 或 ws.record.page(第几页（从1开始）,每页几条,条件,模型名称, uid)

    详见 com.ecfront.odoows.OdooWSSpec 及 com.ecfront.odoows.OpenacademyWSSpec

# Check out sources
`git clone https://github.com/gudaoxuri/ez-odoo-ws.git`

# License

Under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0


