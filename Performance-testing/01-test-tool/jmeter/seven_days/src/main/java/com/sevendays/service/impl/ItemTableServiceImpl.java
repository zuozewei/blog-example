package com.sevendays.service.impl;

import com.sevendays.controller.UserController;
import com.sevendays.mapper.ItemTableMapper;
import com.sevendays.pojo.ItemTable;
import com.sevendays.pojo.ItemTableExample;
import com.sevendays.service.ItemTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 7DGroup
 * @Title: ItemTableServiceImpl
 * @Description: 项目实现类
 * @date 2019/10/25 / 20:52
 */

@Service
public class ItemTableServiceImpl implements ItemTableService {

    @Autowired
    ItemTableMapper itemTableMapper;

    private static Logger LOG = LoggerFactory.getLogger(ItemTableServiceImpl.class);

    /**
     * 根据用户名查询项目信息
     *
     * @param userName
     * @return
     */
    @Override
    public List<ItemTable> findItemInfo(String userName) {
        ItemTableExample example = new ItemTableExample();
        ItemTableExample.Criteria criteria = example.createCriteria();
        criteria.andTestNameLike(userName);
        return itemTableMapper.selectByExample(example);
    }

}
