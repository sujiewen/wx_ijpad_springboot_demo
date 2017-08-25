package cn.channel8.mall.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.Gson;


@Controller
@RequestMapping(value = "/mall")
public class MallInterfaceController {

	private static Logger logger = LoggerFactory.getLogger(MallInterfaceController.class);

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String home() {
		logger.debug("home");
		return "home";
	}

	@RequestMapping(value = "/")
	public String test() {
		return "MallList";
	}

	@RequestMapping(value = "/test")
	public String testML() {
		return "MallDemo";
	}

	@RequestMapping(value = "/wxpay")
	public String wxpay() {
		logger.debug("test");
		return "pay/wxpay";
	}

	@RequestMapping(value = "/wxsubpay")
	public String wxsubpay() {
		logger.debug("test");
		return "pay/wxsubpay";
	}

	@RequestMapping(value = "/success")
	public String success() {
		logger.debug("test");
		return "error/success";
	}

	@RequestMapping(value = "/qrcode")
	public String qrcode() {
		logger.debug("test");
		// 返回路径
		return "pay/qrcode";
	}
	
	@RequestMapping(value = "/alipay")
	public String alipay() {
		return "pay/alipay";
	}
	
	
	//下载页面
	@RequestMapping(value = "/download")
	public String download() {
		return "pay/mjdownload";
	}
	


	/**
	 * 获取商品类别
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/typeList", method = RequestMethod.POST)
	public String merchantTypeList(@RequestParam(value = "merchantId") long merchantId) {

		String str = "";

		return str;
	}

}
