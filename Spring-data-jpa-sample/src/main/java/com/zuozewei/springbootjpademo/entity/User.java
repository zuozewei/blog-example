package com.zuozewei.springbootjpademo.entity;


import lombok.Data;

import javax.persistence.*;

/**
 * 用户实体类
 * @author zuozwei
 *
 */

@Entity
@Data
@Table(name = "person")
public class User {
	//主键自增长
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer age;

	public User() {
	}

	public User(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

}


