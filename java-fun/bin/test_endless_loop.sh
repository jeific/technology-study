#!/bin/bash

index=1
while true
do
    currenttime=`date "+%Y-%m-%d %H:%M:%S"`
    echo "[${currenttime}] 第 [${index}] 次循环"
    index=`expr $index + 1`
    sleep 1
done

