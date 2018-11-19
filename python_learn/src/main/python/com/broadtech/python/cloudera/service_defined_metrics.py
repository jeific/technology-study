# Copyright (c) 2015 Cloudera, Inc. All rights reserved.

import re

import simplejson


class SourceProcessor(object):
    """
    This interface defines a method that will be called on each metric source
    string as part of loading the service-metrics.properties file. This is
    intended to be subclassed per-source convention. For example, a given source
    convention might be to split the source on :: and actually store the result
    list in our source map.
    """

    def process_source(self, source):
        return source


class ServiceDefinedMetrics(object):
    """
    This object is parses the service-metrics.properties file that captures the
    metrics defined for a service, its roles and its associated entity types.
    """

    def __init__(self, path, source_processor):
        if path is None:
            raise Exception("A path is required!")
        if source_processor is None:
            raise Exception("A source_processor is required!")

        # This is a map from entity type -> metric id -> processed source. See the
        # comments in _process_source for more information.
        self._sources = dict()
        # This is a set of metric IDs for all counter metrics in the file.
        self._counters = set()

        data = open(path, 'r')
        try:
            parsed = simplejson.load(data)
            for entity_type, entity_metrics in parsed.iteritems():
                self._sources[entity_type] = dict()
                for metricid, metric in entity_metrics.iteritems():
                    metricid = int(metricid)
                    self._sources[entity_type][metricid] = \
                        source_processor.process_source(metric.get('source'))
                    if metric.get('counter', False):
                        self._counters.add(metricid)
        finally:
            data.close()

    def get_sources(self, entity_type):
        """
        Returns a map from metric ID -> processed source for the input entity type.
        """
        return self._sources.get(entity_type, dict())

    def is_counter(self, metricid):
        """
        Returns whether or not the input metric ID is for a counter.
        """
        return metricid in self._counters


class JmxBeanMetric(object):
    """
    A class holding metadata about a single metric collected froma json serialized
    jmx beans end-point. It exposes the metric name as it appears in the jmx bean,
    the metric id in the cloudera manager schema, and the default value if one
    exists for the metric.
    """

    def __init__(self, jmx_bean_metric_name, metric_id, default=None):
        if jmx_bean_metric_name is None or metric_id is None:
            raise Exception("Metric bean name or metric id cannot be None")
        self._jmx_bean_metric_name = jmx_bean_metric_name
        self._metric_id = metric_id
        self._default = default

    @property
    def jmx_bean_metric_name(self):
        return self._jmx_bean_metric_name

    @property
    def metric_id(self):
        return self._metric_id

    @property
    def default_value(self):
        return self._default

    @property
    def has_default_value(self):
        return not self._default is None


class JmxBeanMetrics(object):
    """
    A helper class holding information about all metrics that should be extracted
    from a specific json serialized jmx bean.
    """

    def __init__(self, regex_pattern):
        self._regex = re.compile(regex_pattern)
        self._bean_metrics = []

    @property
    def regex(self):
        return self._regex

    @property
    def metrics(self):
        """
        returns a copy of the list of JmxBeanMetric objects that this bean contain.
        """
        return self._bean_metrics[:]

    def add_metric(self, jmx_bean_metric_name, metric_id, default=None):
        self._bean_metrics.append(JmxBeanMetric(jmx_bean_metric_name,
                                                metric_id,
                                                default))


class ServiceDefinedMetricsJsonJmxBeans(ServiceDefinedMetrics):
    """
    A service defined metrics instance that goes over the sources and process
    sources for metrics extracted from json serialized jmx end-points.
    """

    def __init__(self, path, source_processor):
        ServiceDefinedMetrics.__init__(self, path, source_processor)
        # A dictionary holding a map between an entity type and a map of all the
        # beans that we should attempt to extract metrics from.
        self._entity_beans = dict()
        for entity, sources in self._sources.iteritems():
            self._entity_beans[entity] = self._compile_bean_info_for_entity(sources)

    def _compile_bean_info_for_entity(self, sources):
        beans = dict()
        for metric_id, source in sources.iteritems():
            if len(source) < 2:
                # This is not our metric
                continue
            bean_regex = source[0]
            if not bean_regex.startswith("REGEX$$"):
                continue
            bean_regex = bean_regex[len("REGEX$$"):]
            bean_metric_name = source[1]
            default = None
            if len(source) == 3:
                default = float(source[2])

            bean = beans.get(bean_regex, None)
            if bean is None:
                bean = JmxBeanMetrics(bean_regex)
                beans[bean_regex] = bean
            bean.add_metric(bean_metric_name, metric_id, default)
        return beans

    def get_entity_beans(self, entity):
        """
        Returns a copy of a map between a regex pattern and the JmxBeanMetrics
        object that can be used to extract metrics for a bean that matches the
        pattern.
        """
        return self._entity_beans.get(entity, dict()).copy()
