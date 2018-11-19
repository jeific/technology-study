package com.broadtech.trial

object NewClass {
  def apply: NewClass = new NewClass(1, "Tome", 12)
}

class NewClass(id: Int, name: String) {
  var age: Int = 0

  def showDetails() {
    println(id + " " + name + " " + age)
  }

  def this(id: Int, name: String, age: Int) {
    this(id, name) // Calling primary constructor, and it is first line
    this.age = age
  }
}
