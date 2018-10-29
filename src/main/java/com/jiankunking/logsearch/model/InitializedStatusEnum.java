package com.jiankunking.logsearch.model;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public enum InitializedStatusEnum {
    /**
     * agent 安装状态：未初始化
     */
    uninitialized("未初始化", 0),
    /**
     * agent 安装状态：失败
     */
    fail("失败", 1),
    /**
     * agent 安装状态：成功
     */
    success("成功", 2),
    /**
     * agent 安装状态：不支持
     */
    invalid("不支持", Integer.MAX_VALUE);

    private String name;
    private int index;

    InitializedStatusEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
