package com.ecfront.odoows.helper

import java.net.URL
import java.util

import org.apache.xmlrpc.client.{XmlRpcClient, XmlRpcClientConfigImpl}

import scala.collection.JavaConversions._

case class XmlRPCHelper(baseUrl: String) {

  private val client = new XmlRpcClient()

  def request(path: String, method: String, args: List[Any] = List()): Any = {
    val commonConfig = new XmlRpcClientConfigImpl()
    commonConfig.setServerURL(new URL(baseUrl + path))
    client.execute(commonConfig, method, args)
  }
}

object XmlRPCHelper {

  def toJavaMap[E](map: Map[String, E]): util.HashMap[String, E] = new util.HashMap[String, E](map)

  def toJavaList[E](list: List[E]): util.ArrayList[E] = new util.ArrayList[E](list)

  def toJavaLists[E](list: List[List[E]]): util.ArrayList[util.ArrayList[E]] = toJavaList(list.map(toJavaList))

}
