package com.sevendays.service;

import com.sevendays.pojo.UserTable;

import java.util.List;

/**
 * @author 7DGroup
 * @Title: UserService
 * @Description:
 * @date 2019/10/25 / 20:46
 */

public interface UserService {
    List<UserTable> findAllList();

    List<UserTable> findinfo(UserTable userInfo);
}
