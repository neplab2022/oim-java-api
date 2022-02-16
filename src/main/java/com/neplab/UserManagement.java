package com.neplab;

import java.io.IOException;
import java.util.Properties;

public class UserManagement {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(UserManagement.class.getClassLoader().getResourceAsStream("application.properties"));
        System.out.println(properties.getProperty("oim_username"));
        System.out.println(properties.getProperty("oim_pass"));
        System.out.println(properties.getProperty("oim_url"));
        System.out.println(properties.getProperty("authwl_path"));
        System.out.println(Config.OIM_USERNAME);
    }
}
