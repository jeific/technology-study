package com.starcor.biz.demo.plugin.jdbc;

import com.starcor.biz.demo.servlet.AppMainServlet;

import javax.servlet.annotation.WebServlet;

/**
 * 修改请求地址，将/jdbc转入本实现处理
 */
@WebServlet(urlPatterns = "/jdbc", asyncSupported = true)
public class JdbcWebService extends AppMainServlet {

}
