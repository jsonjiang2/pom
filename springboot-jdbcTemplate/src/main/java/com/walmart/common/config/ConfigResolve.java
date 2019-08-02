package com.walmart.common.config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ConfigResolve {

    private static final Logger logger = LoggerFactory.getLogger(ConfigResolve.class);
    private static final String DBNAME = "/dbmysql.json";
    private static JSONObject dbJsonObject;
  public static synchronized JSONObject getDbJsonObjectConfig(){
      if(dbJsonObject == null){
          dbJsonObject = resolveConfig(DBNAME);
      }
      return dbJsonObject;
  }


    public static  JSONObject resolveConfig(String fileName) {
        JSONObject jsonObject = new JSONObject();
        InputStream inputStream = null;
        BufferedReader br = null;
        try{
            inputStream = ConfigResolve.class.getResourceAsStream(fileName);
            br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null){
                String str = line.trim();
                if(!str.startsWith("//")  && !str.startsWith("##") ){
                    sb.append(str);
                }
            }
            if(sb.length() > 0){
                String sbToString = sb.toString();
                jsonObject = JSON.parseObject(sbToString);
               logger.info("配置文件读取成功,{}",sbToString);
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.info("配置文件读取失败,{}",e);
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }
}
