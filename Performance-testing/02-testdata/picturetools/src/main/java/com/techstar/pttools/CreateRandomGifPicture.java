package com.techstar.pttools;


import com.gif4j.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CreateRandomGifPicture {
    /***
     * 	 * 创建一张带水印的GIF图片
     * 	 * 图片存放路径：D:\VBI5\testpictures\testgif\image.gif
     * 	 * 1.循环创建批量GIF图片
     * 	 * 2.将GIF图片写到磁盘里
     * 	 * @author zuozewei
     * 	 * @throws IOException
     * 	 * @date 2018-8-16
     * 	 *
     */
    public static void main(String[] args) throws IOException {
        //图片文字
        String s = "测试图片";

        //需要添加水印的图片路径
        String path = "D:\\VBI5\\testpictures\\testgifsrc\\test.gif";
        File file = new File(path);

        //循环调用加水印方法
        for (int x=1;x<=20;x++){
            addTextWatermarkToGif(file, s+x, new File("D:\\VBI5\\testpictures\\testgif\\" +"testgif"+x+".gif"));
        }

    }


    public static void addTextWatermarkToGif(File src, String watermarkText, File dest)throws IOException {

        //水印初始化、设置（字体、样式、大小、颜色）
        TextPainter textPainter = new TextPainter(new Font("黑体", Font.ITALIC, 12));
        textPainter.setOutlinePaint(Color.WHITE);
        BufferedImage renderedWatermarkText = textPainter.renderString(watermarkText, true);

        //图片对象
        GifImage gf = GifDecoder.decode(src);

        //获取图片大小
        int iw = gf.getScreenWidth();
        int ih = gf.getScreenHeight();

        //获取水印大小
        int tw = renderedWatermarkText.getWidth();
        int th = renderedWatermarkText.getHeight();

        //水印位置
        Point p = new Point();
        p.x = iw - tw - 5;
        p.y = ih - th - 4;

        //加水印
        Watermark watermark = new Watermark(renderedWatermarkText, p);
        gf = watermark.apply(GifDecoder.decode(src), true);

        //输出
        GifEncoder.encode(gf, dest);
    }

}
