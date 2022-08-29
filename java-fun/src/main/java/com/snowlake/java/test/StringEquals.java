package com.snowlake.java.test;


import org.json.JSONArray;
import org.json.JSONObject;

public class StringEquals {

    public static void main(String[] args) {
//        String str1 = "create view allplatform.nns_address_code as select nns_address_code.nns_code_orig nns_address_code.nns_name nns_address_code.nns_code nns_address_code.sp_id nns_address_code.ext_id nns_address_code.day'starcor' as platform_id from starcor.nns_address_code";
//        String str2 = "create view  allplatform.nns_address_code as select nns_address_code.nns_code_orig nns_address_code.nns_name nns_address_code.nns_code nns_address_code.sp_id nns_address_code.ext_id nns_address_code.day'starcor' as platform_id from starcor.nns_address_code";
//        boolean rs = str1.equals(str2);
//        System.out.println(rs);
        System.out.println(new JSONObject()); // {}
        System.out.println(new JSONArray());  // []
        System.out.println(new JSONObject("[]")); // 词句代码异常
    }
}
