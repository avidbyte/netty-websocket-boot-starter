package io.github.avidbyte.controller;


import io.github.avidbyte.TextWebSocket;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Aaron
 * @since 2022-12-08 15:27
 */
@RestController
@RequestMapping("/test")
public class TestController {


    @Resource
    TextWebSocket textWebSocket;

    /**
     * redisTemplateUtil
     *
     * @param text text
     * @return CommonResult<Boolean>
     */
    @PostMapping("/message")
    public void redisTemplateUtil(@RequestParam("text") String text) {
        textWebSocket.sendMessageAll(text);
    }




}
