# Copyright (c) 2015 Cloudera, Inc. All rights reserved.

import logging
import os
import json

from cmf.monitor import constants
from cmf.monitor.generic.hadoop_adapter import HadoopAdapter
from cmf.monitor.generic.service_defined_metrics import \
  ServiceDefinedMetricsJsonJmxBeans
from cmf.monitor.generic.utils import SplittingSourceProcessor, \
  JsonJmxBeanMetricsExtractor, visit_json
from cmf.monitor.jmx_beans import JsonParserJmx
from cmf.throttling_logger import ThrottlingLogger

from cmf.monitor.generic.adapter import Adapter
from cmf.monitor.generic.service_defined_metrics import ServiceDefinedMetrics
from cmf.monitor.generic.utils import parse_properties_file
from cmf.url_util import urlopen_with_timeout
from cmf.monitor.json_metrics_utils import add_derived_jvm_metrics_jmx
from cmf.monitor.json_metrics_utils import add_metrics_jmx
from cmf.monitor.json_metrics_utils import add_pause_metrics

LOG = logging.getLogger('BdpAdapters')
#THROTTLED_LOG = ThrottlingLogger(LOG, 60 * 60) # Once an hour.

class _BdpAdapter(Adapter):
  """
  Base class for all Bdp service role adapters. This exists to provide utility
  methods and to ensure consistency in the handling of common metrics.
  """

  BDP_MONITORING_PROPERTIES = "bdp-monitoring.properties"

  def __init__(self,
               role_type,
               safety_valve,
               host_key=None,
               port_key=None,
               enabled_key=None):
    Adapter.__init__(self, "BDP", role_type, safety_valve)
    self._host_key = host_key
    self._port_key = port_key
    self._enabled_key = enabled_key
    self._metrics = None

  def get_metrics_url(self, conf):
    return self._get_api_url(conf, "jmx")

  def _get_api_url(self, conf, endpoint):
    if conf is None:
      raise Exception("A configuration is required!")
    if endpoint is None:
      raise Exception("An endpoint is required!")

    if (self._host_key is None or
        self._port_key is None or
        self._enabled_key is None):
      return None

    path = os.path.join(os.path.dirname(conf.path),
                        _BdpAdapter.BDP_MONITORING_PROPERTIES)
    try:
      LOG.info("self: %s" % self.role_type)
      parser = parse_properties_file(path, self.section)
      if parser is None:
        raise Exception("%s does not exist" % (os.path.abspath(path)))

      enabled = parser.getboolean(self.section, self._enabled_key)
      if not enabled:
        return None
      """
      host = parser.get(self.section, self._host_key)
      if host is None:
        LOG.error("%s entry missing from %s" % (self._host_key, path))
        return None
      """
      host = conf.get(self.section, Adapter._HOST)
      if host is None:
        LOG.error("%s entry missing from %s" % (Adapter._HOST,
                                                os.path.abspath(conf.path)))
        return None
      port = parser.getint(self.section, self._port_key)
      if port is None:
        LOG.error("%s entry missing from %s" % (self._port_key, path))
        return None
      return "http://%s:%s/%s" % (host, port, endpoint)
    except:
      LOG.exception("Failed to read conf file '%s'" % (os.path.abspath(path)))
      return None
  
  def read_service_defined_metrics(self, path):
    if path is None:
      raise Exception("A path is required!")
    #self._metrics = ServiceDefinedMetrics(path, SplittingSourceProcessor('::'))
    #ServiceDefinedMetricsJsonJmxBeans(ServiceDefinedMetrics)
    self._metrics = ServiceDefinedMetricsJsonJmxBeans(path, SplittingSourceProcessor('::'))
  """
  def parse_metrics_from_url(self, conf, json):
    LOG.info("### parse_metrics_from_url")
    return JsonParserJmx(json, self.role_type)
  
  def add_webserver_metrics(self, version, update, metrics, accessors):
    add_derived_jvm_metrics_jmx(update, metrics)
    LOG.info("### add_webserver_metrics begin..")
    if version == constants.SERVICE_VERSION_CDH4:
      version_string = "cdh4"
    elif version == constants.SERVICE_VERSION_CDH5:
      version_string = "cdh5"
    else:
      raise Exception("Unknown %s version: %s" % (self._role_type, version))
    add_metrics_jmx(update, metrics, accessors, self._role_type, version_string)
    add_pause_metrics(update, metrics)
    LOG.info("### add_webserver_metrics end..")
    LOG.info(json.dumps(update._metrics));
  """
class BdpAdapter(_BdpAdapter):
  # Config keys used to read from bdp-monitoring.properties.
  _BRD_WEBSERVER_HOST = "bdp.http.metrics.host"
  _BRD_WEBSERVER_PORT = "bdp.http.metrics.port"
  _BRD_WEBSERVER_ENABLED = "monitoring.enabled"

  def __init__(self, role_type, safety_valve):
    _BdpAdapter.__init__(self,
                           role_type,
                           safety_valve,
                           BdpAdapter._BRD_WEBSERVER_HOST,
                           BdpAdapter._BRD_WEBSERVER_PORT,
                           BdpAdapter._BRD_WEBSERVER_ENABLED)
    # This exists to ease testing.
    self._url_open_function = urlopen_with_timeout
  
  def parse_metrics_from_url(self, conf, jsonv):
    if jsonv is None:
      raise Exception("a json sample is required")
    if self._metrics is None:
      raise Exception("No metrics have been loaded!")
    #LOG.info("### parse_metrics_from_url")
    #LOG.info(json.dumps(jsonv))
    role_extractor = JsonJmxBeanMetricsExtractor(
      self._metrics.get_entity_beans(self._service_type + "-" + self._role_type))
    visit_json(jsonv, [role_extractor])
    return role_extractor.metrics
  
  def add_webserver_metrics(self, version, update, metrics, accessors):
    #LOG.info("### add_webserver_metrics begin..")
    LOG.info("%s: %s" % (self._role_type, json.dumps(metrics)))
    for metricid, value in metrics.iteritems():
      update.add_metric(metricid, value)
    #LOG.info("### add_webserver_metrics end..")
    #LOG.info(json.dumps(update._metrics));

class QueryAdapter(_BdpAdapter):
  # Config keys used to read from bdp-monitoring.properties.
  _BRD_QUERY_WEBSERVER_HOST = "bdp.http.metrics.host"
  _BRD_QUERY_WEBSERVER_PORT = "bdp.http.metrics.port"
  _BRD_QUERY_WEBSERVER_ENABLED = "monitoring.enabled"
  _ROLE_ENTITY_TYPE = 'BDP-BRD_QUERY'

  def __init__(self, safety_valve):
    _BdpAdapter.__init__(self,
                           "BRD_QUERY",
                           safety_valve,
                           QueryAdapter._BRD_QUERY_WEBSERVER_HOST,
                           QueryAdapter._BRD_QUERY_WEBSERVER_PORT,
                           QueryAdapter._BRD_QUERY_WEBSERVER_ENABLED)
    # This exists to ease testing.
    self._url_open_function = urlopen_with_timeout
  
  def parse_metrics_from_url(self, conf, jsonv):
    if jsonv is None:
      raise Exception("a json sample is required")
    if self._metrics is None:
      raise Exception("No metrics have been loaded!")
    #LOG.info("### parse_metrics_from_url")
    #LOG.info(json.dumps(jsonv))
    role_extractor = JsonJmxBeanMetricsExtractor(
      self._metrics.get_entity_beans(QueryAdapter._ROLE_ENTITY_TYPE))#self._role_type=BRD_QUERY
    visit_json(jsonv, [role_extractor])
    return role_extractor.metrics
  
  def add_webserver_metrics(self, version, update, metrics, accessors):
    #LOG.info("### add_webserver_metrics begin..")
    LOG.info("%s: %s" % (self._role_type, json.dumps(metrics)))
    for metricid, value in metrics.iteritems():
      update.add_metric(metricid, value)
    #LOG.info("### add_webserver_metrics end..")
    #LOG.info(json.dumps(update._metrics));
  
