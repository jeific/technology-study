1. 解压
2. 检查确认配置 bin/start.sh
export ZK_QUORUM=master01:2181,master02:2181,slave01:2181
# 清理zk节点，默认蓝鲸的任务调度选举节点
export CLR_ZK_PATH=/bluewhale/bdp-web/job/leaderElect
3. 运行 bin/start.sh