package com.starcor.biz.demo.plugin.jdbc;

import com.broadtech.kpiserver.spi.QueryTask;
import com.broadtech.kpiserver.spi.WebAppService;
import com.google.common.collect.ImmutableSet;
import com.starcor.biz.demo.spi.Plugin;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

public class Mysql implements Plugin {
    @Override
    public Collection<Class<? extends WebAppService>> getWebServlets() {

        return ImmutableSet.<Class<? extends WebAppService>>builder()
                .add(JdbcWebService.class)
                .build();
    }

    @Override
    public Set<Class<? extends QueryTask>> getActions() {

        return ImmutableSet.<Class<? extends QueryTask>>builder()
                .add(UserVisitor.class)
                .build();
    }

    @Override
    public void init(String pluginPath, Properties config) {

    }
}
