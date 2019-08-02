
package com.walmart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.walmart.service.userInfo.UserInfoService;

import net.sf.json.JSONObject;

@RestController
public class UserInfoController {
	@Autowired
	private UserInfoService userService;

	@RequestMapping("/restIndex")
	public String restIndex(String jsonStr) {
		if(StringUtils.isEmpty(jsonStr)){
			return "参数错误";
		}else{
			JSONObject json = JSONObject.fromObject(jsonStr);
			String username = json.getString("username");
			if(StringUtils.isEmpty(username)){
				return "用户名为空";
			}
			String password = json.getString("password");
			if(StringUtils.isEmpty(password)){
				return "密码为空";
			}
			return null;//userService.createUser(username, password);
		}
	}

}
