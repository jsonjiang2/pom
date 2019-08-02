package com.walmart.common.sql.bean;



import java.io.Serializable;

import com.walmart.common.config.ConfigResolve;
import com.walmart.common.config.DefaultDataBaseKey;

public class OneSql implements Serializable {

	private static final long serialVersionUID = 5848774308880396497L;

	public OneSql(String sql, int rows) {
		super();
		this.sql = sql;
		this.rows = rows;
	}

	public OneSql(String sql, int rows, Object[] params) {
		this(sql, rows);
		this.params = params;
	}

	public OneSql(String sql, int rows, Object[] params, String database) {
		this(sql, rows, params);
		this.database = database;
	}

	private String sql; // 涉及的sql语句
	private int rows; // sql语句必须影响的行数，-1表示大于0即可 -2表示无限制
	private Object[] params; // sql语句参数
	private String database; // 所在的数据库

	public String getDatabase() {
		return database;
	}

	public String getDatabaseKey() {
		if(database == null) {
			return ConfigResolve.getDbJsonObjectConfig().getString(DefaultDataBaseKey.DEFAULTDATABASEKEY);
		}
		return database;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public boolean next(int efRows) {
		if (efRows >= 0) {
			if (rows == -2) { // 无限制
				return true;
			} else if (rows == -1) { // 大于0即可
				if (efRows > 0) {
					return true;
				}
			} else if (rows == efRows) { // 需求影响行数
				return true;
			}
		}
		return false;
	}
}
