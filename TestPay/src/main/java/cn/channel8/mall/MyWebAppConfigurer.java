package cn.channel8.mall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import cn.channel8.mall.config.WebConfig;


@Configuration
public class MyWebAppConfigurer extends WebMvcConfigurerAdapter {

	
	private static Logger logger = LoggerFactory.getLogger(MyWebAppConfigurer.class);
	 @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		 
		 registry.addResourceHandler(WebConfig.getInstance().rwsourceUrl+"**").addResourceLocations(WebConfig.getInstance().resourceLocations);
	        super.addResourceHandlers(registry);
	    }

}
