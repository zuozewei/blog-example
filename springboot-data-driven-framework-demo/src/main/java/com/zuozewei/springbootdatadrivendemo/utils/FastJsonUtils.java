package com.zuozewei.springbootdatadrivendemo.utils;

/**
 * 描述:
 * FastJson 工具类（阿里巴巴）
 *
 * @author zuozewei
 * @create 2019-07-12 15:03
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FastJsonUtils {

    /***
     * 解析为字符串
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static String fromString(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getString(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为布尔
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static Boolean formBoolean(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getBoolean(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /***
     * 解析为String
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static String formString(String jsonString, String key, String skey) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                JSONObject jsonObject1 = jsonObject.getJSONObject(key);
                String jsonObject2 = jsonObject1.getString(skey);
                return jsonObject2;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为Int
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static int formInt(String jsonString, String key, String skey) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                JSONObject jsonObject1 = jsonObject.getJSONObject(key);
                int jsonObject2 = jsonObject1.getInteger(skey);
                return jsonObject2;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /***
     * 解析为数字
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static Integer formInteger(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getInteger(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为长位十进制数
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static BigDecimal formBigDecimal(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getBigDecimal(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为双精度
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static Double formDouble(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getDouble(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为浮点数
     *
     * @param jsonString json字符串
     * @param key 关键字
     * @return  返回值
     */
    public static Float formFloat(String jsonString, String key) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                return jsonObject.getFloat(key);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 解析为对象
     *
     * @param jsonString json字符串
     * @param key 需要解析的关键字
     * @param t 泛型
     * @param <T> 泛型
     * @return 返回的对象
     */
    public static <T> T fromBean(String jsonString, String key, Class<T> t) {
        if (jsonString != null) {
            try {
                JSONObject jsonObj = JSONObject.parseObject(jsonString);
                return JSONObject.toJavaObject(jsonObj.getJSONObject(key), t);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /***
     * 解析为列表
     *
     * @param jsonString json字符串
     * @param key 需要解析的关键字
     * @param t 泛型
     * @param <T>   泛型
     * @return 返回的列表
     */
    public static <T> ArrayList<T> fromList(String jsonString, String key, Class<T> t) {
        ArrayList<T> list = new ArrayList<T>();
        if (jsonString != null && jsonString.length() > 0) {
            try {
                JSONObject jsonObj = JSONObject.parseObject(jsonString);
                JSONArray inforArray = jsonObj.getJSONArray(key);
                for (int index = 0; index < inforArray.size(); index++) {
                    list.add(JSONObject.toJavaObject(
                            inforArray.getJSONObject(index), t));
                }
            } catch (Exception e) {
            }
        }
        return list;
    }

    /***
     * 直接解析为bean
     *
     * @param jsonString json字符串
     * @param t 泛型
     * @param <T> 泛型
     * @return 返回的bean
     */
    public static <T> T fromBean(String jsonString, Class<T> t) {
        try {
            if (jsonString != null && jsonString.length() > 0) {
                return JSON.parseObject(jsonString, t);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 直接解析为list
     *
     * @param jsonString json字符串
     * @param t 泛型
     * @param <T> 泛型
     * @return 返回的泛型数组
     */
    public static <T> ArrayList<T> fromList(String jsonString, Class<T> t) {
        ArrayList<T> list = null;
        try {
            list = new ArrayList<>();
            if (jsonString != null && jsonString.length() > 0) {
                list.addAll(JSON.parseArray(jsonString, t));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /***
     * 将列表转换为json
     *
     * @param listBean 泛型数组
     * @return json字符串
     */
    public static <T> String ArrayToJson(ArrayList<T> listBean) {
        String jsonString = JSON.toJSONString(listBean);
        return jsonString;
    }

    /***
     * 将类转为json
     *
     * @param <T> 泛型
     * @return json字符串
     */
    public static <T> String BeanToJson(Object obj) {
        String jsonsString = JSON.toJSONString(obj);
        return jsonsString;
    }

    /**
     * 将fastjson  List转JSONArray
     *
     * @param list
     * @return
     */
    public static JSONArray ListToJSONArray (List<String> list){
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(list));
        return jsonArray;
    }

}
