/*
 * Copyright (c) 2018.
 */

package com.hzeng.markdownxserver.config;

import com.hzeng.markdownxserver.service.FileHandler;
import com.hzeng.markdownxserver.service.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author hzeng
 * @email hzeng1998@gmail.com
 * @date 2018/12/30 0:27
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(changeHandler(), "changeHandler/").setAllowedOrigins("*").addInterceptors(new WebSocketInterceptor());
    }

    public WebSocketHandler changeHandler() {
        return new FileHandler();
    }

}
