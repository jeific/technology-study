package com.broadtech.embed.jetty.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * #########################################################<br>
 * Handler Collections and Wrappers<br>
 * <br>
 * Complex request handling is typically built from multiple Handlers that you can combine in various ways. Jetty has several implementations of the HandlerContainer interface:<br>
 * <br>
 * HandlerCollection<br>
 * Holds a collection of other handlers and calls each handler in order. This is useful for combining statistics and logging handlers with the handler that generates the response.<br>
 * HandlerList<br>
 * A Handler Collection that calls each handler in turn until either an exception is thrown, the response is committed or the request.isHandled() returns true. You can use it to combine handlers that conditionally handle a request, such as calling multiple contexts until one matches a virtual host.<br>
 * HandlerWrapper<br>
 * A Handler base class that you can use to daisy chain handlers together in the style of aspect-oriented programming. For example, a standard web application is implemented by a chain of a context, session, security and servlet handlers.<br>
 * ContextHandlerCollection<br>
 * A specialized HandlerCollection that uses the longest prefix of the request URI (the contextPath) to select a contained ContextHandler to handle the request.<br>
 * <br>
 * Scoped Handlers<br>
 * <br>
 * Much of the standard Servlet container in Jetty is implemented with HandlerWrappers that daisy chain handlers together: ContextHandler to SessionHandler to SecurityHandler to ServletHandler. However, because of the nature of the servlet specification, this chaining cannot be a pure nesting of handlers as the outer handlers sometimes need information that the inner handlers process. For example, when a ContextHandler calls some application listeners to inform them of a request entering the context, it must already know which servlet the ServletHandler will dispatch the request to so that the servletPath method returns the correct value.<br>
 * <br>
 * The HandlerWrapper is specialized to the ScopedHandler abstract class, which supports a daisy chain of scopes. For example if a ServletHandler is nested within a ContextHandler, the order and nesting of execution of methods is:<br>
 * <br>
 * Server.handle(...)<br>
 * ContextHandler.doScope(...)<br>
 * ServletHandler.doScope(...)<br>
 * ContextHandler.doHandle(...)<br>
 * ServletHandler.doHandle(...)<br>
 * SomeServlet.service(...)<br>
 * <br>
 * Thus when the ContextHandler handles the request, it does so within the scope the ServletHandler has established.<br>
 * #########################################################
 */
public class HelloHandler extends AbstractHandler {
    private final String greeting;
    private final String body;

    public HelloHandler() {
        this("Hello World");
    }

    public HelloHandler(String greeting) {
        this(greeting, null);
    }

    public HelloHandler(String greeting, String body) {
        this.greeting = greeting;
        this.body = body;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
        System.out.println("target=" + target);
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

        out.println("<h1>" + greeting + "</h1>");
        if (body != null) {
            out.println(body);
        }

        baseRequest.setHandled(true);
    }
}