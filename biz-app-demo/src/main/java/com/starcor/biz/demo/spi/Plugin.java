package com.starcor.biz.demo.spi;

import com.broadtech.kpiserver.spi.QueryTask;
import com.broadtech.kpiserver.spi.WebAppService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * 插件能力：
 * 1. 管理一批请求入口，通过{@link WebAppService}
 * 2. 管理一系列action，由指定的{@link WebAppService}实现转发
 */
public interface Plugin {
    Log log = LogFactory.getLog(Plugin.class);

    /**
     * 插件定义的{@link WebAppService},这些服务具有转发请求的能力，并具有独自的访问入口<br>
     * {@link WebAppService}实现类需要通过语法:  @WebServlet(urlPatterns = "/query", asyncSupported = true)
     * 定义访问路径,用于将该路径下的请求转入到该WebAppService处理
     */
    default Collection<Class<? extends WebAppService>> getWebServlets() {
        return Collections.emptyList();
    }

    /**
     * 获取插件的定义action列表
     */
    Set<Class<? extends QueryTask>> getActions();

    /**
     * 插件初始化
     */
    void init(String pluginPath, Properties config);

    /**
     * 插件卸载,实现插件卸载后的清理等功能
     */
    default void close() {
    }
}
