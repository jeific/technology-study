# Copyright (c) 2015 Cloudera, Inc. All rights reserved.

import cmf.monitor.generic.adapter
import cmf.monitor.generic.hbase_adapters
import cmf.monitor.generic.hdfs_adapters
import cmf.monitor.generic.hive_adapters
import cmf.monitor.generic.impala_adapters
import cmf.monitor.generic.mgmt_adapters
import cmf.monitor.generic.zookeeper_adapters
import cmf.monitor.generic.yarn_adapters
import cmf.monitor.generic.kafka_adapters
import cmf.monitor.generic.kudu_adapters
import cmf.monitor.generic.hue_adapters
import cmf.monitor.generic.bdp_adapters

class AdapterFactory(object):
  """
  Factory for making monitoring Adapter classes.
  """

  def make_adapter(self, service_type, role_type, safety_valve, daemon = None):
    """
    Makes an Adapter for the input role type.
    """
    if service_type == 'ZOOKEEPER' and role_type == 'SERVER':
      return cmf.monitor.generic.zookeeper_adapters.ServerAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'ACTIVITYMONITOR':
      return cmf.monitor.generic.mgmt_adapters.ActivityMonitorAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'HOSTMONITOR':
      return cmf.monitor.generic.mgmt_adapters.HostMonitorAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'SERVICEMONITOR':
      return cmf.monitor.generic.mgmt_adapters.ServiceMonitorAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'EVENTSERVER':
      return cmf.monitor.generic.mgmt_adapters.EventServerAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'REPORTSMANAGER':
      return cmf.monitor.generic.mgmt_adapters.ReportsManagerAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'NAVIGATOR':
      return cmf.monitor.generic.mgmt_adapters.NavigatorAdapter(safety_valve)
    elif service_type == 'MGMT' and role_type == 'NAVIGATORMETASERVER':
      return cmf.monitor.generic.mgmt_adapters.NavigatorMetaServerAdapter(safety_valve)
    elif service_type == 'HDFS' and role_type == 'NAMENODE':
      return cmf.monitor.generic.hdfs_adapters.NameNodeAdapter(safety_valve)
    elif service_type == 'HDFS' and role_type == 'SECONDARYNAMENODE':
      return cmf.monitor.generic.hdfs_adapters.SecondaryNameNodeAdapter(safety_valve)
    elif service_type == 'HDFS' and role_type == 'JOURNALNODE':
      return cmf.monitor.generic.hdfs_adapters.JournalNodeAdapter(safety_valve)
    elif service_type == 'HDFS' and role_type == 'DATANODE':
      return cmf.monitor.generic.hdfs_adapters.DataNodeAdapter(safety_valve)
    elif service_type == 'HDFS' and role_type == 'DSSDDATANODE':
      return cmf.monitor.generic.hdfs_adapters.DssdDataNodeAdapter(safety_valve)
    elif service_type == 'YARN' and role_type == 'RESOURCEMANAGER':
      return cmf.monitor.generic.yarn_adapters.ResourceManagerAdapter(safety_valve)
    elif service_type == 'YARN' and role_type == 'JOBHISTORY':
      return cmf.monitor.generic.yarn_adapters.JobHistoryAdapter(safety_valve)
    elif service_type == 'YARN' and role_type == 'NODEMANAGER':
      return cmf.monitor.generic.yarn_adapters.NodeManagerAdapter(safety_valve)
    elif service_type == 'KAFKA' and role_type == 'KAFKA_BROKER':
      return cmf.monitor.generic.kafka_adapters.BrokerAdapter(safety_valve)
    elif service_type == 'KUDU' and role_type == 'KUDU_TSERVER':
      return cmf.monitor.generic.kudu_adapters.TServerAdapter(safety_valve)
    elif service_type == 'KUDU' and role_type == 'KUDU_MASTER':
      return cmf.monitor.generic.kudu_adapters.MasterAdapter(safety_valve)
    elif service_type == 'HIVE' and role_type == 'HIVEMETASTORE':
      return cmf.monitor.generic.hive_adapters.HiveMetastoreAdapter(safety_valve)
    elif service_type == 'HIVE' and role_type == 'HIVESERVER2':
      return cmf.monitor.generic.hive_adapters.HiveServer2Adapter(safety_valve)
    elif service_type == 'HBASE' and role_type == "MASTER":
      return cmf.monitor.generic.hbase_adapters.MasterAdapter(safety_valve)
    elif service_type == 'HBASE' and role_type == "REGIONSERVER":
      return cmf.monitor.generic.hbase_adapters.RegionServerAdapter(safety_valve)
    elif service_type == "HUE" and role_type == "HUE_SERVER":
      return cmf.monitor.generic.hue_adapters.HueServerAdapter(safety_valve)
    elif service_type == "IMPALA" and role_type == "CATALOGSERVER":
      return cmf.monitor.generic.impala_adapters.CatalogServerAdapter(safety_valve)
    elif service_type == "IMPALA" and role_type == "STATESTORE":
      return cmf.monitor.generic.impala_adapters.StateStoreAdapter(safety_valve)
    elif service_type == "IMPALA" and role_type == "IMPALAD":
      return cmf.monitor.generic.impala_adapters.ImpaladAdapter(daemon, safety_valve)
    elif service_type == "BDP":
      return cmf.monitor.generic.bdp_adapters.BdpAdapter(safety_valve, role_type)
    #elif service_type == "BDP" and role_type == "BRD_QUERY":
    #  return cmf.monitor.generic.bdp_adapters.QueryAdapter(safety_valve)
    else:
      return cmf.monitor.generic.adapter.Adapter(service_type, role_type, safety_valve)
