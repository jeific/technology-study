package com.starcor.biz.demo.plugin.hello;

import com.broadtech.kpiserver.spi.QueryTask;
import com.google.common.collect.ImmutableSet;
import com.starcor.biz.demo.spi.Plugin;

import java.util.Properties;
import java.util.Set;

public class Hello implements Plugin {
    @Override
    public Set<Class<? extends QueryTask>> getActions() {
        return ImmutableSet.<Class<? extends QueryTask>>builder()
                .add(GetWellCome.class)
                .build();
    }

    /**
     * 插件初始化<br>
     * 该接口仅插件load时候加载一次，可用于进行环境初始化等操作
     */
    @Override
    public void init(String pluginPath, Properties config) {

    }

    /**
     * 插件卸载时候调用<br>
     * 不建议使用插件卸载功能,该功能因jvm的设计不能有效实现资源的清理和冲突处理<br>
     * 涉及插件删除请重启平台
     */
    @Override
    public void close() {

    }
}
