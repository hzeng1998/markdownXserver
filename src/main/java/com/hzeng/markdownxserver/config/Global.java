/*
 * Copyright (c) 2019.
 */

package com.hzeng.markdownxserver.config;

import com.hzeng.markdownxserver.file.ChangeSet;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

/**
 * @author hzeng
 * @email hzeng1998@gmail.com
 * @date 2019/1/2 15:15
 */
public class Global {

    private static Map<String, Map<String, WebSocketSession>> connections;

    public static Map<String, Map<String, WebSocketSession>> getConnections() {
        return connections;
    }

    public static void setConnections(Map<String, Map<String, WebSocketSession>> connections) {
        Global.connections = connections;
    }

    private static Map<String, ArrayList<ChangeSet>> changeSetMap;

    public static Map<String, ArrayList<ChangeSet>> getChangeSetMap() {
        return changeSetMap;
    }

    public static void setChangeSetMap(Map<String, ArrayList<ChangeSet>> changeSetMap) {
        Global.changeSetMap = changeSetMap;
    }

    static {
        connections = new HashMap<>();
        changeSetMap = new HashMap<>();
    }
}