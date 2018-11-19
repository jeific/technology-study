package com.starcor.biz.demo.plugin.hello;

import com.broadtech.kpiserver.spi.QueryData;
import com.broadtech.kpiserver.spi.QueryTask;
import com.starcor.biz.demo.spi.Application;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 注册action的名字
@Application(action = "get_wellcome")
public class GetWellCome implements QueryTask {
    private Map<String, Object> params;

    @Override
    public String getName() {
        return "get_wellcome";
    }

    @Override
    public Optional<QueryData> loadData() throws Exception {
        JSONArray data = new JSONArray();
        JSONObject row = new JSONObject();
        row.put("wellcome", "Hello world");
        row.put("receive.params", params);
        data.put(row);
        QueryData queryData = QueryData.builder()
                .setDataArr(data)
                .setDataObj(getQueryOutJson())
                .build();
        return Optional.of(queryData);
    }

    /**
     * 接收输入参数
     */
    @Override
    public void setQuery(Map<String, Object> map) {
        this.params = map;
    }

    @Override
    public int[] getPage() {
        return new int[]{0, 0}; // 给予默认实现，必须实现
    }

    /**
     * 接口自定义数据结构，非标准的返回数据结构的部分
     */
    @Override
    public Map<String, Object> getQueryOutJson() {
        return new HashMap<>();
    }
}
