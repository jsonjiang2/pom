package com.walmart.model;

import com.walmart.common.util.annotation.CustomTableName;

@CustomTableName("userInfo")
public class UserInfo {
	
	
	public String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	

}
