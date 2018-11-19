#!/bin/bash
source /etc/profile
ulimit -n 65535
cd `dirname $0`
cd ..
path=$PWD

mkdir -p $path/logs

webapp="webapp"
cp=".:$path/conf:$path/$webapp/WEB-INF/classes:$path/lib/*:$path/$webapp/WEB-INF/lib/*"
cmd="java -server -Xmx1g -Xms128m -cp $cp -XX:PermSize=100m -XX:MaxPermSize=300m -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:+OptimizeStringConcat -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=68 -XX:CMSFullGCsBeforeCompaction=0 -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$path/logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$path/logs/outOfMemory.log -Djava.util.Arrays.useLegacyMergeSort=true"
debug="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9527"
mainClass="com.broadtech.jetty.BenchServer online"

if [ -z $1 ]; then
  nohup $cmd $mainClass > $path/logs/nohup.out 2>&1 &
elif [ 'debug' = $1 ];then
  nohup  $cmd $debug $mainClass > $path/logs/nohup.out 2>&1 &
elif [ 'exec' = $1 ];then
  $cmd $mainClass
else
  echo "参数错误，仅额外支持debug|exec选项"
fi