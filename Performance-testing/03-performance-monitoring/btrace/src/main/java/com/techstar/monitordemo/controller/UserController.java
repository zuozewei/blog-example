package com.techstar.monitordemo.controller;

import com.techstar.monitordemo.domain.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/7d")
public class UserController {

	@GetMapping(value = "/arg1")
	public String arg1(@RequestParam("name") String name) throws InterruptedException {
		Thread.sleep(2000);
		return "7DGroup," + name;
	}

	@GetMapping(value = "/arg2")
	public User arg2(User user) {
		return user;
	}

	@GetMapping(value = "/constructor")
	public User constructor(User user) {
		return user;
	}

	@GetMapping(value = "/same1")
	public String same(@RequestParam("name") String name) {
		return "7DGroup," + name;
	}

	@GetMapping(value = "/same2")
	public String same(@RequestParam("id") int id, @RequestParam("name") String name) {
		return "7DGroup," + name + "," + id;
	}

	@GetMapping(value = "/exception")
	public String exception() {
		try {
			System.out.println("start...");
			System.out.println(1 / 0); //模拟异常
			System.out.println("end...");
		} catch (Exception e) {}
		return "successful...";
	}

	@GetMapping(value = "/adder")
	public void adder() throws Exception {

	}

}


