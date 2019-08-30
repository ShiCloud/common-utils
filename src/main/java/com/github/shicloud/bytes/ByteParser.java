package com.github.shicloud.bytes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.bytes.annotation.IgnoreToBytes;
import com.github.shicloud.bytes.annotation.IgnoreToObject;
import com.github.shicloud.bytes.annotation.Parser;
import com.github.shicloud.exception.NoDefinedMethodException;
import com.github.shicloud.utils.ByteUtil;
import com.github.shicloud.utils.ReflectUtil;

public class ByteParser {

	private static final Logger logger = LoggerFactory.getLogger(ByteParser.class);
	
	private static final Map<Class<?>, Map<Integer, Object[]>> objectMapCache = new ConcurrentHashMap<>();

	public static <T> T toObject(byte[] bytes, Class<T> clazz) {

		T obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(clazz.getName() + " InstantiationException ");
			return null;
		}

		Map<Integer, Object[]> objMap = getObjectMap(obj);

		int index = 0;
		for (Object[] parserAndField : objMap.values()) {
			Parser parser = (Parser) parserAndField[0];
			Field field = (Field) parserAndField[1];
			
			if(field.getAnnotation(IgnoreToObject.class)!=null) {
				continue;
			}
			
			String fieldName = field.getName();
			Method setter = null;
			Method dependsOnGetter = null;
			try {
				setter = ReflectUtil.getSetter(obj, fieldName);
				int dependsOnIndex = parser.dependsOn();
				if (dependsOnIndex > 0) {
					Object[] dependsOnParserAndField = objMap.get(dependsOnIndex);
					Field dependsOnField = (Field)dependsOnParserAndField[1];
					if(dependsOnField.getType() != Integer.class) {
						logger.error(fieldName + " dependsOn field type not an integer ");
						return null;
					}
					String dependsOnFieldName = dependsOnField.getName();
					dependsOnGetter = ReflectUtil.getGetter(obj, dependsOnFieldName);
					
				}
			} catch (NoDefinedMethodException e) {
				logger.error(fieldName + " can not be found ");
				return null;
			}

			try {
				
				int fieldLength = dependsOnGetter != null ? (int) dependsOnGetter.invoke(obj) : parser.lenght();
				byte[] b = ByteUtil.subBytes(bytes, index + parser.offset(), fieldLength);

				if (field.getType() == Byte.class) {
					setter.invoke(obj, Byte.valueOf(b[0]));
				} else if (field.getType() == Boolean.class) {
					setter.invoke(obj, ByteUtil.byteToBool(b[0]));
				} else if (field.getType() == Short.class) {
					setter.invoke(obj, ByteUtil.byteToshort(b));
				} else if (field.getType() == Integer.class) {
					setter.invoke(obj, ByteUtil.fillFrontBytesToInt(b, 0, fieldLength));
				} else if (field.getType() == Long.class) {
					setter.invoke(obj, ByteUtil.fillFrontBytesToLong(b, 0, fieldLength));
				} else if (field.getType() == Float.class) {
					setter.invoke(obj, ByteUtil.fillFrontBytesToFloat(b, 0, fieldLength));
				} else if (field.getType() == Double.class) {
					setter.invoke(obj, ByteUtil.fillFrontBytesToDouble(b, 0, fieldLength));
				} else if (field.getType() == String.class) {
					setter.invoke(obj, ByteUtil.byteToStr(b));
				} else if (field.getType() == Date.class) {
					setter.invoke(obj, new Date(ByteUtil.bytesToLong(b)));
				}

				index += (fieldLength + parser.offset());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error(fieldName + " parser error ");
				return null;
			}
		}
		return obj;
	}
	
	public static byte[] toBytes(Object obj) {
		Map<Integer, Object[]> objMap = getObjectMap(obj);
		byte[] bytes = new byte[0];
		for (Object[] parserAndField : objMap.values()) {
			Field field = (Field) parserAndField[1];
			if(field.getAnnotation(IgnoreToBytes.class)!=null) {
				continue;
			}
			
			try {
				Method getter = ReflectUtil.getGetter(obj, field.getName());
				Parser parser = (Parser) parserAndField[0];
				if(parser.offset()>0) {
					bytes = ByteUtil.appendBytes(bytes, new byte[parser.offset()]);
				}
				byte[] b = new byte[0];
				int lenght = parser.lenght();
				if (field.getType() == Byte.class) {
					b = ByteUtil.appendBytes(b, new byte[] {(Byte)getter.invoke(obj)});
				} else if (field.getType() == Boolean.class) {
					b = ByteUtil.appendBytes(b, new byte[] {ByteUtil.boolTobyte((Boolean)getter.invoke(obj))});
				} else if (field.getType() == Short.class) {
					b = ByteUtil.shortToBytes((Short)getter.invoke(obj));
				} else if (field.getType() == Integer.class) {
					b = ByteUtil.intToBytes((Integer)getter.invoke(obj));
				} else if (field.getType() == Long.class) {
					b = ByteUtil.longToBytes((Long)getter.invoke(obj));
				} else if (field.getType() == Float.class) {
					b = ByteUtil.floatToBytes((Float)getter.invoke(obj));
				} else if (field.getType() == Double.class) {
					b = ByteUtil.doubleToBytes((Double)getter.invoke(obj));
				} else if (field.getType() == String.class) {
					b = ((String)getter.invoke(obj)).getBytes();
					lenght = b.length;
				} else if (field.getType() == Date.class) {
					b = ByteUtil.longToBytes(((Date)getter.invoke(obj)).getTime());
				}
				
				if(b.length > lenght) {
					bytes = ByteUtil.appendBytes(bytes, ByteUtil.cutFrontBytes(b, lenght));
				}else if (b.length < lenght) {
					bytes = ByteUtil.appendBytes(bytes, ByteUtil.fillFrontBytes(b, lenght));
				}else {
					bytes = ByteUtil.appendBytes(bytes, b);
				}
			} catch (Exception e) {
				logger.error(field.getName() + " parser error ");
				return null;
			}
		}
		
		return bytes;
	}

	private static <T> Map<Integer, Object[]> getObjectMap(T obj) {
		Map<Integer, Object[]> objMap = objectMapCache.get(obj.getClass());
		if(objMap!=null){
    		return objMap;
    	}
		
		objMap = new TreeMap<>();
		Map<String, Field> fieldMap = ReflectUtil.getFields(obj.getClass());
		for (Field field : fieldMap.values()) {
			Parser parser = field.getAnnotation(Parser.class);
			if (parser == null || parser.index() == 0) {
				logger.error(field.getName() + " index is zero ");
				return null;
			}
			Object[] parserAndField = new Object[] { parser, field };
			objMap.put(parser.index(), parserAndField);
		}
		
		objectMapCache.put(obj.getClass(),objMap);
		return objMap;
	}
}
