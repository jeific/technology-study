package com.starcor.biz.demo;

import com.broadtech.kpiserver.spi.QueryTask;
import com.broadtech.kpiserver.spi.utils.PropsUtil;
import com.broadtech.plugin.Module;
import com.broadtech.plugin.PluginFactory;
import com.google.common.collect.ImmutableMap;
import com.starcor.biz.demo.common.AppException;
import com.starcor.biz.demo.servlet.AppMainServlet;
import com.starcor.biz.demo.spi.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * biz接口平台的app demo<br>
 * biz的app从{@link com.broadtech.kpiserver.spi.context.Context}派生
 */
public class BizAppDemoContext implements com.broadtech.kpiserver.spi.context.Context {
    private static final Logger logger = LoggerFactory.getLogger(BizAppDemoContext.class);
    private Properties appConfig;
    private String workHome;
    private String appName;
    private PluginFactory<Plugin> pluginFactory;

    @Override
    public void init(Properties properties) {
        this.appConfig = properties;
        this.workHome = requireNonNull(appConfig.getProperty("app.home"), "app.home must not null");
        this.appName = requireNonNull(appConfig.getProperty("app.name"), "app.name must not null");
        int jettyPort = Integer.parseInt(appConfig.getProperty("serverPort"));
        appConfig.setProperty("master.url", "http://localhost:" + jettyPort + "/apps/" + appName);
        try {
            // 加载app 私有的配置
            appConfig.putAll(PropsUtil.loadProps(workHome + "/conf/demo-site.properties"));
        } catch (IOException e) {
            logger.error("初始化出现错误 读取配置文件失败:");
        }
        this.pluginFactory = loadPlugins(appConfig);
    }

    @Override
    public Map<String, Module<Plugin>> getModules() {
        return this.pluginFactory.getModulesMap();
    }

    @Override
    public Map<String, Class<? extends Servlet>> getSelvetMap() {
        ImmutableMap.Builder<String, Class<? extends Servlet>> builder = ImmutableMap.builder();
        getModules().values().forEach(modle -> {
            DemoLoadPlugin myPluginManger = (DemoLoadPlugin) modle.getUserModuleManger();
            builder.putAll(myPluginManger.getServletMap());
        });
        registerAppServlet(AppMainServlet.class, builder);
        return builder.build();
    }

    @Override
    public int getActionCount() {
        Map<String, Module<Plugin>> modulesMap = getModules();
        return modulesMap.values().stream().flatMapToInt(module ->
                IntStream.of(((DemoLoadPlugin) module.getUserModuleManger()).getActionCount())).sum();
    }

    @Override
    public Properties getConfig() {
        return appConfig;
    }

    @Override
    public String getName() {
        return appName;
    }

    @Override
    public String getWorkPath() {
        return workHome;
    }

    @Override
    public String getMaster() {
        return getConfig().getProperty("master.url");
    }

    @Override
    public <T extends QueryTask> JobBuilder<T> createJob(String taskName) {
        QueryTask task = getAction(taskName);
        if (task == null) {
            throw new AppException("接口action 不存在:" + taskName);
        }
        JobBuilder<T> jobBuilder = new JobBuilder<>();
        jobBuilder.format((T) task);
        return jobBuilder;
    }

    /**
     * 根据Action名获取{@link QueryTask}实现
     */
    private QueryTask getAction(String key) {
        QueryTask obj;
        Map<String, Module<Plugin>> modulesMap = getModules();
        for (Module<Plugin> moudle : modulesMap.values()) {
            DemoLoadPlugin myPluginManger = (DemoLoadPlugin) moudle.getUserModuleManger();
            obj = myPluginManger.getAction(key);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    /**
     * 加载插件
     */
    private PluginFactory<Plugin> loadPlugins(Properties config) {
        String home = config.getProperty("app.home");
        String pluginsDir = home + "/plugins";

        PluginFactory<Plugin> pluginFactory = PluginFactory.<Plugin>builder()
                .setModulesDir(pluginsDir)
                .setPlugin(Plugin.class) //moudle 入口描述接口
                .setDirMonitor(false) //进行监控
                .setConfiguration(config)
                .setMyPluginManger(DemoLoadPlugin.class) //用户自定义管理模块
                .build();

        Map<String, Module<Plugin>> modulesMap = pluginFactory.getModulesMap();  //加载所有插件 生成action配置

        logger.info("加载个数:{}", modulesMap.size());
        pluginFactory.getModulesMap().values().forEach(module -> logger.info(module.getLoadInfo()));
        return pluginFactory;
    }

    private void registerAppServlet(Class<? extends Servlet> servletClass
            , ImmutableMap.Builder<String, Class<? extends Servlet>> builder) {
        WebServlet webApp = servletClass.getAnnotation(WebServlet.class);
        for (String url : webApp.urlPatterns()) {
            builder.put(url, servletClass);
            java.net.URL classPath = servletClass.getResource(servletClass.getSimpleName() + ".class");
            logger.info("add WebAppService:[{}] with:{}", url, classPath);
        }
    }
}
