package com.broadtech.trial

import org.apache.spark.sql.types.DataTypes

object NewClassTest {

  def main(args: Array[String]): Unit = {
    val c = new NewClass(1, "Seek", 23)
    //val c: NewClass = NewClass.apply
    c.showDetails()
    println("Hello world")

    var map: Map[Int, Int] = Map()
    map += (1 -> 2)
    map += 3 -> 4

    // 遍历
    map.foreach { case (k, v) => println(s"$k $v") }
    map.foreach(e => println(s"${e._1} ${e._2}"))

    val types = List(DataTypes.ByteType, DataTypes.ShortType, DataTypes.IntegerType, DataTypes.LongType, DataTypes.DoubleType, DataTypes.StringType)
    types.foreach(t => println(t.json))
  }
}
