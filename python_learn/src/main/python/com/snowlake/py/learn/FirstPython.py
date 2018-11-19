#!/usr/bin/python
# -*- coding: UTF-8 -*-

# package: 包是一个分层次的文件目录结构，它定义了一个由模块及子包，和子包下的子包等组成的 Python 的应用环境
# package该文件夹下必须存在 __init__.py 文件, 该文件的内容可以为空。__init__.py 用于标识当前文件夹是一个包
# 应用其它package下模块通过 from…import 语句

# 命名空间和作用域
# 变量是拥有匹配对象的名字（标识符）。命名空间是一个包含了变量名称们（键）和它们各自相应的对象们（值）的字典
# 一个 Python 表达式可以访问局部命名空间和全局命名空间里的变量。如果一个局部变量和一个全局变量重名，则局部变量会覆盖全局变量
# Python 会智能地猜测一个变量是局部的还是全局的，它假设任何在函数内赋值的变量都是局部的。
# 因此，如果要给函数内的全局变量赋值，必须使用 global 语句。
# global VarName 的表达式会告诉 Python， VarName 是一个全局变量

# Python 的 from 语句让你从模块中导入一个指定的部分到当前命名空间中
from com.snowlake.py.common import MyUtil

print("Hello World")
MyUtil.my_format("123 kk 你好")
