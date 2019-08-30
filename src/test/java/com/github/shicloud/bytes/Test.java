package com.github.shicloud.bytes;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.utils.ByteUtil;

public class Test {
	
	private static final Logger logger = LoggerFactory.getLogger(Test.class);
	
	
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
		logger.info("CreateTime:"+u.getCreateTime());
		logger.info("IsMale:"+u.getIsMale());
		logger.info("IsDel:"+u.getIsDel());
		
		u.setId(1000L);
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
		logger.info("CreateTime:"+u.getCreateTime());
		logger.info("IsMale:"+u.getIsMale());
		logger.info("IsDel:"+u.getIsDel());
	}
}
