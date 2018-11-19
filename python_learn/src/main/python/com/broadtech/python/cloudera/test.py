from com.broadtech.python.cloudera.utils import JsonJmxBeanMetricsExtractor, visit_json

class A:
    def metircs(self, json):
        role_extractor = JsonJmxBeanMetricsExtractor(
            self._metrics.get_entity_beans(self._service_type + "-" + self._role_type))
        visit_json(json, [role_extractor])


b = A().metircs(
    "{\"beans\":[{\"request\":698,\"useMemory\":41.69612121582031,\"bytes\":347931,\"usableMemory\":3458.8038787841797,\"name\":\"Metrics:type=KeyPerMetric\",\"freeMemory\":196.3038787841797}]}")
print(b)
