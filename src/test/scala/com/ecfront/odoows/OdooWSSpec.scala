package com.ecfront.odoows

import java.text.SimpleDateFormat
import java.util.Date

import org.scalatest._

import scala.beans.BeanProperty


class OdooWSSpec extends FunSuite {

  test("Odoo WS test") {

    //Common
    val ws = OdooWS("http://127.0.0.1:8069", "odoo")
    //assert(ws.common.version.get("server_version").get == "8.0")
    val uid = ws.common.login("admin", "123456")

    //Create model
    // ws.model.create(classOf[x_student], uid)

    //Record CRUD
    ws.record.deleteAll(classOf[x_student], uid)
    val dt = new Date()
    val student1 = x_student()
    student1.x_name = "张三"
    student1.x_age = 20
    student1.x_someTime = dt
    student1.x_enable = true
    student1.x_fh = 1.75
    val id = ws.record.create(student1, classOf[x_student], uid)
    val stu1 = ws.record.get(id, classOf[x_student], uid)
    assert(stu1.x_name == "张三" && stu1.x_age == 20 && new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(stu1.x_someTime) == new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt) && stu1.x_enable && stu1.x_fh == 1.75)
    stu1.x_name = "张三new"
    ws.record.update(stu1, classOf[x_student], uid)
    val stu2 = ws.record.get(id, classOf[x_student], uid)
    assert(stu2.x_name == "张三new" && stu2.x_age == 20 && new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(stu2.x_someTime) == new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt) && stu2.x_enable && stu2.x_fh == 1.75)

    val student2 = x_student()
    student2.x_name = "李二"
    ws.record.create(student2, classOf[x_student], uid)
    val student3 = x_student()
    student3.x_name = "李三"
    ws.record.create(student3, classOf[x_student], uid)
    val student4 = x_student()
    student4.x_name = "李四"
    ws.record.create(student4, classOf[x_student], uid)
    val student5 = x_student()
    student5.x_name = "李五"
    ws.record.create(student5, classOf[x_student], uid)
    val student6 = x_student()
    student6.x_name = "李六"
    val stu6Id = ws.record.create(student6, classOf[x_student], uid)

    assert(ws.record.count(List(), classOf[x_student], uid) == 6)
    ws.record.delete(stu6Id, classOf[x_student], uid)
    assert(ws.record.count(List(), classOf[x_student], uid) == 5)

    val page = ws.record.page(1, 2, List(), classOf[x_student], uid)
    assert(page.pageSize == 2 && page.pageNumber == 1 && page.recordTotal == 5 && page.pageTotal == 3)
    assert(page.results(0).x_name == "张三new")

  }
}

case class x_student() extends OdooModel {
  @BeanProperty var x_name: String = _
  @BeanProperty var x_someTime: java.util.Date = _
  @BeanProperty var x_age: Int = _
  @BeanProperty var x_enable: Boolean = _
  @BeanProperty var x_fh: Double = _
}





