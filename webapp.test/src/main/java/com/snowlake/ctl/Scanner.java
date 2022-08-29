package com.snowlake.ctl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@WebServlet
public class Scanner extends HttpServlet {

    private static final Map<String, Class<?>> mapping = new HashMap<>();

    public static synchronized void initMapping() {
        if (!mapping.isEmpty()) return;
        Class<SelfServlet> servletClass = SelfServlet.class;
        String url = getAnnotationValue(servletClass);
        if (url != null)
            mapping.put("/apps/ad" + url, servletClass);
    }

    public static HttpServlet getServlet(String url) throws IOException {
        Class<?> clzz = mapping.get(url);
        Objects.requireNonNull(clzz, url + "不识别");
        try {
            return (HttpServlet) clzz.newInstance();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static String getAnnotationValue(Class<?> clzz) {
        WebServlet ann = clzz.getAnnotation(WebServlet.class);
        if (ann == null) return null;
        return ann.urlPatterns()[0];
    }

    public static void doGet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doIs(servlet, req, resp, "doGet");
    }

    public static void doPost(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doIs(servlet, req, resp, "doPost");
    }

    private static void doIs(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp, String methodName) throws IOException {
        Method method = null;
        try {
            method = getServletMethod(servlet.getClass(), methodName);
            method.setAccessible(true);
            method.invoke(servlet, req, resp);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static Method getServletMethod(Class clzz, String methodName) throws NoSuchMethodException {
        try {
            return clzz.getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            Class parentClass = clzz.getSuperclass();
            if (parentClass == null) throw e;
            else return getServletMethod(parentClass, methodName);
        }
    }
}
