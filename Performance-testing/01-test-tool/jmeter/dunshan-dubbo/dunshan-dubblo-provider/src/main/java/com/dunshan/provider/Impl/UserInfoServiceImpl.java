package com.dunshan.provider.Impl;

import com.dunshan.api.pojo.UserInfo;
import com.dunshan.api.service.UserInfoService;
import org.apache.dubbo.config.annotation.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 12:03
 * @Description: 提供者实现类
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Override
    public List<UserInfo> queryList() {
        // 初始化数据
        UserInfo testDTO1 = new UserInfo();
        testDTO1.setId(1);
        testDTO1.setName("学生");
        testDTO1.setNumber(100);
        testDTO1.setCreateTime(new Date());
        UserInfo testDTO2 = new UserInfo();
        testDTO2.setId(2);
        testDTO2.setName("7D-RESAR-性能测试");
        testDTO2.setNumber(101);
        testDTO2.setCreateTime(new Date());
        // 组装数据
        List<UserInfo> list = new ArrayList<>();
        list.add(testDTO1);
        list.add(testDTO2);
        return list;
    }

    @Override
    public HashMap<String, Object> queryMap(String name) {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(name, "7D-RESAR-初级工具班");
        map.put("nacos", "注册中心，配置管理中心");
        map.put("date", System.currentTimeMillis());
        return map;
    }
}
