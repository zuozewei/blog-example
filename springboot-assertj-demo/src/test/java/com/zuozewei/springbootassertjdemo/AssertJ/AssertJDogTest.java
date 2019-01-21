package com.zuozewei.springbootassertjdemo.AssertJ;

import com.zuozewei.springbootassertjdemo.entity.Dog;
import org.assertj.core.util.Maps;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AssertJDogTest {

	@Test(description = "对象断言1")
	public void whenComparingReferences_thenNotEqual() {

		// 实例化两个对象
		Dog fidos= new Dog("fidos",5.14f);
		Dog fidosClone = new Dog("fidosClone",5.14f);

		// 断言两个对象引用
		assertThat(fidos).isNotEqualTo(fidosClone);

	}

	@Test(description = "对象断言2")
	public void whenComparingFields_thenEqual() {
		// 实例化两个对象
		Dog fido = new Dog("Fido", 5.15f);
		Dog fidosClone = new Dog("Fido", 5.15f);

		// 断言两个对象内容
		assertThat(fido).isEqualToComparingFieldByFieldRecursively(fidosClone);
	}

	@Test(description = "布尔断言")
	public void whenisEmpty_isTrue() {
		// 实例化对象
		Dog fido = new Dog("Fido", 5.15f);

		// 断言字段是否为空
		assertThat(fido.getName().isEmpty()).isTrue();
	}

	@Test(description = "Iterable/Array断言1")
	public void whenCheckingForElement_thenContains(){
		List<String> list = Arrays.asList("1", "2", "3");

		// 断言是否包含给定元素
		assertThat(list).contains("1");
	}

	@Test(description = "Iterable/Array断言2")
	public void whenCheckingForElement_thenMultipleAssertions() {
		List<String> list = Arrays.asList("1", "2", "3");

		// 断言list不为空
		assertThat(list).isNotEmpty();
		// 断言list以给定字段开头
		assertThat(list).startsWith("1");
		// 断言list不包含null
		assertThat(list).doesNotContainNull();
		// 多个断言
		assertThat(list).isNotEmpty().contains("1").startsWith("1").doesNotContainNull().containsSequence("2", "3");
	}

	@Test(description = "字符断言")
	public void whenCheckingCharacter_thenIsUnicode() {
		char someCharacter = 'c';

		// 断言字符是否不是 'a'，在 Unicode 表中，是否大于 'b' 并且是小写的
		assertThat(someCharacter).isNotEqualTo('a').inUnicode().isGreaterThanOrEqualTo('b').isLowerCase();
	}

	@Test(description = "类断言1")
	public void whenCheckingRunnable_thenIsInterface() {
		// 断言Runnable类是一个接口
		assertThat(Runnable.class).isInterface();
	}

	@Test(description = "类断言2")
	public void whenAssigningNSEExToException_thenIsAssignable(){
		// 断言一个类是否可以从另一个类中分配
		assertThat(Exception.class).isAssignableFrom(NoSuchElementException.class);
	}

	@Test(description = "文件断言")
	public void whenCheckingFile_then() throws IOException {
		final File someFile = File.createTempFile("aaa", "bbb");
		someFile.deleteOnExit();

		// 断言文件是否存在，是文件而不是目录，可读写的
		assertThat(someFile).exists().isFile().canRead().canWrite();
	}

	@Test(description = "InputStream断言")
	public void whenCheckingIS_then() {
		InputStream given = new ByteArrayInputStream("foo".getBytes());
		InputStream expected = new ByteArrayInputStream("foo".getBytes());

		// 断言是否预期的 InputStream
		assertThat(given).hasSameContentAs(expected);
	}

	@Test(description = "Map断言")
	public void whenGivenMap_then() {
		Map<Integer, String> map = Maps.newHashMap(2, "a");

		// 断言Map是否为空，包含key “2”，不包含key “10” 并包含元素：key:2,value:“a”
		assertThat(map).isNotEmpty().containsKey(2).doesNotContainKeys(10).contains(entry(2, "a"));
	}

	@Test(description = "Throwable断言")
	public void whenGivenException_then() {
		Exception ex = new Exception("abc");

		// 断言是否抛出了给定的异常并且消息以“c”结尾
		assertThat(ex).hasNoCause().hasMessageEndingWith("c");
	}

	@Test(description = "描述断言")
	public void whenRunningAssertion_thenDescribed() throws Exception {
		Dog fidos= new Dog("fidos",5.14f);

		assertThat(fidos.getWeight()).as("%s's age should be equal to 5.15f").isEqualTo(5.15f);
	}

}
