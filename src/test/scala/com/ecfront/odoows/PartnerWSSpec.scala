package com.ecfront.odoows

import org.scalatest._


class PartnerWSSpec extends FunSuite {

  //Common
  val ws = OdooWS("http://127.0.0.1:8069", "odoo")
  assert(ws.common.version.get("server_version").get == "8.0")
  val uid = ws.common.login("admin", "123456")

  test("Partner WS test") {
    val result=ws.record.find(
      List(
        //List("customer", "=", false),
        List("street", "=", "海天城"),
      "|",
        List("city", "=", "杭州"),
        List("name", "=", "Your Company")
      ), "res.partner", uid)
    print(result)
  }

}







