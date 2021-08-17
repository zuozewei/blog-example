package com.zuozewei.dao;

import com.zuozewei.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by zuozewei
 * Time: 2019/6/23 18:04
 */
public interface PersonRepository extends JpaRepository<Person,Integer> {
    /**
     *  通过年龄来查询
     *  方法名固定
     * @param age
     * @return
     */

    List<Person> findByAge(Integer age);

//    Integer findOne(Integer id);

}
