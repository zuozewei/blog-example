package com.zuozewei.springbootjpademo.repository;

import com.zuozewei.springbootjpademo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 数据访问类
 * @author zuozewei
 *
 */
public interface UserRepository extends JpaRepository<User,Long> {

	User findByName(String name);

	User findByNameAndAge(String name, Integer age);

	@Query("from User u where u.name=:name")
	User findUser(@Param("name") String name);
}
