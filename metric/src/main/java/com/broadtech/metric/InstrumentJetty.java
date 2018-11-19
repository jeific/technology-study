package com.broadtech.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class InstrumentJetty {
    private static final MetricRegistry registry = new MetricRegistry();

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        try (ServerConnector connector =
                     new ServerConnector(server, new InstrumentedConnectionFactory(new HttpConnectionFactory(),
                             registry.timer("http.connections"),
                             registry.counter("http.active-connections")))) {
            InstrumentedHandler handler = new InstrumentedHandler(registry);
            handler.setName("my_handler");
            handler.setHandler(new TestHandler());
            server.setHandler(handler);
            server.addConnector(connector);
            server.start();
            System.out.println("connector: http://localhost:" + connector.getLocalPort());

            ConsoleReporter.forRegistry(registry).build().start(5, TimeUnit.SECONDS);

            Histogram histogram = registry.histogram(MetricRegistry.name(InstrumentJetty.class, "histogram"));
            Random random = new Random();
            for (int i = 0; i < 120; i++) {
                histogram.update(random.nextInt(100));
                Thread.sleep(1000);
            }
        } finally {
            server.stop();
        }
    }

    private static AbstractHandler defaultHandler() {
        return new AbstractHandler() {
            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                try (PrintWriter writer = response.getWriter()) {
                    writer.println("OK");
                }
            }
        };
    }

    /**
     * test handler.
     * <p>
     * Supports
     * <p>
     * /blocking - uses the standard servlet api
     * /async - uses the 3.1 async api to complete the request
     * <p>
     * all other requests will return 404
     */
    private static class TestHandler extends AbstractHandler {
        @Override
        public void handle(
                String path,
                Request request,
                final HttpServletRequest httpServletRequest,
                final HttpServletResponse httpServletResponse
        ) throws IOException, ServletException {
            switch (path) {
                case "/blocking":
                    request.setHandled(true);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    httpServletResponse.setStatus(200);
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().write("some content from the blocking request\n");
                    break;
                case "/async":
                    request.setHandled(true);
                    final AsyncContext context = request.startAsync();
                    Thread t = new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        httpServletResponse.setStatus(200);
                        httpServletResponse.setContentType("text/plain");
                        final ServletOutputStream servletOutputStream;
                        try {
                            servletOutputStream = httpServletResponse.getOutputStream();
                            servletOutputStream.setWriteListener(
                                    new WriteListener() {
                                        @Override
                                        public void onWritePossible() throws IOException {
                                            servletOutputStream.write("some content from the async\n"
                                                    .getBytes(StandardCharsets.UTF_8));
                                            context.complete();
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            context.complete();
                                        }
                                    }
                            );
                        } catch (IOException e) {
                            context.complete();
                        }
                    });
                    t.start();
                    break;
                default:
                    break;
            }
        }
    }
}
