package com.jiankunking.logsearch.model.external.cmdb;

import java.util.List;

public class ListObject {
    private String meta;
    private List<Project> items;
    private Object selectionPredicate;

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public List<Project> getItems() {
        return items;
    }

    public void setItems(List<Project> items) {
        this.items = items;
    }

    public Object getSelectionPredicate() {
        return selectionPredicate;
    }

    public void setSelectionPredicate(Object selectionPredicate) {
        this.selectionPredicate = selectionPredicate;
    }
}
