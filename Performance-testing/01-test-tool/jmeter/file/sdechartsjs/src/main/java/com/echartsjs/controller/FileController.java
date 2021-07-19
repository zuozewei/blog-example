package com.echartsjs.controller;

import com.echartsjs.pojo.Msg;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author 7d
 * @Title: FileController
 * @Description: 文件操作类
 * @date 2019/12/13 / 21:00
 */
@Controller
@RequestMapping("/file/")
public class FileController {


    /**
     * 文件上传
     *
     * @param fileupload 文件
     * @return msg
     */
    @PostMapping("fileupload")
    @ResponseBody
    public Msg upload(@RequestParam("fileupload") MultipartFile fileupload) {

        if (fileupload.isEmpty() || fileupload.getSize() < 0) {
            return Msg.fail().add("mgs", "文件为空,上传失败！");
        }
        // 获取文件名字
        String fileName = fileupload.getOriginalFilename();
        // 获取后缀名
        // String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 文件保存路径
        String filePath = "E:\\test\\7d\\upload\\";
        // 文件重命名，防止重复
        fileName = filePath + UUID.randomUUID() + fileName;
        // 文件对象
        File dest = new File(fileName);
        // 判断路径是否存在，如果不存在则创建
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            // 保存到服务器中
            fileupload.transferTo(dest);
            return Msg.success().add("mgs", "文件上传成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Msg.fail().add("mgs", "文件上传失败");
    }


    /**
     * 文件下载
     *
     * @param name     下载文件名字
     * @param response 响应流
     * @return mgs
     * @throws Exception 异常处理
     */
    @GetMapping("download")
    public void download(@RequestParam("filedown") String name, HttpServletResponse response) throws Exception {
        if (name.isEmpty()) {
            return;
        }
        // 文件地址，真实环境是存放在数据库表中
        File file = new File("E:\\test\\7d\\upload\\" + name);
        //判断文件是否存在
        if (!file.exists()) {
            return;
        }
        // 文件对象输入流
        FileInputStream fis = new FileInputStream(file);
        // 设置相关格式
        response.setContentType("application/force-download");
        // 设置下载后的文件名以及header
        response.addHeader("Content-disposition", "attachment;fileName=" + name);
        // 创建输出对象
        OutputStream os = response.getOutputStream();
        // 常规操作
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = fis.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
        fis.close();
        return;
    }

}
