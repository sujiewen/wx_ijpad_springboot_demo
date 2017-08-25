package cn.channel8.mall.config;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayConfig {
	private static final Logger logger = LoggerFactory.getLogger(PayConfig.class);

	private static PayConfig instance;

	private Map<String, String> aliInfo = null;
	private Map<String, String> wxInfo = null;
	private Map<String, String> wxSubInfo = null;
	private Map<String, Object> openidMap = new HashMap<String, Object>();
	private Map<String, String> sidAndOpenidMap = new HashMap<String, String>();

	private PayConfig() {
		getAliConfiguration();
		getWXConfiguration();
	}

	public static PayConfig getInstance() { // 对获取实例的方法进行同步
		if (instance == null) {
			synchronized (PayConfig.class) {
				if (instance == null)
					instance = new PayConfig();
			}
		}
		return instance;
	}

	public Map<String, String> getAliConfiguration() {
		if (aliInfo == null) {
			aliInfo = new HashMap<String, String>();
			Properties tmp = getClientConfigProps("application.properties");
			String profiles_active = tmp.getProperty("spring.profiles.active");
			if (profiles_active.equals("dev")) {
				Properties tmp2 = getClientConfigProps("application-dev.properties");
				System.out.println(tmp2.getProperty("alipay.privateKey"));
				aliInfo.put("private_key", tmp2.getProperty("alipay.privateKey"));
				aliInfo.put("alipay_public_key", tmp2.getProperty("alipay.alipayPulicKey"));
				aliInfo.put("service_url", tmp2.getProperty("alipay.serverUrl"));
				aliInfo.put("app_id", tmp2.getProperty("alipay.appId"));
				aliInfo.put("notify_domain", tmp2.getProperty("alipay.notify_domain"));
			} else {
				Properties tmp2 = getClientConfigProps("application-prod.properties");
				aliInfo.put("private_key", tmp2.getProperty("alipay.privateKey"));
				aliInfo.put("alipay_public_key", tmp2.getProperty("alipay.alipayPulicKey"));
				aliInfo.put("service_url", tmp2.getProperty("alipay.serverUrl"));
				aliInfo.put("app_id", tmp2.getProperty("alipay.appId"));
				aliInfo.put("notify_domain", tmp2.getProperty("alipay.notify_domain"));
			}
		}
		return aliInfo;
	}

	public Map<String, String> getWXConfiguration() {
		if (wxInfo == null) {
			wxInfo = new HashMap<String, String>();
			Properties tmp = getClientConfigProps("application.properties");
			String profiles_active = tmp.getProperty("spring.profiles.active");
			if (profiles_active.equals("dev")) {
				Properties tmp2 = getClientConfigProps("application-dev.properties");
				wxInfo.put("appid", tmp2.getProperty("wxpay.appId"));
				wxInfo.put("mch_id", tmp2.getProperty("wxpay.mch_id"));
				wxInfo.put("partnerKey", tmp2.getProperty("wxpay.partnerKey"));
				wxInfo.put("notify_url", tmp2.getProperty("wxpay.domain"));
				wxInfo.put("wxpay_appSecret", tmp2.getProperty("wxpay.appSecret"));
				wxInfo.put("wxpay_certPath", tmp2.getProperty("wxpay.certPath"));
			} else {
				Properties tmp2 = getClientConfigProps("application-prod.properties");
				wxInfo.put("appid", tmp2.getProperty("wxpay.appId"));
				wxInfo.put("mch_id", tmp2.getProperty("wxpay.mch_id"));
				wxInfo.put("partnerKey", tmp2.getProperty("wxpay.partnerKey"));
				wxInfo.put("notify_url", tmp2.getProperty("wxpay.domain"));
				wxInfo.put("wxpay_appSecret", tmp2.getProperty("wxpay.appSecret"));
				wxInfo.put("wxpay_certPath", tmp2.getProperty("wxpay.certPath"));
			}
		}
		return wxInfo;
	}
	
	public Map<String , String> getWXSubConfiguration(){
		if(wxSubInfo == null) {
			wxSubInfo = new HashMap<String, String>();
			Properties tmp = getClientConfigProps("application.properties");
			String profiles_active = tmp.getProperty("spring.profiles.active");
			if (profiles_active.equals("dev")) {
				Properties tmp2 = getClientConfigProps("application-dev.properties");
				wxSubInfo.put("appid", tmp2.getProperty("wxsubpay.appId"));
				wxSubInfo.put("mch_id", tmp2.getProperty("wxsubpay.mch_id"));
				wxSubInfo.put("partnerKey", tmp2.getProperty("wxsubpay.partnerKey"));
				wxSubInfo.put("appSecret", tmp2.getProperty("wxsubpay.appSecret"));
				wxSubInfo.put("sub_appid", tmp2.getProperty("wxsubpay.sub_appid"));
				wxSubInfo.put("sub_mch_id", tmp2.getProperty("wxsubpay.sub_mch_id"));
				wxSubInfo.put("certPath", tmp2.getProperty("certPath"));
				wxSubInfo.put("notify_url", tmp2.getProperty("domain"));
			} else {
				Properties tmp2 = getClientConfigProps("application-prod.properties");
				wxSubInfo.put("appid", tmp2.getProperty("wxsubpay.appId"));
				wxSubInfo.put("mch_id", tmp2.getProperty("wxsubpay.mch_id"));
				wxSubInfo.put("partnerKey", tmp2.getProperty("wxsubpay.partnerKey"));
				wxSubInfo.put("appSecret", tmp2.getProperty("wxsubpay.appSecret"));
				wxSubInfo.put("sub_appid", tmp2.getProperty("wxsubpay.sub_appid"));
				wxSubInfo.put("sub_mch_id", tmp2.getProperty("wxsubpay.sub_mch_id "));
				wxSubInfo.put("certPath", tmp2.getProperty("certPath"));
				wxSubInfo.put("notify_url", tmp2.getProperty("domain"));
			}
		}
		return wxSubInfo;
	}
	
	public Properties getClientConfigProps(String path) {
		Properties config = null;
		String relativelyPath = System.getProperty("user.dir");
		relativelyPath = relativelyPath + path;
		File file = new File(relativelyPath);
		InputStream is = null;
		if (!file.exists()) {
			is = PayConfig.class.getClassLoader().getResourceAsStream(path);
		} else {
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.info("getClientConfigProps file is not exist", e);
				is = PayConfig.class.getClassLoader().getResourceAsStream(path);
			}
		}

		config = new Properties();
		try {
			config.load(is);

			// String strApiHost = config.getProperty("ecwx_api_host");
			// logger.info(strApiHost);

		} catch (IOException e) {
			logger.info("Could not load properties from " + path, e);
			return null;
		}

		return config;
	}
	
	public Object getWXUserInfo(String openid) {
		if(openid != null) 
			return openidMap.get(openid);
		else {
			return null;
		}
	}
	
	public void setWXUserInfo(String openid, Map<String, Object> userInfo) {
		openidMap.put(openid, openid);
	}
	
	public Object getOpenid(String sessionId) {
		if(sessionId != null) 
			return sidAndOpenidMap.get(sessionId);
		else {
			return null;
		}
	}
	
	public void setOpenid(String sessionId,String openid) {
		sidAndOpenidMap.put(sessionId, openid);
	}
}
