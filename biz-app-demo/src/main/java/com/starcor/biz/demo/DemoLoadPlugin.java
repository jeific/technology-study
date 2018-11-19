package com.starcor.biz.demo;

import com.broadtech.kpiserver.spi.QueryTask;
import com.broadtech.kpiserver.spi.exception.TaskAppException;
import com.broadtech.plugin.UserModuleManager;
import com.starcor.biz.demo.spi.Application;
import com.starcor.biz.demo.spi.Plugin;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DemoLoadPlugin implements UserModuleManager<Plugin> {
    private static final Logger logger = LoggerFactory.getLogger(DemoLoadPlugin.class);
    private final Map<String, Class<? extends Servlet>> servletMap = new HashMap<>();
    private final Map<String, Class<? extends QueryTask>> actionsMap = new ConcurrentHashMap<>();
    private Properties appConfig;
    private boolean active = true;  //moudle 插件状态

    @Override
    public void setConfig(Properties properties) {
        this.appConfig = properties;
    }

    @Override
    public void userDelete(List<Plugin> plugins) {
        //插件卸载 释放资源 主要释放连接池啊 线程池啊等等
        plugins.forEach(Plugin::close);
        actionsMap.clear();
    }

    @Override
    public void install(List<Plugin> list, String path) {
        list.forEach(x -> install(x, path));
    }

    /**
     * 插件安装
     */
    private void install(Plugin plugin, String modulePath) {
        plugin.getWebServlets().forEach(servlet -> { // 安装插件实现的WebAppService
            WebServlet webApp = servlet.getAnnotation(WebServlet.class);
            for (String url : webApp.urlPatterns()) {
                servletMap.put(url, servlet);
                java.net.URL classPath = servlet.getResource(servlet.getSimpleName() + ".class");
                logger.info("add WebAppService:[{}] with:{}", url, classPath);
            }
        });
        plugin.getActions().forEach(action -> { // 安装插件实现的Action
            Application app = action.getAnnotation(Application.class);//获取类注解
            String actionName;
            if (app == null) {
                actionName = action.getSimpleName();
                logger.warn("{}缺少接口注释,默认为:{}", action.getName(), action.getSimpleName());
            } else {
                actionName = app.action().trim();
            }
            String[] actions = actionName.split(",");
            Arrays.stream(actions).forEach(name -> this.actionsMap.put(name, action)); //action 安装
        });
        try {
            appConfig.setProperty("moduleHome", modulePath);
            plugin.init(modulePath, appConfig);
        } catch (Exception e) {
            logger.error("moudle{} 初始化失败", modulePath, e);
        }
    }

    @Override
    public String getLoadInfo() {
        JSONObject object = new JSONObject()
                .put("action.size", getActionCount())
                .put("actions", actionsMap.keySet());
        return object.toString();
    }

    int getActionCount() {
        return actionsMap.size();
    }

    QueryTask getAction(String key) {
        if (!active) {
            return null;
        }
        if (actionsMap.containsKey(key)) {
            try {
                return actionsMap.get(key).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new TaskAppException("接口初始化失败" + key, e);
            }
        } else {
            return null;
        }
    }

    Map<String, Class<? extends Servlet>> getServletMap() {
        return servletMap;
    }

    @Override
    public void start() {
        this.active = true;
    }

    @Override
    public void stop() {
        this.active = false;
    }
}
