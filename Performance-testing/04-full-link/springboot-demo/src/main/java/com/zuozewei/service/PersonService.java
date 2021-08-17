package com.zuozewei.service;


import com.zuozewei.dao.PersonRepository;
import com.zuozewei.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Created by zuozewei
 * Time: 2019/6/23 18:04
 */
@Service
public class PersonService {
    @Autowired
    private PersonRepository personRepository;

    /**
     * 事务管理测试
     * 两条数据同时成功，或者同时不成功
     * 保证数据库数据的完整性和一致性
     */
    @Transactional
    public void insertTwo(){
        Person personA = new Person();
        personA.setName("秋雅");
        personA.setAge(19);
        personRepository.save(personA);

        //故意抛出异常
//        System.out.print(1/0);

        Person personB = new Person();
        personB.setName("梦特娇");
        personB.setAge(25);
        personRepository.save(personB);
    }
}
