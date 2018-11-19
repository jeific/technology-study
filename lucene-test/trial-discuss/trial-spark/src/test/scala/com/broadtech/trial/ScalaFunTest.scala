package com.broadtech.trial

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * create by 2018/1/19 15:32<br>
  *
  * @author Yuanjun Chen
  */
object ScalaFunTest {
  def main(args: Array[String]): Unit = {
    val arr = Array[Int](1, 4, 5, 6, 1, 4)
    println(arrayDistinct(arr).mkString(","))

    println(arrayDistinct(Array("1", "3", "1", "1")).mkString(","))
    val v = Predef.Double2double(null)
    println(v + "\t" + (v == null))
  }

  def arrayDistinct[E: ClassTag](arr: Array[E]): Array[E] = {
    val set = new java.util.HashSet[E](arr.length)
    arr.foreach(a => set.add(a))
    import scala.collection.JavaConverters._
    set.asScala.toArray
  }


  def arrayDistinct2(arr: Array[Int]): Array[Int] = {
    val set: mutable.HashSet[Int] = new mutable.HashSet[Int]()
    arr.foreach(a => set.add(a))
    set.toArray
  }
}
