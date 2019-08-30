package com.github.shicloud.bytes;

import java.util.Date;

import com.github.shicloud.bytes.annotation.IgnoreToObject;
import com.github.shicloud.bytes.annotation.Parser;

public class User {

	@Parser(index = 1, lenght = 8, offset = 2)
	private Long id;

	@Parser(index = 2, lenght = 2)
	private Short age;

	@Parser(index = 3, lenght = 4)
	private Float weight;

	@Parser(index = 4, lenght = 8)
	private Double salary;
	
	@Parser(index = 5, lenght = 2)
	private Integer loginLength;
	
	@Parser(index = 6, dependsOn = 5)
	private String login;
	
	@Parser(index = 7, lenght = 8)
	//@IgnoreToBytes
	@IgnoreToObject
	private Date createTime;
	
	@Parser(index = 8, lenght = 1)
	private Boolean isMale;
	
	@Parser(index = 9, lenght = 1)
	private Byte isDel;
	
	@Parser(index = 10, lenght = 10)
	private byte[] descrption;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Short getAge() {
		return age;
	}

	public void setAge(Short age) {
		this.age = age;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(Double salary) {
		this.salary = salary;
	}

	public Integer getLoginLength() {
		return loginLength;
	}

	public void setLoginLength(Integer loginLength) {
		this.loginLength = loginLength;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Boolean getIsMale() {
		return isMale;
	}

	public void setIsMale(Boolean isMale) {
		this.isMale = isMale;
	}

	public Byte getIsDel() {
		return isDel;
	}

	public void setIsDel(Byte isDel) {
		this.isDel = isDel;
	}

	public byte[] getDescrption() {
		return descrption;
	}

	public void setDescrption(byte[] descrption) {
		this.descrption = descrption;
	}

}
