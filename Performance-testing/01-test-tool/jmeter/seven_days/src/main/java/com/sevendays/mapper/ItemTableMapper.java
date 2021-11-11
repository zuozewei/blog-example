package com.sevendays.mapper;

import com.sevendays.pojo.ItemTable;
import com.sevendays.pojo.ItemTableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ItemTableMapper {
    int countByExample(ItemTableExample example);

    int deleteByExample(ItemTableExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ItemTable record);

    int insertSelective(ItemTable record);

    List<ItemTable> selectByExample(ItemTableExample example);

    ItemTable selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ItemTable record, @Param("example") ItemTableExample example);

    int updateByExample(@Param("record") ItemTable record, @Param("example") ItemTableExample example);

    int updateByPrimaryKeySelective(ItemTable record);

    int updateByPrimaryKey(ItemTable record);
}