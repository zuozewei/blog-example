package com.zuozewei.demo.example;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.zuozewei.demo.entity.Dog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * 描述:
 * 常见类型转换
 *
 * @author zuozewei
 * @create 2020-09-17 15:26
 */

@SpringBootTest
@Slf4j
public class example extends AbstractTestNGSpringContextTests {

    @Test(description = "Convert使用：类型转换工具类")
    public void covert() {
        int a = 1;
        String aStr = Convert.toStr(a);
        //转换为指定类型数组
        String[] b = {"1", "2", "3", "4"};
        Integer[] bArr = Convert.toIntArray(b);
        log.info(bArr.toString());

        //转换为日期对象
        String dateStr = "2020-09-17";
        Date date = Convert.toDate(dateStr);
        log.info(date.toString());

        //转换为列表
        String[] strArr = {"a", "b", "c", "d"};
        List<String> strList = Convert.toList(String.class, strArr);
        log.info(strList.toString());
    }

    @Test(description = "DateUtil使用：日期时间工具")
    public void dateUtil() {
        //Date、long、Calendar之间的相互转换
        //当前时间
        Date date = DateUtil.date();
        log.info(date.toString());
        //Calendar转Date
        date = DateUtil.date(Calendar.getInstance());
        //时间戳转Date
        date = DateUtil.date(System.currentTimeMillis());
        //自动识别格式转换
        String dateStr = "2020-09-17";
        date = DateUtil.parse(dateStr);
        //自定义格式化转换
        date = DateUtil.parse(dateStr, "yyyy-MM-dd");
        //格式化输出日期
        String format = DateUtil.format(date, "yyyy-MM-dd");
        log.info(format.toString());
        //获得年的部分
        int year = DateUtil.year(date);
        //获得月份，从0开始计数
        int month = DateUtil.month(date);
        //获取某天的开始、结束时间
        Date beginOfDay = DateUtil.beginOfDay(date);
        Date endOfDay = DateUtil.endOfDay(date);
        //计算偏移后的日期时间
        Date newDate = DateUtil.offset(date, DateField.DAY_OF_MONTH, 2);
        //计算日期时间之间的偏移量
        long betweenDay = DateUtil.between(date, newDate, DateUnit.DAY);
    }

    @Test(description = "StrUtil使用：字符串工具")
    public void strUtil() {
        //判断是否为空字符串
        String str = "test";
        StrUtil.isEmpty(str);
        StrUtil.isNotEmpty(str);
        //去除字符串的前后缀
        StrUtil.removeSuffix("a.jpg", ".jpg");
        StrUtil.removePrefix("a.jpg", "a.");
        //格式化字符串
        String template = "这只是个占位符:{}";
        String str2 = StrUtil.format(template, "我是占位符");
        log.info("/strUtil format:{}", str2);
    }

    @Test(description = "ClassPath单一资源访问类：在classPath下查找文件")
    public void classPath() throws IOException {
        //获取定义在src/main/resources文件夹中的配置文件
        ClassPathResource resource = new ClassPathResource("generator.properties");
        Properties properties = new Properties();
        properties.load(resource.getStream());
        log.info("/classPath:{}", properties);
    }

    @Test(description = "ReflectUtil使用：Java反射工具类")
    public void reflectUtil() {
        //获取某个类的所有方法
        Method[] methods = ReflectUtil.getMethods(Dog.class);
        //获取某个类的指定方法
        Method method = ReflectUtil.getMethod(Dog.class, "getName");
        //使用反射来创建对象
        Dog dog = ReflectUtil.newInstance(Dog.class);
        //反射执行对象的方法
        ReflectUtil.invoke(dog, "setName","大黄");
        log.info(dog.getName());
    }

    @Test(description = "NumberUtil使用：数字处理工具类")
    public void numberUtil() {
        double n1 = 1.234;
        double n2 = 1.234;
        double result;
        //对float、double、BigDecimal做加减乘除操作
        result = NumberUtil.add(n1, n2);
        result = NumberUtil.sub(n1, n2);
        result = NumberUtil.mul(n1, n2);
        result = NumberUtil.div(n1, n2);
        //保留两位小数
        BigDecimal roundNum = NumberUtil.round(n1, 2);
        String n3 = "1.234";
        //判断是否为数字、整数、浮点数
        NumberUtil.isNumber(n3);
        NumberUtil.isInteger(n3);
        NumberUtil.isDouble(n3);
    }

    @Test(description = "RandomUtil使用：随机工具类")
    public void randomUtil() {
        int result;
        String uuid;
        //获得指定范围内的随机数
        result = RandomUtil.randomInt(1, 100);
        log.info("randomInt:{}",StrUtil.toString(result));
        //获得随机UUID
        uuid = RandomUtil.randomUUID();
        log.info("randomUUID:{}", uuid);
    }

    @Test(description = "BeanUtil使用：JavaBean的工具类")
    public void beanUtil() {
        Dog dog = new Dog();
        dog.setName("大黄");
        dog.setWeight(5.14f);

        //Bean转Map
        Map<String, Object> map = BeanUtil.beanToMap(dog);
        log.info("beanUtil bean to map:{}", map);
        //Map转Bean
        Dog mapDog = BeanUtil.mapToBean(map, Dog.class, false);
        log.info("beanUtil map to bean:{}", mapDog);
        //Bean属性拷贝
        Dog copyDog = new Dog();
        BeanUtil.copyProperties(dog, copyDog);
        log.info("beanUtil copy properties:{}", copyDog);
    }

    @Test(description = "CollUtil使用：集合工具类")
    public void collUtil() {
        //数组转换为列表
        String[] array = new String[]{"a", "b", "c", "d", "e"};
        List<String> list = CollUtil.newArrayList(array);
        //join：数组转字符串时添加连接符号
        String joinStr = CollUtil.join(list, ",");
        log.info("collUtil join:{}", joinStr);
        //将以连接符号分隔的字符串再转换为列表
        List<String> splitList = StrUtil.split(joinStr, ',');
        log.info("collUtil split:{}", splitList);
        //创建新的Map、Set、List
        HashMap<Object, Object> newMap = CollUtil.newHashMap();
        HashSet<Object> newHashSet = CollUtil.newHashSet();
        ArrayList<Object> newList = CollUtil.newArrayList();
        //判断列表是否为空
        CollUtil.isEmpty(list);
        CollUtil.isNotEmpty(list);
    }


    @Test(description = "MapUtil使用：Map工具类")
    public void mapUtil() {
        //将多个键值对加入到Map中
        Map<Object, Object> map = MapUtil.of(new String[][]{
                {"key1", "value1"},
                {"key2", "value2"},
                {"key3", "value3"}
        });
        //判断Map是否为空
        MapUtil.isEmpty(map);
        MapUtil.isNotEmpty(map);
    }


    @Test(description = "JSONUtil使用：JSON解析工具类")
    public void jsonUtil() {
        Dog dog = new Dog();
        dog.setName("大黄");
        dog.setWeight(5.14f);

        //对象转化为JSON字符串
        String jsonStr = JSONUtil.parse(dog).toString();
        log.info("jsonUtil parse:{}", jsonStr);
        //JSON字符串转化为对象
        Dog dogBean = JSONUtil.toBean(jsonStr, Dog.class);
        log.info("jsonUtil toBean:{}", dogBean);
        List<Dog> dogList = new ArrayList<>();
        dogList.add(dog);
        String jsonListStr = JSONUtil.parse(dogList).toString();
        //JSON字符串转化为列表
        dogList = JSONUtil.toList(new JSONArray(jsonListStr), Dog.class);
        log.info("jsonUtil toList:{}", dogList);
    }

    @Test(description = "SecureUtil使用：加密解密工具类")
    public void secureUtil() {
        //MD5加密
        String str = "123456";
        String md5Str = SecureUtil.md5(str);
        log.info("secureUtil md5:{}", md5Str);
    }

    @Test(description = "CaptchaUtil使用：图形验证码")
    public void captchaUtil(HttpServletRequest request, HttpServletResponse response) {
        //生成验证码图片
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        try {
            request.getSession().setAttribute("CAPTCHA_KEY", lineCaptcha.getCode());
            response.setContentType("image/png");//告诉浏览器输出内容为图片
            response.setHeader("Pragma", "No-cache");//禁止浏览器缓存
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);
            lineCaptcha.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(description = "Validator使用：字段验证器")
    public void validator() {
        //判断是否为邮箱地址
        boolean result = Validator.isEmail("zuozewei@hotmail.com");
        log.info("Validator isEmail:{}", result);
        //判断是否为手机号码
        result = Validator.isMobile("18911111111");
        log.info("Validator isMobile:{}", result);
        //判断是否为IPV4地址
        result = Validator.isIpv4("192.168.3.101");
        log.info("Validator isIpv4:{}", result);
        //判断是否为汉字
        result = Validator.isChinese("你好");
        log.info("Validator isChinese:{}", result);
        //判断是否为身份证号码（18位中国）
        result = Validator.isCitizenId("123456");
        log.info("Validator isCitizenId:{}", result);
        //判断是否为URL
        result = Validator.isUrl("http://www.7d.com");
        log.info("Validator isUrl:{}", result);
        //判断是否为生日
        result = Validator.isBirthday("2020-09-17");
        log.info("Validator isBirthday:{}", result);
    }

    @Test(description = "DigestUtil使用：摘要算法工具类")
    public void digestUtil() {
        String password = "123456";
        //计算MD5摘要值，并转为16进制字符串
        String result = DigestUtil.md5Hex(password);
        log.info("DigestUtil md5Hex:{}", result);
        //计算SHA-256摘要值，并转为16进制字符串
        result = DigestUtil.sha256Hex(password);
        log.info("DigestUtil sha256Hex:{}", result);
        //生成Bcrypt加密后的密文，并校验
        String hashPwd = DigestUtil.bcrypt(password);
        boolean check = DigestUtil.bcryptCheck(password,hashPwd);
        log.info("DigestUtil bcryptCheck:{}", check);
    }

    @Test(description = "HttpUtil使用：Http请求工具类")
    public void httpUtil() {
        String response = HttpUtil.get("http://example.com/");
        log.info("HttpUtil get:{}", response);
    }

}




