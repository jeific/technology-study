package com.starcor.biz.demo.plugin.jdbc;

import com.starcor.biz.demo.spi.Application;
import com.starcor.biz.demo.spi.JdbcTask;

@Application(action = "get_user_visitor")
public class UserVisitor extends JdbcTask {
    @Override
    public String getName() {
        return "get_user_visitor";
    }

    @Override
    protected String getQueryStrings() {
        Params params = getParameters();
        return "select user_id,pv,uv from " + params.getTable() + " where user_id=" + params.getUserId()
                + " day>=" + params.get("day");
    }
}
