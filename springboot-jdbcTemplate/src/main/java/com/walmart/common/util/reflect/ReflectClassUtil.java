package com.walmart.common.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射操作工具类
 */
public class ReflectClassUtil {

	public static Object getValByField(Class entityClazz , String field , Object o){
		Object result = null;
		Method[] mss = entityClazz.getMethods();
    	for(Method m : mss){
    		if(m.getName().equalsIgnoreCase("get"+field)){
    			try {
					result = m.invoke(o, new Object[]{});
					break;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
    		}
    	}
    	return result;
	}
	
	public static Object setValByField(Class entityClazz , String field , Object o , Object newVal){
		Object result = null;
		Method[] mss = entityClazz.getMethods();
    	for(Method m : mss){
    		if(m.getName().equalsIgnoreCase("set"+field)){
    			try {
					result = m.invoke(o, new Object[]{newVal});
					break;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
    		}
    	}
    	return result;
	}
	
	public static Object invokeByClassAndMethod(Class<?> entityClazz , String method , Object[] params){
		Object result = null;
		try {
			Object o = entityClazz.newInstance();
			Method[] mss = entityClazz.getMethods();
	    	for(Method m : mss){
	    		if(m.getName().equalsIgnoreCase(method)){
	    			try {
						result = m.invoke(o, params);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
	    		}
	    	}
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
    	return result;
	}
	
	public static BigDecimal getDecimalValByField(Class entityClazz , String field , Object o){
		return getBigDecimal(getValByField(entityClazz, field, o));
	}
	public static BigDecimal getBigDecimal(Object param) {
		BigDecimal bd = null;
		if (param instanceof Integer) {
			int value = ((Integer) param).intValue();
			bd = new BigDecimal(value);
		} else if (param instanceof Double) {
			double d = ((Double) param).doubleValue();
			bd = new BigDecimal(d);
		} else if (param instanceof Float) {
			float f = ((Float) param).floatValue();
			bd = new BigDecimal(f);
		} else if (param instanceof Long) {
			long l = ((Long) param).longValue();
			bd = new BigDecimal(l);
		}

		return bd;
	}
	
	public static void printEntity(Object obj, Class clzz){
		try {
			Method[] methods = clzz.getMethods();
			for(int i=0; i<methods.length; i++){
				Method method = methods[i];
				if(method.getName().startsWith("get") && method.getParameterTypes().length==0){
					System.out.println("变量名：" + method.getName() + "\t值：" + method.invoke(obj, null));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Field[] getAllFields(Object object){
		Class clazz = object.getClass();
		List<Field> fieldList = new ArrayList<Field>();
		while (clazz != null){
			fieldList.addAll(new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}

}
