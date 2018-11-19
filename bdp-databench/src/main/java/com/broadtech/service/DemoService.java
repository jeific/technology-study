package com.broadtech.service;

import org.springframework.stereotype.Service;

@Service
public class DemoService {

    public String guide() {
        return "DemoService out message: Hello World";
    }

}
