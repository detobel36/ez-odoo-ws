package com.ecfront.odoows

import java.text.SimpleDateFormat
import java.util.Date

import org.scalatest._

import scala.beans.BeanProperty


class OdooWSSpec extends FunSuite {

  //Common
  val ws = OdooWS("http://127.0.0.1:8069", "odoo")
  //assert(ws.common.version.get("server_version").get == "8.0")
  val uid = ws.common.login("admin", "123456")

  test("Odoo WS test") {

    //Create model
    ws.model.create(classOf[Student], uid)

    //Record CRUD
    ws.record.deleteAll(classOf[Student], uid)
    val dt = new Date()
    val student1 = Student()
    student1.x_name = "张三"
    student1.x_age = 20
    student1.x_someTime = dt
    student1.x_enable = true
    student1.x_fh = 1.75
    student1.x_desc = "3月13日，Spark 1.3.0版本与我们如约而至。这是Spark 1.X发布计划中的第四次发布，距离1.2版本发布约三个月时间。据Spark官方网站报道，此次发布是有史以来最大的一次发布，共有174位开发者为代码库做出贡献，提交次数超过1000次。此次版本发布的最大亮点是新引入的DataFrame API。对于结构型的DataSet，它提供了更方便更强大的操作运算。事实上，我们可以简单地将DataFrame看做是对RDD的一个封装或者增强，使得Spark能够更好地应对诸如数据表、JSON数据等结构型数据样式（Schema），而不是传统意义上多数语言提供的集合数据结构。在一个数据分析平台中增加对DataFrame的支持，其实也是题中应有之义。诸如R语言、Python的数据分析包pandas都支持对Data Frame数据结构的支持。事实上，Spark DataFrame的设计灵感正是基于R与Pandas。Databricks的博客在今年2月就已经介绍了Spark新的DataFrame API。文中提到了新的DataFrames API的使用方法，支持的数据格式与数据源，对机器学习的支持以及性能测评等。文中还提到与性能相关的实现机制："
    val id = ws.record.create(student1, classOf[Student], uid)
    val stu1 = ws.record.get(id, classOf[Student], uid)
    assert(stu1.x_name == "张三" && stu1.x_age == 20 && new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(stu1.x_someTime) == new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt) && stu1.x_enable && stu1.x_fh == 1.75)
    stu1.x_name = "张三new"
    ws.record.update(stu1, classOf[Student], uid)
    val stu2 = ws.record.get(id, classOf[Student], uid)
    assert(stu2.x_name == "张三new" && stu2.x_age == 20 && new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(stu2.x_someTime) == new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt) && stu2.x_enable && stu2.x_fh == 1.75)

    val student2 = Student()
    student2.x_name = "李二"
    ws.record.create(student2, classOf[Student], uid)
    val student3 = Student()
    student3.x_name = "李三"
    ws.record.create(student3, classOf[Student], uid)
    val student4 = Student()
    student4.x_name = "李四"
    ws.record.create(student4, classOf[Student], uid)
    val student5 = Student()
    student5.x_name = "李五"
    ws.record.create(student5, classOf[Student], uid)
    val student6 = Student()
    student6.x_name = "李六"
    val stu6Id = ws.record.create(student6, classOf[Student], uid)

    assert(ws.record.count(List(), classOf[Student], uid) == 6)
    ws.record.delete(stu6Id, classOf[Student], uid)
    assert(ws.record.count(List(), classOf[Student], uid) == 5)

    val page = ws.record.page(1, 2, List(), classOf[Student], uid)
    assert(page.pageSize == 2 && page.pageNumber == 1 && page.recordTotal == 5 && page.pageTotal == 3)
    assert(page.results(0).x_name == "张三new")

  }

}

@Odoo(name = "x_student_model", label = "学生记录")
case class Student() extends OdooModel {
  @BeanProperty var x_name: String = _
  @BeanProperty var x_someTime: java.util.Date = _
  @BeanProperty var x_age: Int = _
  @BeanProperty var x_enable: Boolean = _
  @BeanProperty var x_fh: Double = _
  @BeanProperty
  @OText var x_desc: String = _
}





