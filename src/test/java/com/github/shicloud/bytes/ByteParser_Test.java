package com.github.shicloud.bytes;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.utils.ByteUtil;

public class ByteParser_Test {
	
	private static final Logger logger = LoggerFactory.getLogger(ByteParser_Test.class);
	
	
	public static void main(String[] args) {
		byte[] msg = new byte[0];
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("1")));//offset
		msg = ByteUtil.appendBytes(msg, ByteUtil.longToBytes(99));
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("36")));
		msg = ByteUtil.appendBytes(msg, ByteUtil.floatToBytes(49.8f));
		msg = ByteUtil.appendBytes(msg, ByteUtil.doubleToBytes(149.8));
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("5")));
		msg = ByteUtil.appendBytes(msg, "hello".getBytes());
		msg = ByteUtil.appendBytes(msg, ByteUtil.longToBytes(new Date().getTime()));
		msg = ByteUtil.appendBytes(msg, new byte[] {ByteUtil.boolTobyte(1)});
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("0")));
		logger.info(ByteUtil.byteToSplitStr(msg));
		User u = ByteParser.toObject(msg, User.class);
		logger.info("Id:"+u.getId());
		logger.info("Age:"+u.getAge());
		logger.info("Weight:"+u.getWeight());
		logger.info("Salary:"+u.getSalary());
		logger.info("LoginLength:"+u.getLoginLength());
		logger.info("Login:"+new String(u.getLogin()));
		logger.info("CreateTime:"+u.getCreateTime());
		logger.info("IsMale:"+u.getIsMale());
		logger.info("IsDel:"+u.getIsDel());
		
		u.setId(1000L);
		u.setLoginLength(6);
		u.setLogin("hello2".getBytes());
		u.setCreateTime(new Date());
		u.setIsDel(Byte.valueOf("1"));
		byte[] u2 = ByteParser.toBytes(u);
		logger.info(ByteUtil.byteToSplitStr(u2));
		ByteParser.toObject(u2, User.class);
		logger.info("Id:"+u.getId());
		logger.info("Age:"+u.getAge());
		logger.info("Weight:"+u.getWeight());
		logger.info("Salary:"+u.getSalary());
		logger.info("LoginLength:"+u.getLoginLength());
		logger.info("Login:"+new String(u.getLogin()));
		logger.info("CreateTime:"+u.getCreateTime());
		logger.info("IsMale:"+u.getIsMale());
		logger.info("IsDel:"+u.getIsDel());
		
		
		byte[] msg2 = new byte[0];
		int size = 5;
		for (int i = 0; i < size; i++) {
			User u3 = new User();
			u3.setId(Long.valueOf(100+i));
			u3.setLoginLength(i+1);
			String s = "";
			for (int j = 0; j < i+1 ; j++) {
				s += i;
			}
			u3.setLogin(s.getBytes());
			u3.setCreateTime(new Date());
			u3.setIsDel((byte)i);
			byte[] bytes = ByteParser.toBytes(u3);
			msg2 = ByteUtil.appendBytes(msg2, bytes);
		}
		msg2 = ByteUtil.appendBytes(msg2, new byte[] {1,2,3,4,5});
		logger.info(ByteUtil.byteToSplitStr(msg2));
		List<User> objectList = ByteParser.toObjectList(msg2, User.class, 0,36*size);
		for (User user : objectList) {
			logger.info("Id:"+user.getId());
			logger.info("Age:"+user.getAge());
			logger.info("Weight:"+user.getWeight());
			logger.info("Salary:"+user.getSalary());
			logger.info("LoginLength:"+user.getLoginLength());
			logger.info("Login:"+new String(user.getLogin()));
			logger.info("CreateTime:"+user.getCreateTime());
			logger.info("IsMale:"+user.getIsMale());
			logger.info("IsDel:"+user.getIsDel());
		}
	}
}
