package com.techstar.Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController
@RequestMapping("/")
@Api(tags = "API文档",value = "/",description = "这是我全部的Get方法")
public class MyGetMethod {

    @GetMapping(value = "/getCookies")
    @ApiOperation(value = "通过这个方法可以获取到Cookies",httpMethod = "GET")
    public String getCookies(HttpServletResponse response){
        //HttpServerletRequest 装请求信息的类
        //HttpServerletResponse  装响应信息的类
        Cookie cookie = new Cookie("login","true");
        response.addCookie(cookie);
        return "恭喜zuozewei获得cookies信息成功";
    }

    /**
     * 要求客户端携带cookies访问
     * 这是一个需要携带cookies信息才能访问的get请求
     */
    @GetMapping(value = "/get/with/cookies")
    @ApiOperation(value = "通过这个方法可以获取到Cookies",httpMethod = "GET")
    public String getWithCookies(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)){
            return "必须携带cookies信息来";
        }
        for (Cookie cookie:cookies){
            if (cookie.getName().equals("login") && cookie.getValue().equals("true")){
                return "这是一个需要带着cookies信息才能访问的get请求";
            }
        }

        return "必须携带cookies信息来";
    }
}
