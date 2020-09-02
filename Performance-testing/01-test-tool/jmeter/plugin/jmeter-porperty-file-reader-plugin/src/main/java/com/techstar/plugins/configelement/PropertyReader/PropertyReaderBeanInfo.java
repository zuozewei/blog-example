package com.techstar.plugins.configelement.PropertyReader;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import java.beans.PropertyDescriptor;

/**
 * @author zuozewei
 */
public class PropertyReaderBeanInfo extends BeanInfoSupport {
    //创建一个文件路径常量
    private  static final String FIELD_PROPERTY_FILE_PATH = "propFilePath";

    /**
     * 创建一个无参构造函数
     */
    public PropertyReaderBeanInfo() {
        //调用配置文件读取类
        super(PropertyReader.class);
        //在Jmeter GUI中添加字段及设置
        //添加"FIELD_PROPERTY_FILE_PATH"字段
        PropertyDescriptor p = property(FIELD_PROPERTY_FILE_PATH);
        //设置该字段必填项
        p.setValue(NOT_UNDEFINED,Boolean.FALSE);
        //设置该字段默认值
        p.setValue(DEFAULT,"");

    }
}
