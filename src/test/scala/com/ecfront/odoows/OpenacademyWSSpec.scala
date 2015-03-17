package com.ecfront.odoows

import java.util.Date

import org.scalatest._

import scala.beans.BeanProperty

class OpenacademyWSSpec extends FunSuite {

  //Common
  val ws = OdooWS("http://127.0.0.1:8069", "odoo")
  //assert(ws.common.version.get("server_version").get == "8.0")
  val uid = ws.common.login("admin", "123456")

  test("Openacademy WS test") {

  /*  val course=Course()
        course.name="计算机65"
        course.description="计算机课程。。。"
        course.responsible_id=1
        val id=ws.record.create(course,classOf[Course],uid)
        val cou=ws.record.get(id,classOf[Course],uid)*/

    //val courses=ws.record.page(1,5,List(),classOf[Course],uid)

    /*ws.record.deleteAll(classOf[Session],uid)
    val session = Session()
    session.name = "选课332"
    session.course_id = 1
    session.active = true
    session.attendee_ids = List(1,6)
    val sId = ws.record.create(session, classOf[Session], uid)
    val sess=ws.record.get(sId,classOf[Session],uid)
    sess.attendee_ids=List(7,9)
    ws.record.update(sess, classOf[Session], uid)
    val sessions = ws.record.page(1, 6, List(), classOf[Session], uid)
    assert(sessions.getRecordTotal==1)
    assert(sessions.results(0).attendee_ids==List(7,9))*/
  }

}

@Odoo(name = "openacademy.course", label = "课程")
case class Course() extends OdooModel {
  @BeanProperty var name: String = _
  @BeanProperty
  @OText var description: String = _
  @BeanProperty
  @OManyToOne var responsible_id: Int = _
  @BeanProperty var session_ids:List[Int] = _
}

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









