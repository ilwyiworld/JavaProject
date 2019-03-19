package com.znv.fss.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionCheckUtil {
    public void getVersion() throws IOException {
        Properties properties = new Properties();
        InputStream in = this .getClass().getResourceAsStream( "/META-INF/maven/com.znv/fss-common/pom.properties" );
        properties.load(in);
        String str = properties.getProperty("version");
        System.out.println(str);
    }

    public static void main(String[] args) throws IOException {
        VersionCheckUtil versionCheckUtil = new VersionCheckUtil();
        versionCheckUtil.getVersion();
    }
}
