/*
 * Copyright (c) 2018.
 */

package com.hzeng.markdownxserver.service;

import com.hzeng.markdownxserver.util.UUIDHexGenerator;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author hzeng
 * @email hzeng1998@gmail.com
 * @date 2018/12/30 11:18
 */
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String ID = servletRequest.getServletRequest().getParameter("ID");
            String inviteCode = servletRequest.getServletRequest().getParameter("code");
            if (ID == null) {
                return false;
            }
            if (inviteCode == null || inviteCode.equals("null") || inviteCode.equals("")) {
                inviteCode = new UUIDHexGenerator().generate();
            }
            attributes.put("USERID", ID);
            attributes.put("CODE", inviteCode);
        }
        return true;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}