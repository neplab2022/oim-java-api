package com.neplab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.security.auth.login.LoginException;
import com.thortech.util.logging.Logger;
import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Operations.TaskDefinitionOperationsIntf;
import Thor.API.Operations.tcProvisioningOperationsIntf;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;

public class Config {
    public static final Logger logger = Logger.getLogger(Config.class.getName());
    OIMClient oimClient = null;
    static Properties props = new Properties();

    // OIM Connection Details
    public static final String OIM_USERNAME = props.getProperty("oim_username");
    public static final String OIM_PASSWORD = props.getProperty("oim_pass");
    public static final String OIM_URL = props.getProperty("oim_url"); // OIM HostName and Port
    public static final String AUTHWL_PATH = props.getProperty("authwl_path"); // eg. D:\\authwl.conf

//    Properties properties = ReadProperties.loadPropertiesFile("oim.properties");
    // Retry task details
    public static final String APPINST_NAME = "skillsoftsst";
    //public static final String TASK_NAME = "HR emplid Updated";
    public static final String TASK_NAME = "Change HR emplid";
    public static final String ACCOUNT_STATUS = "Provisioned"; // Trigger task only for accounts in Provisioned state


    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getOIMConnection() {
        System.out.println("getOIMConnection() : Start");
        // set system properties
        System.setProperty("java.security.auth.login.config", AUTHWL_PATH);
        System.setProperty("OIM.AppServerType", "wls");
        System.setProperty("APPSERVER_TYPE", "wls");
        Hashtable oimenv = new Hashtable();
        oimenv.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
        oimenv.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIM_URL);
        oimClient = new OIMClient(oimenv);
        try {
            oimClient.login(OIM_USERNAME, OIM_PASSWORD.toCharArray());
            System.out.println("Connected");
            logger.debug("Connected");

        } catch (LoginException e) {
            e.printStackTrace();
        }
        System.out.println("getOIMConnection() : End");
    }
}
