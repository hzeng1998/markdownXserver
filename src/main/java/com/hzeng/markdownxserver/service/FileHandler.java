/*
 * Copyright (c) 2018.
 */

package com.hzeng.markdownxserver.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzeng.markdownxserver.config.Global;
import com.hzeng.markdownxserver.file.ChangeSet;
import com.hzeng.markdownxserver.file.Operation;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hzeng
 * @email hzeng1998@gmail.com
 * @date 2018/12/30 0:16
 */

@Service
public class FileHandler implements WebSocketHandler {

   // private static final Map<String, Map<String, WebSocketSession>> users;

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        System.out.println("connect succeed");
      //  String ID = webSocketSession.getUri().toString().split("ID=")[1];
        String ID = getClientID(webSocketSession);
        String FileID = getFileID(webSocketSession);
        System.out.println(FileID);
        if (ID != null) {

            Map<String, WebSocketSession> users;

            if (Global.getConnections().containsKey(FileID)) {
                users = Global.getConnections().get(FileID);
            } else {
                users = new HashMap<>();
                Global.getConnections().put(FileID, users);
            }

            users.put(ID, webSocketSession);

            if (Global.getChangeSetMap().get(FileID) == null)
                return;

            for (ChangeSet changeSet: Global.getChangeSetMap().get(FileID)) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("event", "pull");
                jsonObject.put("fileVersion", changeSet.getFileVerstion());
                jsonObject.put("message", JSON.toJSONString(changeSet.getOperations()));
                jsonObject.put("ID", changeSet.getUser());
                System.out.println(jsonObject.toJSONString());

                webSocketSession.sendMessage(new TextMessage(jsonObject.toJSONString()));
            }

            System.out.println("current collaborate:" + users.size());
        }
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {

        String payload = (String) webSocketMessage.getPayload();
        HashMap map = JSONObject.parseObject(payload, HashMap.class);

        JSONObject jsonObject = new JSONObject();

        if (map.containsKey("event") && map.get("event").equals("invite")) {
            System.out.println("invite code "  + getFileID(webSocketSession));
            jsonObject.put("event", "invite");
            jsonObject.put("code", getFileID(webSocketSession));
            webSocketSession.sendMessage(new TextMessage(jsonObject.toJSONString()));
        }

        if (map.containsKey("event") && map.get("event").equals("change")) {

            Map<String, ArrayList<ChangeSet>> changeSetMap = Global.getChangeSetMap();

            JSONArray jsonArray = JSON.parseArray((String) map.get("message"));
            ArrayList<Operation> operations = new ArrayList<>();
            for (Object o : jsonArray) {
                operations.add(JSON.parseObject(o.toString(), Operation.class));
            }

            ChangeSet changeSet = new ChangeSet(
                    operations,
                    (int) map.get("fileVersion"),
                    (String) map.get("ID"),
                    getFileID(webSocketSession));

            if (! changeSetMap.containsKey(getFileID(webSocketSession))) {
                changeSetMap.put(changeSet.getFileID(), new ArrayList<>());
            }

            synchronized (Global.getChangeSetMap()) {
                changeSet.transformitter();
                changeSet.insertChangeSet();
            }

            jsonObject.put("event", "change");
            jsonObject.put("fileVersion", changeSet.getFileVerstion());
            jsonObject.put("message", JSON.toJSONString(changeSet.getOperations()));
            jsonObject.put("ID", changeSet.getUser());

            System.out.println(jsonObject.toJSONString());

            boolean succeed = broadcastMessage(getFileID(webSocketSession), new TextMessage(jsonObject.toJSONString()));
            if (!succeed)
                System.out.println("Error!!!");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        if (webSocketSession.isOpen()) webSocketSession.close();
        System.out.println("connect error");
        Global.getConnections().get(getFileID(webSocketSession)).remove(getClientID(webSocketSession));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        System.out.println("connect closed:" + closeStatus);
        Global.getConnections().get(getFileID(webSocketSession)).remove(getClientID(webSocketSession));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public boolean sendMessage(String FileID, String clientID, TextMessage textMessage) {

        WebSocketSession session = Global.getConnections().get(FileID).get(clientID);

        if (session == null)
            return false;

        System.out.println("send message: " + session);
        if (!session.isOpen()) return false;
        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean broadcastMessage(String FileID, TextMessage textMessage) {

        boolean allSendSuccess = true;
        WebSocketSession webSocketSession;

        Map<String, WebSocketSession> users = Global.getConnections().get(FileID);

        for (String clientId : users.keySet()) {
            try {
                webSocketSession = users.get(clientId);
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(textMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                allSendSuccess = false;
            }
        }

        return allSendSuccess;
    }

    private String getClientID(WebSocketSession webSocketSession) {
        return (String) webSocketSession.getAttributes().get("USERID");
    }

    private String getFileID(WebSocketSession webSocketSession) {
        return (String) webSocketSession.getAttributes().get("CODE");
    }
}
