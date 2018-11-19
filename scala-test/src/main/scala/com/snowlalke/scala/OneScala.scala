package com.snowlalke.scala

import java.util

object OneScala {
  def main(args: Array[String]): Unit = {
    val map = new util.HashMap[String, String]()
    map.put("key", "1234")
    print(map)
    import scala.collection.JavaConverters._
    map.asScala.foreach(e => println(e._1 + "_" + e._2))
    val host = "master01:1234"
    host.split(",").foreach(k => println(k))
  }
}
