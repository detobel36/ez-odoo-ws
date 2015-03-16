package com.ecfront.odoows

import java.util

import scala.beans.BeanProperty

case class LoginInfo(uid: Int, password: String)

case class PageModel[M](
                         @BeanProperty var pageNumber: Long,
                         @BeanProperty var pageSize: Long,
                         @BeanProperty var pageTotal: Long,
                         @BeanProperty var recordTotal: Long,
                         @BeanProperty var results: List[M]
                         )

/**
 * 所有Odoo模型的父类
 */
abstract class OdooModel {
  @BeanProperty var id: Int = _
  @BeanProperty var display_name: String = _
  //array[id#int,name#string]
  @BeanProperty var create_uid: Array[Any] = _
  @BeanProperty var create_date: util.Date = _
  //array[id#int,name#string]
  @BeanProperty var write_uid: Array[Any] = _
  @BeanProperty var write_date: util.Date = _
}
