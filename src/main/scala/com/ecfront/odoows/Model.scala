package com.ecfront.odoows

import java.util

import scala.annotation.StaticAnnotation
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
  @BeanProperty
  @OManyToOne var create_uid: Int = _
  @BeanProperty var create_date: util.Date = _
  @BeanProperty
  @OManyToOne var write_uid: Int = _
  @BeanProperty var write_date: util.Date = _
}

case class Odoo(name: String, label: String) extends StaticAnnotation

@scala.annotation.meta.field
case class OManyToOne() extends StaticAnnotation

@scala.annotation.meta.field
case class OOneToMany() extends StaticAnnotation

@scala.annotation.meta.field
case class OManyToMany() extends StaticAnnotation

@scala.annotation.meta.field
case class OText() extends StaticAnnotation

@scala.annotation.meta.field
case class ODate() extends StaticAnnotation

@scala.annotation.meta.field
case class OSelection() extends StaticAnnotation

@scala.annotation.meta.field
case class OTransient() extends StaticAnnotation

