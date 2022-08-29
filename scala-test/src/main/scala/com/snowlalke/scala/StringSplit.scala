package com.snowlalke.scala

object StringSplit {

  def main(args: Array[String]): Unit = {
    val str = "hello,ok,okay"
    str.toUpperCase().split(",").foreach(s => println(s))
  }
}
