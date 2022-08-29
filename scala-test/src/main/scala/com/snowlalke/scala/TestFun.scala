package com.snowlalke.scala

object TestFun {
  def main(args: Array[String]): Unit = {
    val p = "^\\d+[\\.]?\\d+$".r
    println(p.findFirstIn("12.3").isDefined)
    println(p.findFirstIn("w12.3").isDefined)
  }
}
