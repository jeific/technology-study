#!/bin/bash
cd `dirname $0`/..
MAIN_HOME=$PWD

#参数
export ZK_QUORUM=master01:2181,master02:2181,slave01:2181
# 清理zk节点，默认蓝鲸的任务调度选举节点
export CLR_ZK_PATH=/bluewhale/bdp-web/job/leaderElect

java -cp .:$MAIN_HOME/lib/* com.bluewhale.BDPWeb
