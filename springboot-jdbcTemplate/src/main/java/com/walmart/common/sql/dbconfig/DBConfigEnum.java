package com.walmart.common.sql.dbconfig;

public enum DBConfigEnum {
    YSDB("ysdb","养生项目DB"),
    ADMIN("admin","管理后台DB"),

    ;


    DBConfigEnum(String dbname, String desc) {
        this.dbname = dbname;
        this.desc = desc;
    }

    private String dbname;
    private String desc;

    public String getDbname() {
        return dbname;
    }

    public String getDesc() {
        return desc;
    }
}
