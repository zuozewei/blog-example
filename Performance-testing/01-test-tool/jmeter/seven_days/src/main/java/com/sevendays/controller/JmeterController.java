package com.sevendays.controller;


import com.sevendays.pojo.Msg;
import com.sevendays.service.JmerterScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author 7DGroup
 * @Title: JmeterController
 * @Description: Jmeter启动页面
 * @date 2019/11/17 / 10:32
 */

@Controller
@RequestMapping("/jmeter")
public class JmeterController {
    private static final Logger logger = LoggerFactory.getLogger(JmeterController.class);

    @Autowired
    JmerterScriptService jmerterScriptService;

    @GetMapping("/jmeterIndex")
    public String jmeterIndex() {
        return "jmeter/jmterIndex";
    }


    /**
     * 上传脚本
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public Msg upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Msg.fail().add("err", "上传失败");
        }
        String fileName = file.getOriginalFilename();
        logger.info("路径" + fileName);
        String filePath = "/home/7d/";
//        String filePath = "E:\\test\\7d\\data\\";
        if (!fileName.endsWith(".jmx")) {
            return Msg.fail().add("err", "脚本上传失败");
        }
        File dest = new File(filePath + fileName);

        String jmxName = fileName.substring(0, fileName.lastIndexOf("."));
        try {
            file.transferTo(dest);
            logger.info("上传成功:" + jmxName);
            return Msg.success().add("file", jmxName);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
        return Msg.fail();
    }

    /**
     * 上传参数文件
     *
     * @param file
     * @return
     */
    @PostMapping("/Paramupload")
    @ResponseBody
    public Msg uploadParam(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Msg.fail().add("err", "上传失败");
        }
        String fileName = file.getOriginalFilename();
        logger.info("路径" + fileName);
        String filePath = "/home/7d";
//        String filePath = "E:\\test\\7d\\data\\";
        File dest = new File(filePath + fileName);
        String jmxName = fileName.substring(0, fileName.lastIndexOf("."));

        try {
            file.transferTo(dest);
            logger.info("上传成功:" + jmxName);
            return Msg.success().add("file", jmxName);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
        return Msg.fail();
    }


    /**
     * 运行脚本
     *
     * @return
     */
    @PostMapping("/JmeterRun")
    @ResponseBody
    public Msg run(@RequestParam("jmeterName") String jmeterName, @RequestParam("numberName") String numberName, @RequestParam("duration") String duration) {
        logger.info(jmeterName);
        if (!jmeterName.isEmpty() && !numberName.isEmpty()) {
            jmerterScriptService.runCommand(jmeterName.trim(), numberName.trim(), duration);
            return Msg.success();
        } else {
            return Msg.fail();
        }
    }

    /**
     * 停止脚本
     *
     * @return
     */
    @GetMapping("/JmeterStop")
    @ResponseBody
    public Msg stop() {
        jmerterScriptService.stopCommand();
        return Msg.success();
    }


    /**
     * 查看日志
     *
     * @return
     */
    @GetMapping("/Jmeterinfo")
    @ResponseBody
    public Msg info() {
        String info = jmerterScriptService.selectInfo();
        return Msg.success().add("infopage", info);
    }
}
