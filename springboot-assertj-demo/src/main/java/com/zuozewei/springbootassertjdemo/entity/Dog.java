package com.zuozewei.springbootassertjdemo.entity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dog {
	private String name;
	private Float weight;

}
