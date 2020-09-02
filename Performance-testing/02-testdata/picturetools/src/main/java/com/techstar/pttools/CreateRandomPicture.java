package com.techstar.pttools;

import com.sun.glass.ui.Size;

import javax.imageio.ImageIO;
import javax.naming.Name;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CreateRandomPicture {
    /***
     * 	 * 创建一张图片
     * 	 * 图片存放路径：D:\VBI5\testpictures\image.png
     * 	 * 1.循环创建批量图片
     * 	 * 2.将图片写到磁盘里
     * 	 * @author zuozewei
     * 	 * @throws IOException
     * 	 * @date 2018-8-16
     * 	 *
     */
    public static void main(String[] args) throws IOException {
        //图片文字
        String s = "测试图片";
        for (int x = 1; x <= 300; x++) {
            creatPicture(s + x, x);
        }
    }

    public static void creatPicture(String s, int num) throws IOException {
        int width;  //图片宽度
        int height; //图片高度

        //图片存储目录
        File file = new File("D:\\VBI5\\testpictures\\test\\image" + num + ".png");
        //设置文字属性
        Font font = new Font("Serif", Font.BOLD, 10);
        //BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //获取本地原图
        File picture = new File("D:\\VBI5\\testpictures/广州地铁.png");
        BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));

        width = sourceImg.getWidth();  //原图宽度
        height = sourceImg.getHeight(); //原图高度

        Graphics2D g2 = (Graphics2D) sourceImg.getGraphics();
        //g2.setBackground(Color.ORANGE);
        //g2.clearRect(0, 0, width, height);
        //设置文字颜色
        g2.setPaint(Color.yellow);
        FontRenderContext context = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(s, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;

        g2.drawString(s, (int) x, (int) baseY);
        //文件流写入磁盘
        ImageIO.write(sourceImg, "png", file);
    }

}
