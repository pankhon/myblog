package com.mtons.mblog;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**

 */
public class Base64Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] keys = "mtons".getBytes("UTF-8");
        System.out.println(Base64Utils.encodeToString(Arrays.copyOf(keys, 16)));

        String src = "/static/";
        if (StringUtils.isNoneBlank(src) && src.length() > 1) {
            if (src.startsWith("/")) {
                src = src.substring(1);
            }

            if (!src.endsWith("/")) {
                src = src + "/";
            }
        }
        System.out.println(src);

        System.out.println("sg[hide]test[/hide]<asf>fsd</sdf>".replaceAll("\\[hide\\]([\\s\\S]*)\\[\\/hide\\]", "$1"));
    }
}
