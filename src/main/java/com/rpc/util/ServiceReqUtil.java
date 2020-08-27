package com.rpc.util;

import java.io.Serializable;
import java.util.Arrays;

public class ServiceReqUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    // 服务名
    private String serverName;

    //方法名
    private String methodName;

    // 参数类型
    private Class<?>[] parameterTypes;

    // 参数值
    private Object[] values;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "ServiceReqUtil{" +
                "serverName='" + serverName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
