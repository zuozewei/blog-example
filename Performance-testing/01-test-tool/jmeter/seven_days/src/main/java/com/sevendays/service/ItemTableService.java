package com.sevendays.service;

import com.sevendays.pojo.ItemTable;

import java.util.List;

/**
 * @author 7DGroup
 * @Title: ItemTableService
 * @Description:
 * @date 2019/10/25 / 20:51
 */

public interface ItemTableService {
    List<ItemTable> findItemInfo(String userName);
}
