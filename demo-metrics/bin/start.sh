#!/bin/bash
source /etc/profile
ulimit -n 65535
cd `dirname $0`
cd ..
path=$PWD

mkdir -p $path/logs
nohup java -server -Xmx256m -Xms64m -cp .:$path/conf:$path/lib/*:$path/slib/* -XX:PermSize=100m -XX:MaxPermSize=300m -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:+OptimizeStringConcat -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=68 -XX:CMSFullGCsBeforeCompaction=0 -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$path/logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$path/logs/outOfMemory.log  -Djava.util.Arrays.useLegacyMergeSort=true -Dcom.sun.management.config.file=conf/management.properties com.broadtech.demo.metric.Demo > $path/logs/nohup.out 2>&1 &
