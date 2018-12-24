package com.zuozewei.springbootmail.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author zuozewei
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
	@Ignore
    public void testSimpleMail() throws Exception {
        mailService.sendSimpleMail("zuozewei@hotmail.com","test simple mail"," hello this is simple mail");
    }

    @Test
    public void testHtmlMail() throws Exception {
        String content="<html>\n" +
                "<body>\n" +
                "    <h3>hello world ! 这是一封html邮件!</h3>\n" +
                "</body>\n" +
                "</html>";
        mailService.sendHtmlMail("zuozewei@hotmail.com","test simple mail",content);
    }

    @Test
	@Ignore
    public void sendAttachmentsMail() {
        String filePath="./test-output/index.html";
        mailService.sendAttachmentsMail("zuozewei@hotmail.com", "主题：带附件测试报告的邮件", "有附件测试报告，请查收！", filePath);
    }


    @Test
	@Ignore
    public void sendInlineResourceMail() {
        String rscId = "test";
        String content="<html><body>这是有图片的邮件：<img src=\'cid:" + rscId + "\' ></body></html>";
        String imgPath = "/Users/apple/Downloads/图片/ads-beverage-black-coffee-33972.jpg";

        mailService.sendInlineResourceMail("zuozewei@hotmail.com", "主题：这是有图片的邮件", content, imgPath, rscId);
    }


    @Test
    public void sendTemplateMail() {
        //创建邮件正文
        Context context = new Context();
        context.setVariable("id", "zuozewei");
        String emailContent = templateEngine.process("emailable-report.html", context);

        mailService.sendHtmlMail("zuozewei@hotmail.com","主题：这是模板邮件",emailContent);
    }
}
