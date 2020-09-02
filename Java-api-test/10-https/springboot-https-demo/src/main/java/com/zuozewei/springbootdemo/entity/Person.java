package com.zuozewei.springbootdemo.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Person实体类
 */

@Entity
@Data
@Table(name = "person")
public class Person {
	//主键自增长
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private Integer age;
}
