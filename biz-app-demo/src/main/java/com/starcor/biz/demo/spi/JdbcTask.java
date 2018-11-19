package com.starcor.biz.demo.spi;

import com.broadtech.kpiserver.spi.QueryData;
import com.broadtech.kpiserver.spi.QueryTask;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC action的抽象实现
 */
public abstract class JdbcTask implements QueryTask {
    private Params params;

    @Override
    public void setQuery(Map<String, Object> map) {
        String table = (String) map.get("table");
        String platformId = (String) map.get("platformId");
        String numParam = (String) map.get("num");
        int num = numParam == null ? 0 : Integer.parseInt(numParam);
        String userId = (String) map.get("userId");
        this.params = new Params(map, table, platformId, num, userId);
    }

    @Override
    public Optional<QueryData> loadData() throws Exception {
        String sql = getQueryStrings();
        QueryData queryData = QueryData.builder()
                .setDataArr(query(sql))
                .setDataObj(getQueryOutJson())
                .build();  //TODO 建议在大数据量的计算业务中不提供total功能
        return Optional.of(queryData);
    }

    private JSONArray query(String sql) {
        // TODO 从数据库查询数据, 查询时通用逻辑，应该在App的实现中提供工具来处理
        // 此处直接模拟数据输出
        JSONObject data = new JSONObject();
        data.put("user", "user1");
        data.put("pv", 100);
        data.put("uv", 26);
        JSONArray records = new JSONArray();
        records.put(data);
        return records;
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

    protected Params getParameters() {
        return params;
    }

    /**
     * 查询数据SQL描述
     */
    protected abstract String getQueryStrings();

    public class Params {
        private final Map<String, Object> params;
        private final String table;
        private final String platformId;
        private final int num;
        private final String userId;

        public Params(Map<String, Object> params, String table, String platformId, int num, String userId) {
            this.params = params;
            this.table = table;
            this.platformId = platformId;
            this.num = num;
            this.userId = userId;
        }

        public Object get(String parameterName) {
            return params.get(parameterName);
        }

        public String getTable() {
            return table;
        }

        public String getPlatformId() {
            return platformId;
        }

        public int getNum() {
            return num;
        }

        public String getUserId() {
            return userId;
        }
    }
}
