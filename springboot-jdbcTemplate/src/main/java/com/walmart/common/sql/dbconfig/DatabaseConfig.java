package com.walmart.common.sql.dbconfig;

/**
 * DB数据库配置信息
 */
public class DatabaseConfig {
	private String databaseKey;
	private String host;
	private String dbName;
	private String dbUser;
	private String dbPwd;
	private boolean rewriteBatchedStatements;
	private boolean isOk;

	public DatabaseConfig() {
	}

	public DatabaseConfig(String databaseKey, String host,String dbName,
						  String dbUser, String dbPwd, boolean rewriteBatchedStatements, boolean isOk) {
		this.databaseKey = databaseKey;
		this.host = host;
		this.dbUser = dbUser;
		this.dbName = dbName;
		this.dbPwd = dbPwd;
		this.rewriteBatchedStatements = rewriteBatchedStatements;
		this.isOk = isOk;
	}

	public String getDatabaseKey() {
		return databaseKey;
	}

	public void setDatabaseKey(String databaseKey) {
		this.databaseKey = databaseKey;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPwd() {
		return dbPwd;
	}

	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}

	public boolean getRewriteBatchedStatements() {
		return rewriteBatchedStatements;
	}

	public void setRewriteBatchedStatements(boolean rewriteBatchedStatements) {
		this.rewriteBatchedStatements = rewriteBatchedStatements;
	}

	public boolean isOk() {
		return isOk;
	}

	public void setOk(boolean ok) {
		isOk = ok;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
}
