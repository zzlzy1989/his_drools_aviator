package com.his.common.web.context;

/**
 * 用户上下文持有者
 *
 * <p>通过 ThreadLocal 存储当前请求的用户信息</p>
 */
public class UserContext {

    private static final ThreadLocal<String> USER_HOLDER = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_HOLDER.get();
    }

    public static String getUserId(String defaultValue) {
        String userId = getUserId();
        return userId != null ? userId : defaultValue;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    public static boolean hasUser() {
        return getUserId() != null;
    }
}