package com.dunshan.api.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 11:47
 * @Description: api接口实体
 */
@Data
public class UserInfo implements Serializable {
    /**
     * ID
     */
    private Integer id;
    /**
     * 名字
     */
    private String name;
    /**
     * 序号
     */
    private Integer number;
    /**
     * 日前
     */
    private Date createTime;
}
