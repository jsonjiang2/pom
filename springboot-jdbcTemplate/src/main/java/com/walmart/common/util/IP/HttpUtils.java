package com.walmart.common.util.IP;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
/*
 <dependency>
		 <groupId>commons-httpclient</groupId>
		 <artifactId>commons-httpclient</artifactId>
		 <version>3.1</version>
		 </dependency>*/
public class HttpUtils
{
	public static final int MAX_CONNECTION_TIME = 1000 * 60 * 30;
	
	private static final Logger logger = Logger.getLogger(HttpUtils.class);

/**	
 * 获取请求对象的URL地址	
 * @param request	 
 *  请求对象	
 * @return String URL地址（http://www.yl-blog.com）	 
*/
	
public static String getRequestURL(HttpServletRequest request) 
{	
	StringBuilder url = new StringBuilder();	
	String scheme = request.getScheme();	
	String contextPath = request.getContextPath();
	int port = request.getServerPort();
	url.append(scheme); 
	// http, https
	url.append("://");	
	url.append(request.getServerName());
		
	if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {	
		url.append(':');			
		url.append(request.getServerPort());		
	}
	url.append(contextPath);
	return url.toString();
}

	
/**	
 * 检查是否有cookieName	
 * 
 * @param request
 * @param cookieName
 * @return
 */
	
public static boolean checkCookie(HttpServletRequest request,String cookieName) 
{	
	Cookie cookie;		
	Cookie[] cookies = request.getCookies();	
	if (cookies != null) {		
		for (int i = 0; i < cookies.length; i++) {			
			cookie = cookies[i];			
			if (cookie.getName().equals(cookieName)) {				
				return true;				
		}			
	}		
}
	return false;
	}

	
/**	
 * 获取cookie
 * 	
 * @param request	 
 * @param cookieName	
 * @return	
 */
	
public static String getCookie(HttpServletRequest request, String cookieName) 
{		
	Cookie cookie;		
	Cookie[] cookies = request.getCookies();		
	if (cookies != null) {			
	for (int i = 0; i < cookies.length; i++) {				
	cookie = cookies[i];				
	if (cookie.getName().equals(cookieName)) {					
	return cookie.getValue();			
		}			
	}		
}		
	return "";	
}

	
/**	 
 * 获取语言信息	
 * 	
 * 1. 优先获取URL重写参数
 * 2. 获取Http请求浏览器语言标识	
 * 	 
 * @param request	
 * @return	
 */
	
public static String getLanguage(HttpServletRequest request) 
{
	String lang = request.getParameter("lang");		
	if (lang != null && !lang.equals("")) {			
		return lang;		
	}		
	Locale locale = request.getLocale();	
	return locale.getLanguage() + "-" + locale.getCountry();	
}

	
/** 
 * 获取请求IP	
 * 	
 * @param request	
 * @return	
 */
	
public static String getIntranetIp(HttpServletRequest request) 
{		
	String ip = request.getHeader("x-forwarded-for");		
	if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {			
		ip = request.getHeader("Proxy-Client-IP");		
	}
		
	if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {			
		ip = request.getHeader("WL-Proxy-Client-IP");		
	}
		
	if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {			
		ip = request.getHeader("X-Real-IP");		
	}
		
	if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {			
		ip = request.getRemoteAddr();		
	}		
	return ip;	
}
	
	
/**	
 * @Name getMACAddress	
 * @Description 获取MAC地址	
 * @Author sheng	 
 * @Version V1.00 
 * @CreateDate 2016-11-14 上午10:41:46	
 * @param ip	 
 * @return	
 */	
 public static String getMACAddress(String ip) 
 {  	       
	 String str = "";         
	 String macAddress = ""; 
	 
	 try {             
		 Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);            
		 InputStreamReader ir = new InputStreamReader(p.getInputStream());  	           
		 LineNumberReader input = new LineNumberReader(ir);  
	           
		 for (int i = 1; i < 100; i++) {                 
			 str = input.readLine();                 
			 if (str != null) {                     
				 if (str.indexOf("MAC Address") > 1) {                        
					 macAddress = str.substring(  str.indexOf("MAC Address") + 14, str.length());  	                       
					 break;  	                   
				 	}  	               
			  }  	           
		}        
	 	} catch (IOException e) {  e.printStackTrace(System.out);         
	 	}  	        
	 return macAddress;    
 }  
	 
	
/** 
 * @Name: getIpInfo	 
 * @Description: 根据Ip，获取Ip城市，运营商	
 * @Author: sheng	 
 * @Version: V1.00	
 * @Create Date: 2016-11-14
 * @Parameters: ip	
 * @Return: 返回符合条件的节点	
 */
	
public static String getIpInfo(String ip) 
{		
	try {			
		String body = readUrlData("http://ip.taobao.com/service/getIpInfo.php?ip="+ ip);
		
		JSONObject obj = (JSONObject) JSONObject.fromObject(body).get("data");			
		String city = obj.getString("city");			
		return ip.concat(" ").concat(city);		
	} catch (MalformedURLException e) {
		logger.error(e);		
	} catch (IOException e) {			
		logger.error(e);		
	}		
	return ip.concat(" 未知地区");
}

	
public static String getIpCity(String ip) 
{	
	String city = null;		
	try {			
		String body = readUrlData("http://ip.taobao.com/service/getIpInfo.php?ip="+ ip);
			
		JSONObject obj = (JSONObject) JSONObject.fromObject(body).get("data");			
		city = obj.getString("city");		
	} catch (Exception e) {
		logger.error(e);		
	}		
	return StringUtils.isEmpty(city) ? "北京市" : city;
}

	
/**	
 * @Name: getIpInfo	 
 * @Description: 根据Ip，获取Ip城市，运营商	
 * @Author: sheng	
 * @Version: V1.00	 
 * @Create Date: 2016-11-14
 * @Parameters: ip	 
 * @Return: 返回符合条件的节点	 
*/
	
public static String getIpInfo(HttpServletRequest request)
{		
	String ip = getIntranetIp(request);		
	return getIpInfo(ip);	
}

	
/**	
 * 读取远程URL中数据	 
 * 	 
 * @param requestAddress	
 * @return	 
 * @throws IOException	
 * @throws HttpException	
 * @throws RuntimeException	
 */
	
public static String readUrlData(String requestAddress) throws HttpException, IOException
{		
	HttpClient client = new HttpClient();			
	GetMethod getMethod = new GetMethod(requestAddress);		
	client.executeMethod(getMethod);		
	client.setTimeout(2000);	
	client.setConnectionTimeout(2000);
	return getMethod.getResponseBodyAsString();	
}

	
/**	
 * @Name ipExistsInRange	
 * @Description 验证IP是否属于某个IP段	 
 * @Author sheng
 * @Version V1.00	
 * @CreateDate 2016年11月14日 
 * @param ip  "10.11.10.116"	
 * @param ipSection
 * 示例 "10.10.1.00-10.255.255.255"
 * @return	
 */
	
public static boolean ipExistsInRange(String ip, String ipSection)
{		
	ipSection = ipSection.trim();		
	ip = ip.trim();		
	int idx = ipSection.indexOf('-');	
	String beginIP = ipSection.substring(0, idx);		
	String endIP = ipSection.substring(idx + 1);		
	return getIp2long(beginIP) <= getIp2long(ip) && getIp2long(ip) <= getIp2long(endIP);	
}

	
public static long getIp2long(String ip) 
{		
	ip = ip.trim();		
	String[] ips = ip.split("\\.");		
	long ip2long = 0L;		
	for (int i = 0; i < 4; ++i) {
		ip2long = ip2long << 8 | Integer.parseInt(ips[i]);		
	}		
	return ip2long;
}

	
public static long getIp2long2(String ip) 
{		
	ip = ip.trim();		
	String[] ips = ip.split("\\.");		
	long ip1 = Integer.parseInt(ips[0]);		
	long ip2 = Integer.parseInt(ips[1]);	
	long ip3 = Integer.parseInt(ips[2]);		
	long ip4 = Integer.parseInt(ips[3]);		
	long ip2long = 1L * ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256+ ip4;		
	return ip2long;
	}
}
