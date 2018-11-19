package com.starcor.biz.demo.common;

import com.broadtech.kpiserver.spi.exception.TaskAppException;

public class AppException
        extends TaskAppException {
    private static final long serialVersionUID = 1177322219627156317L;

    private AppErrCode errCode = AppErrCode.PARAMETER_ERROR;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable e) {
        super(message, e);
    }

    public AppException(Throwable e) {
        super(e);
    }

    public AppException(AppErrCode errCode, String message, Throwable e) {
        super(message, e);
        this.errCode = errCode;
    }

    public AppException(AppErrCode errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public AppErrCode getErrCode() {
        return errCode;
    }

    public static enum AppErrCode {
        CONNECTION_ERROR(1401002001),   //连接错误
        PARAMETER_ERROR(1401002002),    // 接口参数错误
        QUERY_ERROR(1401002003),           // sql等query 错误
        INTERNAL_ERROR(1401002004),     // 内部错误
        SYSTEM_ERROR(1401002005),    //系统错误
        UNKNOWN_ERROR(-1);  //未知错误 或者没有分类的错误

        private final int code;

        AppErrCode(int errCode) {
            this.code = errCode;
        }

        public int getErrCode() {
            return code;
        }

        public static AppErrCode fromInteger(int name) {
            for (AppErrCode v : AppErrCode.values()) {
                if (v.code == name) {
                    return v;
                }
            }
            // Throws IllegalArgumentException if name does not exact match with enum name
            throw new IllegalArgumentException("未知的错误码:" + name);
            //return ErrCode.valueOf(name);
        }

        public boolean equals(AppErrCode errCode) {
            return errCode.code == this.code;
        }

        @Override
        public String toString() {
            return super.toString() + ":" + this.code;
        }
    }
}
