package com.zuozewei.springbootfastjsondemo.JsonBean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

	// 忽略序列化和反序列化
	@JSONField(name = "AGE", serialize = false, deserialize = false)
	private int age;

	// ordinal指定顺序
	@JSONField(name = "LAST NAME", ordinal = 2)
	private String lastName;

	@JSONField(name = "FIRST NAME", ordinal = 1)
	private String firstName;

	// format格式化
	@JSONField(name = "DATE OF BIRTH", format = "dd/MM/yyyy",ordinal = 3)
	private Date dateOfBirth;

}
