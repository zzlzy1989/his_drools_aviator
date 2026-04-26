package com.his.common.web.result;

import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * 分页响应封装
 *
 * @param <T> 数据类型
 */
@Data
@Builder
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否还有下一页
     */
    private Boolean hasNext;

    /**
     * 创建分页响应
     *
     * @param list  数据列表
     * @param total 总记录数
     * @param page  当前页码
     * @param pageSize 每页记录数
     * @param <T>   数据类型
     * @return 分页响应
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        boolean hasNext = page < totalPages;

        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 创建空分页响应
     *
     * @param page     当前页码
     * @param pageSize 每页记录数
     * @param <T>     数据类型
     * @return 空分页响应
     */
    public static <T> PageResult<T> empty(Integer page, Integer pageSize) {
        return of(List.of(), 0L, page, pageSize);
    }
}
