package com.github.shicloud.bytes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.bytes.annotation.IgnoreToBytes;
import com.github.shicloud.bytes.annotation.IgnoreToObject;
import com.github.shicloud.bytes.annotation.LittleEnd;
import com.github.shicloud.bytes.annotation.Parser;
import com.github.shicloud.bytes.annotation.TargetModel;
import com.github.shicloud.exception.NoDefinedMethodException;
import com.github.shicloud.utils.ByteUtil;
import com.github.shicloud.utils.ReflectUtil;

public class ByteParser {

	private static final Logger logger = LoggerFactory.getLogger(ByteParser.class);

	private static final Map<Class<?>, Map<Integer, Object[]>> objectMapCache = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> classCache = new HashMap<>();

	private static final int errorIndex = -1;

	public static <T> T toObject(byte[] bytes, Class<T> clazz, int offset, int endIndex) {

		T obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(clazz.getName() + " InstantiationException ");
			return null;
		}

		Map<Integer, Object[]> objMap = getObjectMap(obj);

		int index = setObjectValue(bytes, obj, objMap, offset, endIndex);
		if (index == errorIndex) {
			return null;
		}
		return obj;
	}
	

	private static int getFieldLength(Object obj,Map<Integer, Object[]> objMap,Parser parser,String fieldName) 
			throws Exception {
		Method dependsOnGetter = null;
		int dependsOnIndex = parser.dependsOn();
		if (dependsOnIndex > 0) {
			Object[] dependsOnParserAndField = objMap.get(dependsOnIndex);
			Field dependsOnField = (Field) dependsOnParserAndField[1];
			if (dependsOnField.getType() != Integer.class) {
				logger.error(fieldName + " dependsOn field type not an integer ");
				return 0;
			}
			String dependsOnFieldName = dependsOnField.getName();
			dependsOnGetter = ReflectUtil.getGetter(obj, dependsOnFieldName);
			if(dependsOnGetter != null) {
				Object invoke = dependsOnGetter.invoke(obj);
				if(invoke !=null) {
					return (int) invoke;
				}
			}
			
			return 0;
		}
		
		return parser.lenght();
	}

	/**
	 * @param bytes
	 * @param obj
	 * @param objMap
	 * @param index
	 * @return currIndex
	 */
	private static <T> int setObjectValue(byte[] bytes, T obj, Map<Integer, Object[]> objMap, int index, int endIndex) {
		for (Object[] parserAndField : objMap.values()) {
			Parser parser = (Parser) parserAndField[0];
			Field field = (Field) parserAndField[1];
			String fieldName = field.getName();
			Method setter = null;
			try {
				setter = ReflectUtil.getSetter(obj, fieldName);
			} catch (NoDefinedMethodException e) {
				logger.error(fieldName + " can not be found ");
				return errorIndex;
			}

			try {
				int fieldLength = getFieldLength(obj, objMap, parser, fieldName);
				int currIndex = index;
				if (field.getAnnotation(IgnoreToObject.class) != null) {
					continue;
				}
				index += (fieldLength + parser.offset());//判断数组是否够长
				if (index > bytes.length) {
					logger.debug(fieldName + " exceed total length ");
					return errorIndex;
				}
				if(fieldLength == 0) {
					continue;
				}
				
				byte[] b = null;
				
				if (field.getType() == List.class && parser.dependsOn() > 0) {
					b = ByteUtil.subBytes(bytes, currIndex, fieldLength + parser.offset());
				}else {
					b = ByteUtil.subBytes(bytes, currIndex + parser.offset(), fieldLength);
				}
				
				
				LittleEnd littleEnd = field.getAnnotation(LittleEnd.class);
				boolean isLittleEnd = (littleEnd!=null && littleEnd.value());
				
				if (field.getType() == Byte.class) {
					setter.invoke(obj, Byte.valueOf(b[0]));
				} else if (field.getType() == Boolean.class) {
					setter.invoke(obj, ByteUtil.byteToBool(b[0]));
				} else if (field.getType() == Short.class) {
					if(isLittleEnd) {
						setter.invoke(obj, ByteUtil.bytesLEToshort(b));
					}else {
						setter.invoke(obj, ByteUtil.bytesToshort(b));
					}
				} else if (field.getType() == Integer.class) {
					if(isLittleEnd) {
						setter.invoke(obj, ByteUtil.bytesLEToInt(ByteUtil.fillEndBytes(b, fieldLength)));
					}else {
						setter.invoke(obj, ByteUtil.fillFrontBytesToInt(b, 0, fieldLength));
					}
				} else if (field.getType() == Long.class) {
					if(isLittleEnd) {
						setter.invoke(obj, ByteUtil.bytesLEToLong(ByteUtil.fillEndBytes(b, fieldLength)));
					}else {
						setter.invoke(obj, ByteUtil.fillFrontBytesToLong(b, 0, fieldLength));
					}
				} else if (field.getType() == Float.class) {
					if(isLittleEnd) {
						setter.invoke(obj, ByteUtil.bytesLEToFloat(ByteUtil.fillEndBytes(b, fieldLength)));
					}else {
						setter.invoke(obj, ByteUtil.fillFrontBytesToFloat(b, 0, fieldLength));
					}
				} else if (field.getType() == Double.class) {
					if(isLittleEnd) {
						setter.invoke(obj, ByteUtil.bytesLEToDouble(ByteUtil.fillEndBytes(b, fieldLength)));
					}else {
						setter.invoke(obj, ByteUtil.fillFrontBytesToDouble(b, 0, fieldLength));
					}
				} else if (field.getType() == String.class) {
					setter.invoke(obj, ByteUtil.byteToStr(b));
				} else if (field.getType() == Date.class) {
					if(isLittleEnd) {
						setter.invoke(obj, new Date(ByteUtil.bytesLEToLong(b)/parser.divide()));
					}else {
						setter.invoke(obj, new Date(ByteUtil.bytesToLong(b)/parser.divide()));
					}
				} else if (field.getType() == byte[].class) {
					setter.invoke(obj, b);
				} else if (field.getType() == List.class) {
					TargetModel target = field.getAnnotation(TargetModel.class);
					Class<?> jvmClasses = getJVMClasses(target.value());
					if(jvmClasses == null) {
						logger.error("unknown class " + target.value());
						return errorIndex;
					}
					List<?> objectList = toObjectList(b,jvmClasses);
					setter.invoke(obj, objectList);
				}  else {
					logger.error(fieldName + " unsupport data type " + field.getType());
					return errorIndex;
				}
			} catch (Exception e) {
				logger.error(fieldName + " setObjectValue error ");
				return errorIndex;
			}
		}
		return index;
	}
	
	public static Class<?> getJVMClasses(String name) throws ClassNotFoundException{
		Class<?> c = classCache.get(name);
		if(c!=null) {
			return c;
		}
		c = Class.forName(name);
		classCache.put(name, c);
		return c;
	}
	
	
	public static byte[] toBytes(Object obj) {
		Map<Integer, Object[]> objMap = getObjectMap(obj);
		byte[] bytes = new byte[0];
		for (Object[] parserAndField : objMap.values()) {
			Parser parser = (Parser) parserAndField[0];
			Field field = (Field) parserAndField[1];
			String fieldName = field.getName();

			if (field.getAnnotation(IgnoreToBytes.class) != null) {
				continue;
			}

			try {
				if (parser.offset() > 0) {
					bytes = ByteUtil.appendBytes(bytes, new byte[parser.offset()]);
				}
				
				Method getter = ReflectUtil.getGetter(obj, fieldName);
				int fieldLength = getFieldLength(obj, objMap, parser, fieldName);
				
				byte[] b = new byte[0];
				if (getter.invoke(obj) == null) {
					bytes = ByteUtil.appendBytes(bytes, new byte[fieldLength]);
				} else {
					LittleEnd littleEnd = field.getAnnotation(LittleEnd.class);
					boolean isLittleEnd = (littleEnd!=null && littleEnd.value());
					if (field.getType() == Byte.class) {
						b = ByteUtil.appendBytes(b, new byte[] { (Byte) getter.invoke(obj) });
					} else if (field.getType() == Boolean.class) {
						b = ByteUtil.appendBytes(b, new byte[] { ByteUtil.boolTobyte((Boolean) getter.invoke(obj)) });
					} else if (field.getType() == Short.class) {
						if(isLittleEnd) {
							b = ByteUtil.shortToBytesLE((Short) getter.invoke(obj));
						}else {
							b = ByteUtil.shortToBytes((Short) getter.invoke(obj));
						}
					} else if (field.getType() == Integer.class) {
						if(isLittleEnd) {
							b = ByteUtil.intToBytesLE((Integer) getter.invoke(obj));
						}else {
							b = ByteUtil.intToBytes((Integer) getter.invoke(obj));
						}
					} else if (field.getType() == Long.class) {
						if(isLittleEnd) {
							b = ByteUtil.longToBytesLE((Long) getter.invoke(obj));
						}else {
							b = ByteUtil.longToBytes((Long) getter.invoke(obj));
						}
					} else if (field.getType() == Float.class) {
						if(isLittleEnd) {
							b = ByteUtil.floatToBytesLE((Float) getter.invoke(obj));
						}else {
							b = ByteUtil.floatToBytes((Float) getter.invoke(obj));
						}
					} else if (field.getType() == Double.class) {
						if(isLittleEnd) {
							b = ByteUtil.doubleToBytesLE((Double) getter.invoke(obj));
						}else {
							b = ByteUtil.doubleToBytes((Double) getter.invoke(obj));
						}
					} else if (field.getType() == String.class) {
						b = ((String) getter.invoke(obj)).getBytes();
						fieldLength = b.length;
					} else if (field.getType() == Date.class) {
						if(isLittleEnd) {
							b = ByteUtil.longToBytesLE(((Date) getter.invoke(obj)).getTime());
						}else {
							b = ByteUtil.longToBytes(((Date) getter.invoke(obj)).getTime());
						}
					} else if (field.getType() == byte[].class) {
						b = (byte[]) getter.invoke(obj);
					} else if (field.getType() == List.class) {
						List<?> objectList = (List<?>) getter.invoke(obj);
						for (Object object : objectList) {
							b = ByteUtil.appendBytes(b, toBytes(object));
						}
					}  else {
						logger.error(field.getName() + " unsupport data type " + field.getType());
						return null;
					}

					if (b.length > fieldLength) {
						bytes = ByteUtil.appendBytes(bytes, ByteUtil.cutFrontBytes(b, fieldLength));
					} else if (b.length < fieldLength) {
						bytes = ByteUtil.appendBytes(bytes, ByteUtil.fillFrontBytes(b, fieldLength));
					} else {
						bytes = ByteUtil.appendBytes(bytes, b);
					}
				}
			} catch (Exception e) {
				logger.error(field.getName() + " toBytes error ");
				return null;
			}
		}

		return bytes;
	}
	
	private static <T> Map<Integer, Object[]> getObjectMap(T obj) {
		Map<Integer, Object[]> objMap = objectMapCache.get(obj.getClass());
		if (objMap != null) {
			return objMap;
		}

		objMap = new TreeMap<>();
		Map<String, Field> fieldMap = ReflectUtil.getFields(obj.getClass());
		for (Field field : fieldMap.values()) {
			Parser parser = field.getAnnotation(Parser.class);
			if (parser == null) {
				logger.error(field.getName() + " not set parser ");
				return null;
			}
			if (parser.index() == 0) {
				logger.error(field.getName() + " index is zero ");
				return null;
			}
			Object[] parserAndField = new Object[] { parser, field };
			objMap.put(parser.index(), parserAndField);
		}

		objectMapCache.put(obj.getClass(), objMap);
		return objMap;
	}

	public static <T> T toObject(byte[] bytes, Class<T> clazz) {
		return toObject(bytes, clazz, 0, bytes.length);
	}

	private static <T> T createObject(Class<T> clazz) {
		T obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(clazz.getName() + " InstantiationException ");
			return null;
		}
		return obj;
	}

	public static <T> List<T> toObjectList(byte[] bytes, Class<T> clazz, int offset, int endIndex) {
		List<T> list = new ArrayList<T>();
		int index = offset;
		Map<Integer, Object[]> objMap = getObjectMap(createObject(clazz));

		while (index < bytes.length && index < endIndex) {
			T obj = createObject(clazz);
			index = setObjectValue(bytes, obj, objMap, index, endIndex);
			if (index == errorIndex) {
				return list;
			}
			list.add(obj);
		}
		return list;
	}

	public static <T> List<T> toObjectList(byte[] bytes, Class<T> clazz) {
		return toObjectList(bytes, clazz, 0, bytes.length);
	}


}
