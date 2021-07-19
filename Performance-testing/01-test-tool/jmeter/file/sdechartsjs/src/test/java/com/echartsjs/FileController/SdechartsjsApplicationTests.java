package com.echartsjs.FileController;

import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Magic;
import com.github.abel533.echarts.code.Tool;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.feature.MagicType;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Line;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;

@SpringBootTest
@RunWith(SpringRunner.class)
class SdechartsjsApplicationTests {

    @Test
    void contextLoads() {
        GsonOption option = new GsonOption();
        option.title("某楼盘销售情况", "纯属虚构");
        option.tooltip().trigger(Trigger.axis);
        option.legend("意向", "预购", "成交");
        option.toolbox().show(true).feature(Tool.mark,
                Tool.dataView,
                new MagicType(Magic.line, Magic.bar, Magic.stack, Magic.tiled),
                Tool.restore,
                Tool.saveAsImage).padding(20);
        option.calculable(true);
        option.xAxis(new CategoryAxis().boundaryGap(false).data("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        option.yAxis(new ValueAxis());

        Line l1 = new Line("成交");
        l1.smooth(true).itemStyle().normal().areaStyle().typeDefault();
        l1.data(10, 12, 21, 54, 260, 830, 710);

        Line l2 = new Line("预购");
        l2.smooth(true).itemStyle().normal().areaStyle().typeDefault();
        l2.data(30, 182, 434, 791, 390, 30, 10);

        Line l3 = new Line("意向");
        l3.smooth(true).itemStyle().normal().areaStyle().typeDefault();
        l3.data(1320, 1132, 601, 234, 120, 90, 20);

        option.series(l1, l2, l3);

        System.out.println(option.toString());
//        option.exportToHtml("line3.html");
//        option.view();
    }

    static int length = 0x8FFFFFF; // 128 Mb

    @Test
    public void readJtl() throws IOException {

//        String jlt = "E:\\javaspoot\\接口测试\\20160904\\20160904\\资料\\testreport\\jtl\\TestReport.jtl";
        String jtl = "E:\\test\\7d\\jmeter\\ww.jtl";
        long l = readByFis(new File(jtl));
        System.out.println(l);

    }


    private static long readByFis(File file) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(file);
        byte[] buff = new byte[4096];
        long counts = 0;
        int offset = 0;
        while ((offset = is.read(buff)) != -1) {
            getJtl(new String(buff, 0, offset));
            counts = counts + offset;
        }
        is.close();
        return counts;
    }

    private static long readByBos(File file) throws FileNotFoundException, IOException {
        long counts = 0;
        int offset = 0;
        BufferedInputStream bos = new BufferedInputStream(new FileInputStream(file));
        byte[] buff = new byte[4096];
        while ((offset = bos.read(buff)) != -1) {
            counts = counts + offset;

            System.out.println(offset);

        }
        bos.close();
        return counts;
    }


    /**
     * 读取文件
     * @param lin
     */
    public static void getJtl(String lin) {
        String[] newlin = lin.split(",");
        System.out.println(newlin[0]);




    }


}
