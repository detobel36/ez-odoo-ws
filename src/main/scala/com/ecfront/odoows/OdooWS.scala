package com.ecfront.odoows

import java.text.SimpleDateFormat
import java.util

import com.ecfront.common.{BeanHelper, JsonHelper}
import com.ecfront.odoows.helper.XmlRPCHelper
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.JavaConversions._

/**
 * Odoo WS
 * @param url ws url
 * @param db 连接的db
 */
case class OdooWS(url: String, db: String) extends LazyLogging {

  private val rpc = new XmlRPCHelper(url)
  //保存登录信息
  private val session = collection.mutable.Map[Int, LoginInfo]()
  private val emptyMap = new java.util.HashMap()


  object common {

    def version: Map[String, Any] = rpc.request("/xmlrpc/2/common", "version", List()).asInstanceOf[java.util.HashMap[String, Object]].toMap

    def login(userName: String, password: String): Int = {
      val uid: Int = rpc.request("/xmlrpc/2/common", "authenticate", List(db, userName, password, emptyMap)).asInstanceOf[java.lang.Integer].toInt
      session += uid -> LoginInfo(uid, password)
      uid
    }

  }

  object model {

    /**
     * 创建模型，Note:由于odoo没有提供删除物理表的接口故重建模型时需要手工删除已存在的物理表
     * @param clazz
     * @param uid
     * @tparam M
     */
    def create[M <: OdooModel](clazz: Class[M], uid: Int): Unit = {
      val modelName = clazz.getSimpleName
      val existIds = record.findIds(List(List("model", "=", modelName)), "ir.model", uid)
      if (existIds != null) {
        record.delete(existIds(0), "ir.model", uid)
      }
      val id = rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, "ir.model", "create",
          XmlRPCHelper.toJavaList(List(new util.HashMap[String, Any]() {
            {
              put("name", modelName.substring(2))
              put("model", modelName)
              put("state", "manual")
            }
          })), emptyMap)).asInstanceOf[java.lang.Integer].toInt
      BeanHelper.findFields(clazz, OdooWS.filterNames).map {
        item =>
          new util.HashMap[String, Any]() {
            {
              put("model_id", id)
              put("name", item._1)
              //TODO text  rel ... support
              put("ttype", item._2.toLowerCase match {
                case t if t == "string" || t == "char" => "char"
                case t if t == "int" || t == "integer" || t == "long" || t == "short" => "integer"
                case t if t == "boolean" || t == "bool" => "boolean"
                case t if t == "float" || t == "double" => "float"
                case t if t.endsWith("date") => "datetime"
                case _ => throw new Exception("Not support type:" + item._2)
              })
              put("state", "manual")
              put("required", false)
            }
          }
      }.foreach {
        item =>
          //没有批量创建接口，只得一条条提交
          rpc.request("/xmlrpc/2/object", "execute_kw", List(db, uid, session(uid).password, "ir.model.fields",
            "create", XmlRPCHelper.toJavaList(List(item)), emptyMap))
      }
    }

  }

  object record {

    def create[M <: OdooModel](record: M, clazz: Class[M], uid: Int): Int = doCreate(OdooWS.getValues(record), clazz.getSimpleName, uid)

    def create(record: Map[String, Any], modeName: String, uid: Int): Int = doCreate(XmlRPCHelper.toJavaMap(record), modeName, uid)

    private def doCreate(record: util.Map[String, Any], modeName: String, uid: Int): Int =
      rpc.request("/xmlrpc/2/object", "execute_kw", List(db, uid, session(uid).password, modeName, "create",
        XmlRPCHelper.toJavaList(List(record))
        , emptyMap)).asInstanceOf[java.lang.Integer].toInt

    def update[M <: OdooModel](record: M, clazz: Class[M], uid: Int): Boolean = doUpdate(record.id, OdooWS.getValues(record), clazz.getSimpleName, uid)

    def update(id: Int, record: Map[String, Any], modeName: String, uid: Int): Boolean = doUpdate(id, XmlRPCHelper.toJavaMap(record), modeName, uid)

    private def doUpdate(id: Int, record: util.Map[String, Any], modeName: String, uid: Int): Boolean =
      rpc.request("/xmlrpc/2/object", "execute_kw", List(db, uid, session(uid).password, modeName, "write",
        XmlRPCHelper.toJavaList(List(new util.ArrayList[Integer]() {
          {
            add(id)
          }
        }, record)), emptyMap)).asInstanceOf[java.lang.Boolean]


    def delete[M <: OdooModel](id: Int, clazz: Class[M], uid: Int): Unit = doDelete(id, clazz.getSimpleName, uid)

    def deleteAll[M <: OdooModel](clazz: Class[M], uid: Int): Unit = doDelete(0, clazz.getSimpleName, uid)

    def delete(id: Int, modeName: String, uid: Int): Unit = doDelete(id, modeName, uid)

    def deleteAll(modeName: String, uid: Int): Unit = doDelete(0, modeName, uid)

    private def doDelete(id: Int, modeName: String, uid: Int): Unit = {
      val delIds = new util.ArrayList[Integer]()
      if (id == 0) {
        //delete all
        delIds.addAll(findIds(List(), modeName, uid).map(_.asInstanceOf[Integer]))
      } else {
        delIds.add(id)
      }
      rpc.request("/xmlrpc/2/object", "execute_kw", List(db, uid, session(uid).password, modeName, "unlink",
        XmlRPCHelper.toJavaList(List(delIds)), emptyMap))
    }

    def count[M <: OdooModel](conditions: List[List[Any]], clazz: Class[M], uid: Int): Int = doCount(conditions, clazz.getSimpleName, uid)

    def count(conditions: List[List[Any]], modeName: String, uid: Int): Int = doCount(conditions, modeName, uid)

    private def doCount(conditions: List[List[Any]], modeName: String, uid: Int): Int =
      rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modeName, "search_count",
          OdooWS.packageConditions(conditions))).asInstanceOf[Integer].toInt

    def get[M <: OdooModel](id: Int, clazz: Class[M], uid: Int): M = OdooWS.toObject(doGet(id, clazz.getSimpleName, uid), clazz)

    def get(id: Int, modeName: String, uid: Int): Map[String, Any] = doGet(id, modeName, uid).toMap

    private def doGet(id: Int, modeName: String, uid: Int): util.Map[String, Any] = {
      rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modeName, "read",
          XmlRPCHelper.toJavaList(List(id)), emptyMap)).asInstanceOf[util.HashMap[String, Any]]
    }

    def find[M <: OdooModel](conditions: List[List[Any]], clazz: Class[M], uid: Int): List[M] = doFind(conditions, clazz.getSimpleName, uid).map(OdooWS.toObject(_, clazz))

    def find(conditions: List[List[Any]], modeName: String, uid: Int): List[Map[String, Any]] = doFind(conditions, modeName, uid).map(_.asInstanceOf[util.HashMap[String, Any]].toMap)

    private def doFind(conditions: List[List[Any]], modeName: String, uid: Int): List[Any] = {
      rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modeName, "search_read",
          OdooWS.packageConditions(conditions), emptyMap)).asInstanceOf[Array[Any]].toList
    }

    def page[M <: OdooModel](pageNumber: Int, pageSize: Int, conditions: List[List[Any]], clazz: Class[M], uid: Int): PageModel[M] =
      doPage(pageNumber, pageSize, conditions, clazz.getSimpleName, clazz, uid)

    def page(pageNumber: Int, pageSize: Int, conditions: List[List[Any]], modeName: String, uid: Int): PageModel[Map[String, Any]] =
      doPage(pageNumber, pageSize, conditions, modeName, classOf[Map[String, Any]], uid)

    private def doPage[M](pageNumber: Int, pageSize: Int, conditions: List[List[Any]], modeName: String, clazz: Class[M], uid: Int): PageModel[M] = {
      val recordTotal = count(conditions, modeName, uid)
      val result = rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modeName, "search_read",
          OdooWS.packageConditions(conditions), new util.HashMap[String, Any]() {
            {
              put("offset", pageSize * (pageNumber - 1))
              put("limit", pageSize)
            }
          })).asInstanceOf[Array[Any]].toList.map(OdooWS.toObject(_, clazz))
      PageModel[M](pageNumber, pageSize, (recordTotal + pageSize - 1) / pageSize, recordTotal, result)
    }

    def findIds[M <: OdooModel](conditions: List[List[Any]], clazz: Class[M], uid: Int): List[Int] = doFindIds(conditions, clazz.getSimpleName, uid)

    def findIds(conditions: List[List[Any]], modelName: String, uid: Int): List[Int] = doFindIds(conditions, modelName, uid)

    private def doFindIds(conditions: List[List[Any]], modelName: String, uid: Int): List[Int] =
      rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modelName, "search",
          OdooWS.packageConditions(conditions), emptyMap)).asInstanceOf[Array[Any]].toList.map(_.asInstanceOf[Int])

    def pageIds[M <: OdooModel](pageNumber: Int, pageSize: Int, conditions: List[List[Any]], clazz: Class[M], uid: Int): List[Int] =
      doPageIds(pageNumber, pageSize, conditions, clazz.getSimpleName, uid)

    def pageIds(pageNumber: Int, pageSize: Int, conditions: List[List[Any]], modelName: String, uid: Int): List[Int] =
      doPageIds(pageNumber, pageSize, conditions, modelName, uid)

    private def doPageIds(pageNumber: Int, pageSize: Int, conditions: List[List[Any]], modelName: String, uid: Int): List[Int] =
      rpc.request("/xmlrpc/2/object", "execute_kw",
        List(db, uid, session(uid).password, modelName, "search",
          OdooWS.packageConditions(conditions), new util.HashMap[String, Any]() {
            {
              put("offset", pageSize * (pageNumber - 1))
              put("limit", pageSize)
            }
          })).asInstanceOf[Array[Any]].toList.map(_.asInstanceOf[Int])
  }

}


object OdooWS {

  private val filterNames = Seq("id", "display_name", "create_uid", "create_date", "write_uid", "write_date")
  private val clazzFieldCache = collection.mutable.Map[String, Map[String, String]]()
  //Odoo的Datetime以String形式表达，需要按yyyy-MM-dd hh:mm:ss格式转换
  private val dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

  private def toObject[E](obj: Any, clazz: Class[E]): E = {
    obj match {
      case o: util.HashMap[String, Any] if clazz == classOf[Map[String, Any]] =>
        o.toMap.asInstanceOf[E]
      case o: util.HashMap[String, Any] if clazz != classOf[Map[String, Any]] =>
        var fields: Map[String, String] = null
        if (clazzFieldCache.contains(clazz.getName)) {
          fields = clazzFieldCache(clazz.getName)
        } else {
          fields = BeanHelper.findFields(clazz)
          clazzFieldCache += clazz.getName -> fields
        }
        val value = o.filter(_._1 != "__last_update").map {
          item =>
            item._1 match {
              case key if fields(key) == "java.util.Date" =>
                item._2 match {
                  case v: String =>
                    item._1 -> dt.parse(v)
                  case _ =>
                    //当表中datetime为空返回的竟然是false，无语……，这里需要转成null
                    item._1 -> null
                }
              case _ => item._1 -> item._2
            }
        }
        JsonHelper.toObject(value, clazz)
      case _ => JsonHelper.toObject(obj, clazz)
    }
  }

  private def getValues[M <: OdooModel](record: M): util.Map[String, Any] = {
    XmlRPCHelper.toJavaMap(BeanHelper.findValues(record, OdooWS.filterNames).filter(_._2 != null).map {
      item =>
        item._1 -> (item._2 match {
          case t: util.Date =>
            //date -> string 数据表格式为datetime
            dt.format(t)
          case _ => item._2
        })
    })
  }

  private def packageConditions(conditions: List[List[Any]]) = {
    val cond = new util.ArrayList[Object]()
    cond.add(XmlRPCHelper.toJavaLists(conditions))
    cond
  }
}
