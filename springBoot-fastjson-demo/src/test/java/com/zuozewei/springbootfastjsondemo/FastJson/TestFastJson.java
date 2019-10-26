package com.zuozewei.springbootfastjsondemo.FastJson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.zuozewei.springbootfastjsondemo.JsonBean.Person;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestFastJson {
	private List<Person> listOfPersons;

	@BeforeTest
	public void setUp() {
		listOfPersons = new ArrayList<Person>();
		// 获取当前时间，取得一个Calendar的实例
		Calendar calendar = Calendar.getInstance();
		// 设置日历
		calendar.set(2019, 01, 31);
		// 实例化Java对象
		listOfPersons.add(new Person(15, "John", "Doe", calendar.getTime()));
		listOfPersons.add(new Person(20, "Janette", "Doe", calendar.getTime()));
	}

	@Test(description = "将Java对象转换为JSON格式")
	public void whenJavaList_thanConvertToJsonCorrect() {

		// 将Java对象转换为JSON字符串
		String personJsonFormat = JSON.toJSONString(listOfPersons);
		// 断言字符串是否相同
		Assert.assertEquals(personJsonFormat, "[{\"FIRST NAME\":\"Doe\",\"LAST NAME\":\"John\",\"DATE OF BIRTH\":"
				+ "\"01/02/2019\"},{\"FIRST NAME\":\"Doe\",\"LAST NAME\":\"Janette\",\"DATE OF BIRTH\":"
				+ "\"01/02/2019\"}]");
	}

	@Test(description = "将JSON字符串解析为Java对象")
	public void whenJson_thanConvertToObjectCorrect() {
		// 将Java对象转换为JSON字符串
		String personJsonFormat = JSON.toJSONString(listOfPersons.get(0));
		System.out.println(personJsonFormat);

		// 从JSON字符串中获取Java对象
		Person newPerson = JSON.parseObject(personJsonFormat, Person.class);
		System.out.println(newPerson.toString());

		// 使用参数serialize忽略Age字段的序列化
		Assert.assertEquals(newPerson.getAge(), 0);
		System.out.println(newPerson.getAge());

		Assert.assertEquals(newPerson.getFirstName(), listOfPersons.get(0).getFirstName());
		System.out.println(newPerson.getFirstName());

		Assert.assertEquals(newPerson.getLastName(), listOfPersons.get(0).getLastName());
		System.out.println(newPerson.getLastName());
	}

	@Test(description = "创建JSON对象")
	public void whenGenerateJson_thanGenerationCorrect() {
		// 组合JSONObject和JSONArray对象
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < 2; i++) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("FIRST NAME", "John" + i);
			jsonObject.put("LAST NAME", "Doe" + i);
			jsonObject.put("DATE OF BIRTH", "2019/2/1 12:12:12");
			jsonArray.add(jsonObject);
		}

		Assert.assertEquals(jsonArray.toString(), "[{\"LAST NAME\":\"Doe0\",\"DATE OF BIRTH\":"
				+ "\"2019/2/1 12:12:12\",\"FIRST NAME\":\"John0\"},{\"LAST NAME\":\"Doe1\","
				+ "\"DATE OF BIRTH\":\"2019/2/1 12:12:12\",\"FIRST NAME\":\"John1\"}]");
		System.out.println(jsonArray.toString());
	}

	@Test(description = "使用ContextValueFilter配置JSON转换")
	public void givenContextFilter_whenJavaObject_thanJsonCorrect() {
		// 使用ContextValueFilter对象对转换流应用其他过滤和自定义处理
		ContextValueFilter valueFilter = new ContextValueFilter() {
			public Object process(BeanContext context, Object object, String name, Object value) {
				// 隐藏了 DATE OF BIRTH 字段，强制一个常量值
				if (name.equals("DATE OF BIRTH")) {
					return "NOT TO DISCLOSE";
				}
				// 忽略了所有不是John或Doe的字段
				if (value.equals("John") || value.equals("Doe")) {
					return ((String) value).toUpperCase();
				} else {
					return null;
				}
			}
		};
		// 将Java对象转换为JSON字符串并过滤及自定义处理
		String personJsonFormat = JSON.toJSONString(listOfPersons, valueFilter);
		System.out.println(personJsonFormat);

	}

	@Test(description = "使用NameFilter和SerializeConfig")
	public void givenSerializeConfig_whenJavaObject_thanJsonCorrect() {

		// formatName过滤器来处理字段名称。
		NameFilter formatName = new NameFilter() {
			public String process(Object object, String name, Object value) {
				return name.toLowerCase().replace(" ", "_");
			}
		};
		SerializeConfig.getGlobalInstance().addFilter(Person.class, formatName);

		// 将对象转换为JSON格式，快速在日期字段上应用相同的格式规则。
		String jsonOutput = JSON.toJSONStringWithDateFormat(listOfPersons, "yyyy-MM-dd");

		System.out.println(jsonOutput);
		Assert.assertEquals(jsonOutput, "[{\"first_name\":\"Doe\",\"last_name\":\"John\","
				+ "\"date_of_birth\":\"2019-02-01\"},{\"first_name\":\"Doe\",\"last_name\":"
				+ "\"Janette\",\"date_of_birth\":\"2019-02-01\"}]");


		// 重新设置自定义序列化器
		SerializeConfig.getGlobalInstance().put(Person.class, null);
	}

}
