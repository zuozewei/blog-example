package com.dunshan.api.service;

import com.dunshan.api.pojo.UserInfo;

import java.util.HashMap;
import java.util.List;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 11:51
 * @Description: rpc接口调用
 */
public interface UserInfoService {

    List<UserInfo> queryList();

    HashMap<String, Object> queryMap(String name);
}
