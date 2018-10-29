package com.jiankunking.logsearch.model;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public enum OSTypeEnum {
    /**
     * windows 操作系统
     */
    WINDOWS("WINDOWS", 0),
    /**
     * linux 操作系统
     */
    LINUX("LINUX", 1);

    private String name;
    private int index;

    OSTypeEnum(String name, int index) {
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
