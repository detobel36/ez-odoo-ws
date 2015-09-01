package com.ecfront.odoows.helper

import java.net.URL
import java.util

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.xmlrpc.client.{XmlRpcClient, XmlRpcClientConfigImpl}

import scala.collection.JavaConversions._

case class XmlRPCHelper(baseUrl: String) extends LazyLogging {

  private val client = new XmlRpcClient()

  def request(path: String, method: String, args: List[Any] = List(), retryTime: Int = 0): Any = {
    val commonConfig = new XmlRpcClientConfigImpl()
    commonConfig.setServerURL(new URL(baseUrl + path))
    try {
      client.execute(commonConfig, method, args)
    } catch {
      case e: Exception =>
        if (e.getMessage != null && (e.getMessage.contains("current transaction is aborted") || e.getMessage.contains("One of the documents you are trying to access has been deleted"))) {
          if (retryTime <= 8) {
            Thread.sleep(1000)
            logger.debug(s"RPC execute error retry [${retryTime + 1}]: ${e.getMessage}  ...")
            request(path, method, args, retryTime + 1)
          } else {
            logger.warn("RPC execute error : " + e.getMessage)
            throw e
          }
        } else {
          logger.warn("RPC execute error : " + e.getMessage)
          throw e
        }
    }
  }

}

object XmlRPCHelper {

  def toJavaMap[E](map: Map[String, E]): util.HashMap[String, E] = new util.HashMap[String, E](map)

  def toJavaList[E](list: List[E]): util.ArrayList[E] = new util.ArrayList[E](list)

  def toJavaLists(list: List[Any]): util.ArrayList[Any] = {
    val l: List[Any] = list.map {
      case e: List[Any] => new util.ArrayList(e)
      case e: String => e
      case item => item
    }
    new util.ArrayList[Any](l)
  }

}
