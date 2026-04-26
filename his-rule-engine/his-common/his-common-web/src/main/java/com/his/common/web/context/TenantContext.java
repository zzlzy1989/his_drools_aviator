package com.his.common.web.context;

/**
 * 租户上下文持有者
 *
 * <p>通过 ThreadLocal 存储当前请求的租户信息</p>
 */
public class TenantContext {

    /**
     * ThreadLocal 持有者
     */
    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static String getTenantId() {
        return TENANT_HOLDER.get();
    }

    /**
     * 获取当前租户ID，如果为空则返回默认值
     *
     * @param defaultValue 默认值
     * @return 租户ID
     */
    public static String getTenantId(String defaultValue) {
        String tenantId = getTenantId();
        return tenantId != null ? tenantId : defaultValue;
    }

    /**
     * 清除租户上下文
     * <p>在请求结束时必须调用</p>
     */
    public static void clear() {
        TENANT_HOLDER.remove();
    }

    /**
     * 判断是否设置了租户
     *
     * @return true表示已设置租户
     */
    public static boolean hasTenant() {
        return getTenantId() != null;
    }
}
