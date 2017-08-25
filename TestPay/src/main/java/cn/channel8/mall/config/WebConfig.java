package cn.channel8.mall.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.channel8.mall.MallApplication;


public class WebConfig {
	private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
	public String rwsourceUrl="/img/";
	public String resourceLocations="file:///F:/img/";
	private static class NewsConfigHolder{
		private static final WebConfig InSTANCE = new WebConfig();
	}
	
	private WebConfig(){
		getConfig();
	};
	public static final WebConfig getInstance(){
		return NewsConfigHolder.InSTANCE;
	}
	
    private void getConfig(){
    	String relativelyPath = System.getProperty("user.dir");
    	relativelyPath = relativelyPath  + "application.properties";
    	File file = new File(relativelyPath);
    	InputStream is = null;
    	if (!file.exists()) {
    		is = MallApplication.class.getClassLoader().getResourceAsStream("application.properties");
    	} else {
    		try {
    			is = new FileInputStream(file);
    		} catch (FileNotFoundException e) {
    			logger.info("getClientConfigProps file is not exist", e);
    			is = MallApplication.class.getClassLoader().getResourceAsStream("application.properties");
    		}
    	}
    	Properties config = new Properties();
    	try {
    		config.load(is);
    	} catch (IOException e) {
    		logger.info("Could not load properties from " + "application.properties", e);
    	}
    	
   
    	try {
    		rwsourceUrl = config.getProperty("resourceUrl");
    		resourceLocations=config.getProperty("resourceLocations");
    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    } 

}
