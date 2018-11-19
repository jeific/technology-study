package com.broadtech.controller;

import com.broadtech.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class DemoController {
    @Autowired
    private DemoService demoService;

    @RequestMapping("/well")
    public void welcome(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write(demoService.guide());
    }

    @RequestMapping("/jsp")
    public String first() {
        return "first";
    }
}
