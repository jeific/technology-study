# -*- coding:utf-8 -*-
import os

import fun

# fun.p()

print(fun.f2(4, 2))
print(fun.f2(1, 2))

# print(raw_input("input:"))

# str = input("可以接收一个Python表达式作为输入，并将运算结果返回,请输入：") #[x*5 for x in range(2,10,2)]
# print (u"你输入的内容是: ", str)

p = "D:\WorkSpace\TestProject"
f1 = open(p + "\pom.xml", "rb")
# for k in f1:
#    print (k)
print("file >> %s" % (os.fstat(f1.fileno())))
f1.close()
print (os.listdir(p))

try:
    f1 = open("kk")
except IOError, arg:
    print(">> %s" % arg)


# 自定义异常
class Networkerror(RuntimeError):
    def __init__(self, arg):
        self.args = arg


try:
    raise Networkerror("Bad hostname")
except Networkerror, e:
    print (e)
