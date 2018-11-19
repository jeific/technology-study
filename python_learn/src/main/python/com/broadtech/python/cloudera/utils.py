# Copyright (c) 2015 Cloudera, Inc. All rights reserved.

import ConfigParser

try:
    from cStringIO import StringIO
except:
    from StringIO import StringIO
import os
import sys

from com.broadtech.python.cloudera.service_defined_metrics import SourceProcessor
from com.broadtech.python.cloudera import enum

# Enum to represent the version type
VersionType = enum(MAJOR=1,
                   MINOR=2,
                   PATCH=3)


class SplittingSourceProcessor(SourceProcessor):
    """
    A simple SourceProcessor that splits the context by the input separator.
    """

    def __init__(self, separator):
        self._separator = separator

    def process_source(self, source):
        return source.split(self._separator)


def parse_properties_file(path, section):
    """
    Parses a properties file without a section header into a ConfigParser. This
    exists primarily to make it easy to interact with the CSD generated properties
    files. This will return None if the file does not exist.
    """
    if path is None:
        raise Exception("A path is required!")
    if section is None:
        raise Exception("A section is required!")

    f = None
    try:
        if not os.path.exists(path):
            return None

        # This is a little goofy: the properties file is basically in .ini format,
        # but it has no section header. We play games with StringIO to let us use
        # ConfigParser.
        f = open(path, 'r')
        config = StringIO()
        config.write('[%s]\n' % section)
        config.write(f.read())
        f.close()
        f = None
        config.seek(0, 0)

        parser = ConfigParser.SafeConfigParser()
        parser.readfp(config)

        return parser
    except:
        if f is not None:
            f.close()
        raise


class JsonVisitor(object):
    """
    Abstract class for visiting nodes in a JSON tree.
    """

    def visit_root(self, json):
        """
        Called to visit the root JSON node.
        """
        pass

    def visit_node(self, key, json):
        """
        Called to visit non-root JSON nodes. If False is returned, iteration will be
        stopped.
        """
        return False


def visit_json(json, visitors):
    """
    Calls each visitor's visit methods on the root and first level nodes of the
    input JSON tree.
    """
    for visitor in visitors:
        visitor.visit_root(json)
        for key, value in json.iteritems():
            if not visitor.visit_node(key, value):
                break


def extract_metric_from_json(json, source):
    """
    Utility method that takes a json object and a list of paths to use to look up
    a numeric metric value.
    """
    value = json
    for part in source:
        value = value.get(part, None)
        if value is None:
            return None
    try:
        return float(value)
    except ValueError:
        return None


class SimpleMetricsExtractor(JsonVisitor):
    """
    A simple metrics extractor that takes a dictionary of metric sources which
    it uses to look up a set of metrics.
    """

    def __init__(self, sources):
        self._sources = sources
        self.metrics = dict()

    def visit_root(self, json):
        for metricid, source in self._sources.iteritems():
            value = extract_metric_from_json(json, source)
            if value is None:
                continue
            self.metrics[metricid] = value


class SimpleMetricsExtractorWithDefaults(JsonVisitor):
    """
    A simple metrics extractor that takes a metric sources which it uses to look
    up a set of metrics. If the metric source has a default value and the metric
    cannot be found the defaul is used.
    """

    class MetricSource(object):
        """
        A helper class to hold metadata about one metric source.
        """

        def __init__(self, source, default_value=None):
            if source is None:
                raise Exception("source cannot be None")

            self._source = source
            self._default_value = default_value

        @property
        def source(self):
            return self._source

        @property
        def default_value(self):
            return self._default_value

    def __init__(self, sources):
        self._sources = sources
        self.metrics = dict()

    def visit_root(self, json):
        for metric_id, metric_source in self._sources.iteritems():
            source = metric_source.source
            default_value = metric_source.default_value

            value = extract_metric_from_json(json, source)
            if value is None:
                value = default_value

            if value is None:
                continue
            self.metrics[metric_id] = value


class JsonJmxBeanMetricsExtractor(JsonVisitor):
    """
    A collector that knows to extract metrics from Hadoop json serialized jmx
    beans end-points. It iterates over the list of beans and try to match the
    regular expression with the bean name. If a match is found then it tries to
    extract the metrics from the bean.
    """

    def __init__(self, sources):
        self._sources = sources
        self.metrics = dict()

    def visit_root(self, json):
        if not 'beans' in json:
            return
        for bean in json['beans']:
            if not 'name' in bean:
                continue
            for name, jmx_bean in self._sources.iteritems():
                if jmx_bean.regex.match(bean['name']) is None:
                    continue
                for bean_metric in jmx_bean.metrics:
                    if bean_metric.jmx_bean_metric_name in bean:
                        try:
                            self.metrics[bean_metric.metric_id] = \
                                float(bean[bean_metric.jmx_bean_metric_name])
                        except ValueError:
                            pass
                    elif bean_metric.has_default_value:
                        self.metrics[bean_metric.metric_id] = bean_metric.default_value

                # We assume that each bean will be matched with at most one regular
                # expression and that a regular expression will match at most one
                # bean.
                del self._sources[name]
                break


class VersionUtils(object):
    """
    A utility class to handle version comparisons for release strings.

    Note that callers of this utility should generally try to do version checking on the
    server-side rather than agent-side.
    """

    @staticmethod
    def to_tuple(version, version_type):
        """
        Converts a given version string to a tuple based on the
        version type.
        :param version: A string representing the version.
        :param version_type: An int representing the {@code VersionType} enum. Truncates portions
        of the version less significant than this type.
        :return: True if version1 >= version2.
        """
        out = []
        for part in version.split(".")[:version_type]:
            if part == 'x':
                # If any member of the version string is "x", it is considered to be the highest
                # version part.
                if sys.version_info < (3, 0):
                    part = sys.maxint
                else:
                    # In Python 3, sys.maxint constant was removed, using sys.maxsize instead.
                    part = sys.maxsize
            else:
                part = int(part)
            out.append(part)
        return tuple(out)

    @staticmethod
    def at_least(version1, version2, version_type):
        """
        Shorthand for equal or greater than. Compares two version tuples up-to the desired
        version type.
        :param version1: 1st version string.
        :param version2: 2nd version string.
        :param version_type: An int representing the {@code VersionType} enum. Truncates portions
        of the version less significant than this type.
        :return: True if version1 < version2.
        """
        return VersionUtils.to_tuple(version1, version_type) \
               >= VersionUtils.to_tuple(version2, version_type)

    @staticmethod
    def less_than(version1, version2, version_type):
        """
        Strictly less than. Compares two version tuples up-to the desired version type.
        :param version1: 1st version string.
        :param version2: 2nd version string.
        :param version_type: An int representing the {@code VersionType} enum. Truncates portions
        of the version less significant than this type.
        :return: A boolean value.
        """
        return VersionUtils.to_tuple(version1, version_type) \
               < VersionUtils.to_tuple(version2, version_type)
