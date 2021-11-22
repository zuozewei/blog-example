package com.dunshan.consumer.controller;

import com.dunshan.api.pojo.ResultVO;
import com.dunshan.api.pojo.UserInfo;
import com.dunshan.api.service.UserInfoService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 11:54
 * @Description: 消费测试接口
 */
@RestController
@RequestMapping("/api")
public class ConsumerController {

    /**
     * Dubbo远程调用注解
     */
    @Reference
    private UserInfoService userInfoService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResultVO getList() {
        List<UserInfo> providerTestDTOList = userInfoService.queryList();
        return new ResultVO.Builder<>().code(200).message("success").data(providerTestDTOList).build();
    }

    /**
     * 查询查询
     * @param name
     * @return
     */
    @GetMapping("/api/query")
    public ResultVO query(String name) {
        HashMap<String, Object> map = userInfoService.queryMap(name);
        return new ResultVO.Builder<>().code(200).message("success").data(map).build();
    }


}
