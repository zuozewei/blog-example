package ptest;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class performanceTest implements JavaSamplerClient {
	private SampleResult results;
	private String a;
	private String b;
	private String resultData;
	
	//设置传入的参数，可以设置多个,已设置的参数会显示到Jmeter的参数列表中
	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument("a", "0");          //设置参数，并赋予默认值0
		params.addArgument("b", "0");          //设置参数，并赋予默认值0
		
		return params;
	}
	
	//执行的执行体，根据线程数和循环次数的不同可执行多次
	@Override
	public SampleResult runTest(JavaSamplerContext arg0) {
		a = arg0.getParameter("a"); // 获取在Jmeter中设置的参数
		b = arg0.getParameter("b");

		try {
			results.sampleStart(); // 开始统计响应时间标记
			outputServer test = new outputServer();

			// 通过下面的操作就可以将被测方法的响应输出到Jmeter的察看结果树中的响应数据里面了。
			resultData = String.valueOf(test.output(Integer.parseInt(a), Integer.parseInt(b)));
			if (resultData != null && resultData.length() > 0) {
				results.setResponseData("结果是：" + resultData, null);
				results.setDataType(SampleResult.TEXT);
			
			
			}
			System.out.println(resultData);
			
			results.setSuccessful(true);
		} catch (Exception e) {
			results.setSuccessful(false);
			e.printStackTrace();
		} finally {
			results.sampleEnd(); // 结束统计响应时间标记
		}

		return results;
	}

	//初始化方法，实际运行时每个线程仅执行一次，在测试方法前执行
	@Override
	public void setupTest(JavaSamplerContext arg0) {
		results = new SampleResult();
	}

	//结束方法，实际运行时每个线程仅执行一次，在测试方法结束后执行
	@Override
	public void teardownTest(JavaSamplerContext arg0) {
		// TODO Auto-generated method stub
	}
	
/*  public static void main(String[] args) {
		Arguments params = new Arguments();
		params.addArgument("a", "1");          //设置参数，并赋予默认值0
		params.addArgument("b", "2");          //设置参数，并赋予默认值0
		JavaSamplerContext arg0 = new JavaSamplerContext(params);
		performanceTest test = new performanceTest();
		test.setupTest(arg0);
		test.runTest(arg0);
		test.teardownTest(arg0);	
	}*/
}
