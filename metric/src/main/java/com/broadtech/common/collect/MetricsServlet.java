package com.broadtech.common.collect;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A servlet which returns the metrics in a given registry as an {@code application/json} response.
 */
public class MetricsServlet extends HttpServlet {
    private static final String RATE_UNIT = MetricsServlet.class.getCanonicalName() + ".rateUnit";
    private static final String DURATION_UNIT = MetricsServlet.class.getCanonicalName() + ".durationUnit";
    private static final String SHOW_SAMPLES = MetricsServlet.class.getCanonicalName() + ".showSamples";
    private static final String METRICS_REGISTRY = MetricsServlet.class.getCanonicalName() + ".registry";
    private static final String ALLOWED_ORIGIN = MetricsServlet.class.getCanonicalName() + ".allowedOrigin";
    private static final String METRIC_FILTER = MetricsServlet.class.getCanonicalName() + ".metricFilter";
    private static final String CALLBACK_PARAM = MetricsServlet.class.getCanonicalName() + ".jsonpCallback";

    private static final long serialVersionUID = 1049773947734939602L;
    private static final String CONTENT_TYPE = "application/json";

    private String allowedOrigin;
    private String jsonpParamName;
    private transient MetricRegistry registry;
    private transient MetricNormal metricNormal;

    public MetricsServlet() {
    }

    public MetricsServlet(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final ServletContext context = config.getServletContext();
        if (null == registry) {
            final Object registryAttr = context.getAttribute(METRICS_REGISTRY);
            if (registryAttr instanceof MetricRegistry) {
                this.registry = (MetricRegistry) registryAttr;
            } else {
                throw new ServletException("Couldn't find a MetricRegistry instance.");
            }
        }
        this.allowedOrigin = context.getInitParameter(ALLOWED_ORIGIN);
        this.jsonpParamName = context.getInitParameter(CALLBACK_PARAM);

        final TimeUnit rateUnit = parseTimeUnit(context.getInitParameter(RATE_UNIT), TimeUnit.SECONDS);
        final TimeUnit durationUnit = parseTimeUnit(context.getInitParameter(DURATION_UNIT), TimeUnit.SECONDS);
        final boolean showSamples = Boolean.parseBoolean(context.getInitParameter(SHOW_SAMPLES));
        MetricFilter filter = (MetricFilter) context.getAttribute(METRIC_FILTER);
        if (filter == null) {
            filter = MetricFilter.ALL;
        }
        MetricsModule module = new MetricsModule(rateUnit, durationUnit, showSamples, filter);
        this.metricNormal = new MetricNormal(registry, module);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(CONTENT_TYPE);
        if (allowedOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        }
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setStatus(HttpServletResponse.SC_OK);

        try (OutputStream output = resp.getOutputStream()) {
            final boolean prettyPrint = Boolean.parseBoolean(req.getParameter("pretty"));
            if (jsonpParamName != null && req.getParameter(jsonpParamName) != null) {
                metricNormal.output(output, prettyPrint, req.getParameter(jsonpParamName));
            } else {
                metricNormal.output(output, prettyPrint);
            }
        }
    }

    private TimeUnit parseTimeUnit(String value, TimeUnit defaultValue) {
        try {
            return TimeUnit.valueOf(String.valueOf(value).toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
