package cn.channel8.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication
@PropertySource(value = { "classpath:application-${spring.profiles.active}.properties",
		"file:config/application-${spring.profiles.active}.properties" }, ignoreResourceNotFound = true)
public class MallApplication {
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx =  SpringApplication.run(MallApplication.class, args);
	}
}


