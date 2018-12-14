package com.zuozewei.springbootdemo.controller;

import com.zuozewei.springbootdemo.entity.Person;
import com.zuozewei.springbootdemo.entity.Result;
import com.zuozewei.springbootdemo.repository.PersonRepository;
import com.zuozewei.springbootdemo.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 控制器，处理Http/https请求（RESTful API）
 */

@RestController
public class PersonController {
	@Autowired
	PersonRepository personRepository;

	/**
	 * 查询所有人员列表（Get方式）
	 * @return
	 */

	@GetMapping(value = "/person")
	private List<Person> personlist() {
		return personRepository.findAll();
	}

	/**
	 * 添加人员 (Post方式)
	 * @param person
	 * @return
	 */
	@PostMapping(value = "/person")
	public Result personAdd(HttpServletRequest request,@RequestBody Person person) {

		return ResultUtil.sucess(personRepository.save(person));
	}

}
