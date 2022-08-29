package com.snowlake.java.path;

public class Record {
    private final String userId;
    private final String spId;
    private final String clientCatalog;
    private final String source;
    private final String target;

    private Record(String userId, String spId, String source, String target, String clientCatalog) {
        this.userId = userId;
        this.spId = spId;
        this.source = source;
        this.target = target;
        this.clientCatalog = clientCatalog;
    }

    public static Record of(String userId, String spId, String source, String target, String clientCatalog) {
        return new Record(userId, spId, source, target, clientCatalog);
    }

    public String getUserId() {
        return userId;
    }

    public String getClientCatalog() {
        return clientCatalog;
    }

    String getSpId() {
        return spId;
    }

    String getSource() {
        return source;
    }

    String getTarget() {
        return target;
    }
}
