/**
  * @Title: WebServerConfiguration.java
  * @Package cn.party1.oauth2service.desk.config
  * @Description: TODO
  * Copyright: Copyright (c) 2016 
  * Company:杭州一聚会科技有限公司
  * 
  * @author kahntang-kahn
  * @date 2016年12月5日 下午3:06:09
  * @version V1.0
  * @since
  */
package cn.channel8.mall.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
   * @author kahntang-kahn
   * @date 2016年12月5日 下午3:06:09
   * @version 1.0
   */
@Configuration
public class WebServerConfiguration {

	@Bean  
    public EmbeddedServletContainerFactory createEmbeddedServletContainerFactory()  
    {  
        TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();  
//        tomcatFactory.setPort(8081);  
        tomcatFactory.addConnectorCustomizers(new MyTomcatConnectorCustomizer());  
        return tomcatFactory;  
    }  
}


class MyTomcatConnectorCustomizer implements TomcatConnectorCustomizer  
{  
    public void customize(Connector connector)  
    {  
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();  
        //设置最大连接数  
        protocol.setMaxConnections(1000);  
        //设置最大线程数  
        protocol.setMaxThreads(1000);  
        protocol.setConnectionTimeout(30000);  
    }  
}  