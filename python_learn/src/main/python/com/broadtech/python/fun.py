import os

def p():
    for k in os.environ:
        print("%25s:\t%s" %(k,os.environ[k]))

def f2(a, b):
    c = a/b
    return c