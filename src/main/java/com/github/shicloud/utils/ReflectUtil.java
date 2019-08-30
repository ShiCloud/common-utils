package com.github.shicloud.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.exception.NoDefinedMethodException;

public class ReflectUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);

	private static final Map<Class<?>, Map<String,Field>> fieldsCache = 
			new ConcurrentHashMap<Class<?>, Map<String,Field>>();
	
	private static final Map<Class<?>, Map<String,Method>> methodsCache = 
			new ConcurrentHashMap<Class<?>, Map<String,Method>>();
	
	public static <T> Map<String,Field> getFields(Class<T> clazz) {
		Map<String,Field> fMap = fieldsCache.get(clazz);
		if(fMap!=null){
    		return fMap;
    	}
        Map<String,Field> classFieldMap = new HashMap<>();
        Class<? extends Object> tempClass = clazz;
        //包括父类的Field
        while (tempClass != null && !tempClass.getName().toLowerCase().equals("java.lang.object")) {
            Field[] declaredFields = tempClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
				Field field = declaredFields[i];
				if(field.getName().equals("serialVersionUID")){
					continue;
				}
				if(classFieldMap.get(field.getName())==null){
					classFieldMap.put(field.getName(), field);
					logger.debug("add class "+tempClass.getName()+" field :"+field.getName()+" into fieldsCache");
				}
			}
            tempClass = tempClass.getSuperclass();
        }
        fieldsCache.put(clazz, classFieldMap);
        return classFieldMap;
    }
	
	public static <T> Method getMethod(Class<T> clazz, String methodName) throws NoDefinedMethodException {
        Map<String,Method> mmap = methodsCache.get(clazz);
		if(mmap==null){
			mmap = new HashMap<String, Method>();
    	}
		if(mmap.get(methodName) == null){
	        Class<? extends Object> tempClass = clazz;
	        //包括父类的Method
	        while (tempClass != null && !tempClass.getName().toLowerCase().equals("java.lang.object")) {
	        	Method[] methods = tempClass.getDeclaredMethods();
	        	for (Method m : methods) {
	        		mmap.put(m.getName(), m);
	        		logger.debug("add class "+tempClass.getName()+" method :"+m.getName()+" into methodsCache");
				}
	            tempClass = tempClass.getSuperclass();
	        }
	        methodsCache.put(clazz, mmap);
		}
		Method method = mmap.get(methodName);
		if (method == null) {
			throw new NoDefinedMethodException(methodName);
		}
        return method;
    }
	
	public static <T> Method getGetter(T obj, String fieldName) throws NoDefinedMethodException{
		String methodName = "get"+CamelNameUtils.capitalize(fieldName);
		return getMethod(obj.getClass(), methodName);
	}
	
	public static <T> Method getSetter(T obj, String fieldName) throws NoDefinedMethodException{
		String methodName = "set"+CamelNameUtils.capitalize(fieldName);
		return getMethod(obj.getClass(), methodName);
	}
	
}