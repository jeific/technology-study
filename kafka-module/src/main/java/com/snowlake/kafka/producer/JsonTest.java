package com.snowlake.kafka.producer;

import org.json.JSONObject;

public class JsonTest {

    public static void main(String[] args) {
        String jsonStr = "{\"session\":\"5b6ab5f4228f3776988ac0fa4e306051\"}";
        JSONObject object = new JSONObject(jsonStr);
        System.out.println(object.toMap());
    }
}
