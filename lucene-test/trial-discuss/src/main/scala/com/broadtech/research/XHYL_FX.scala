package com.broadtech.research

/**
  * 测试多层次泛型继承
  */

class M[T, R] {
  def map(t: K[T, R]): Unit = {
    println("kkk")
    t.m2()
  }
}

class A[T] {
  def transform[R](k: T, r: R): R = {
    (k + " trans").asInstanceOf[R]
  }
}

abstract class B[T] {
  def transform[R](map: M[T, R]): Unit

  def m2(): Unit
}

abstract class K[T, R](a: B[T]) {
  def trans(): Unit

  def m2(): Unit = println("k.m2")
}

class Y[T, R](b: B[T]) extends K[T, R](b) {
  override def trans(): Unit = {
  }

  override def m2(): Unit = {
    b.m2()
  }

}

class XHYL_FX[T, R] extends B[T] {
  override def transform[R1](map: M[T, R1]): Unit = {
    map.map(new Y[T, R1](this)) // 语法允许
  }

  def m2(): Unit = print("XHYL_FX.m2")
}

object XHYL_FX {
  def main(args: Array[String]): Unit = {
    new XHYL_FX[String, String].transform[Long](new M[String, Long])
  }
}
