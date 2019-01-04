/*
 *  Copyright (c) 2018. All Rights Reserved.
 */

package com.hzeng.markdownxserver.file;

import lombok.Data;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Operation implements Serializable {

    public Operation(Method method, String value) {
        this.method = method;
        this.value = value;
    }

    private Method method;
    private String value;

    @Override
    public String toString() {
        return "Operation{" +
                "method=" + method +
                ", value='" + value + '\'' +
                '}';
    }

}
