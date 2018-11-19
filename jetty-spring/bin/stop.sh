#!/usr/bin/env bash
cd `dirname $0`
cd ..
path=$PWD
proName="JettySpringServer"

#ps -ef|grep -w $proName|grep $path|grep -v grep|awk '{print $2}'|xargs -n 1 -i kill {}
pro=`ps -ef|grep -w $proName|grep $path|grep -v grep|awk '{print $2}'`
if [ -n "$pro" ];then
 echo stopping process: $pro ...
 kill $pro
fi
