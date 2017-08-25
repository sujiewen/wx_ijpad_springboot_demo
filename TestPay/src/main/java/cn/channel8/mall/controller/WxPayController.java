package cn.channel8.mall.controller;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jfinal.kit.PathKit;
import com.jpay.ext.kit.HttpKit;
import com.jpay.ext.kit.IpKit;
import com.jpay.ext.kit.PaymentKit;
import com.jpay.ext.kit.StrKit;
import com.jpay.ext.kit.ZxingKit;
import com.jpay.vo.AjaxResult;
import com.jpay.weixin.api.WxPayApi;
import com.jpay.weixin.api.WxPayApi.TradeType;
import com.jpay.weixin.api.WxPayApiConfig;
import com.jpay.weixin.api.WxPayApiConfig.PayModel;

import cn.channel8.mall.config.PayConfig;
import cn.channel8.mall.config.WebConfig;
import cn.channel8.mall.utils.GsonUtil;

import com.jpay.weixin.api.WxPayApiConfigKit;

/**
 * @author Javen 商户模式下微信支付
 */
@Controller
@RequestMapping(value = "/wxpay")
public class WxPayController {
	private static Logger logger = LoggerFactory.getLogger(MallInterfaceController.class);
	private AjaxResult ajax = new AjaxResult();
	// 商户相关资料
	String appid = "";// prop.get("appId");
	String mch_id = "";// prop.get("mch_id");
	String partnerKey = "";// prop.get("partnerKey");
	String notify_url = "";// prop.get("domain")+"/wxpay/pay_notify";

	WxPayController() {
		Map<String, String> map = PayConfig.getInstance().getWXConfiguration();
		appid = map.get("appid");
		mch_id = map.get("mch_id");
		partnerKey = map.get("partnerKey");
		notify_url = map.get("notify_url") + "/wxpay/pay_notify";
	}

	public WxPayApiConfig getApiConfig() {
		WxPayApiConfig apiConfig = WxPayApiConfig.New().setAppId(appid).setMchId(mch_id).setPaternerKey(partnerKey)
				.setPayModel(PayModel.BUSINESSMODEL);
		WxPayApiConfigKit.setThreadLocalWxPayApiConfig(apiConfig);
		return apiConfig;
	}

	@RequestMapping(value = "/")
	@ResponseBody
	public String index() {
		logger.info("欢迎使用IJPay,商户模式下微信支付 - by Javen");
		return ("欢迎使用IJPay 商户模式下微信支付  - by Javen");
	}

	/**
	 * 公众号支付
	 * 
	 * @TODO 待测试 o5NJx1dVRilQI6uUVSaBDuLnM3iM
	 */
	@RequestMapping(value = "/webpay",method = { RequestMethod.POST },produces="application/json;charset=UTF-8")
	@ResponseBody
	public String webPay(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "total_fee") String total_fee) {

		// openId，采用 网页授权获取 access_token API：SnsAccessTokenApi获取
		// String openId = "";//(String) getSession().getAttribute("openId");
//		response.addHeader("Access-Control-Allow-Origin", "*");
//		response.setHeader("Content-Type", "application/json;charset=UTF-8");

		HttpSession httpSession = request.getSession();

		String strResult = "";

		String openId = (String) httpSession.getAttribute("openId");
		
		if(openId == null) {
			
		}

		if (openId != null) {
			try {

				if (StrKit.isBlank(openId)) {
					ajax.addError("openId is null");
					return GsonUtil.toJson(ajax, false);
				}
				if (StrKit.isBlank(total_fee)) {
					ajax.addError("请输入数字金额");
					return GsonUtil.toJson(ajax, false);
				}

				String ip = IpKit.getRealIp(request);
				if (StrKit.isBlank(ip)) {
					ip = "127.0.0.1";
				}

				this.getApiConfig();
				Map<String, String> params = WxPayApiConfigKit.getWxPayApiConfig().setAttach("IJPay 公众号支付测试  -By Javen")
						.setBody("IJPay 公众号支付测试  -By Javen").setOpenId(openId).setSpbillCreateIp(ip)
						.setTotalFee(total_fee).setTradeType(TradeType.JSAPI).setNotifyUrl(notify_url)
						.setOutTradeNo(String.valueOf(System.currentTimeMillis())).build();

				logger.info(params.toString());
				String xmlResult = WxPayApi.pushOrder(false, params);
				logger.info(xmlResult);
				Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

				String return_code = result.get("return_code");
				String return_msg = result.get("return_msg");
				if (!PaymentKit.codeIsOK(return_code)) {
					ajax.addError(return_msg);
					return GsonUtil.toJson(ajax, false);
				}
				String result_code = result.get("result_code");
				if (!PaymentKit.codeIsOK(result_code)) {
					ajax.addError(return_msg);
					return GsonUtil.toJson(ajax, false);
				}
				// 以下字段在return_code 和result_code都为SUCCESS的时候有返回
				String prepay_id = result.get("prepay_id");

				Map<String, String> packageParams = PaymentKit.prepayIdCreateSign(prepay_id);

				String jsonStr = GsonUtil.toJson(packageParams, false);
				ajax.success(jsonStr);
				strResult = GsonUtil.toJson(ajax, false);
			} catch (Exception e) {
				strResult = "{\"success\":2,\"msg\":\"异常\"}";
			}
		} else {
			strResult = "{\"success\":1,\"msg\":\"openId 为空\"}";
		}

		return strResult;
	}

	/**
	 * 生成支付二维码（模式一）并在页面上显示 已测试
	 */
	@RequestMapping(value = "/scanCode1")
	@ResponseBody
	public String scanCode1(HttpServletRequest request, @RequestParam(value = "product_id") String product_id) {
		try {
			if (StrKit.isBlank(product_id)) {
				ajax.addError("productId is null");
				return GsonUtil.toJson(ajax, false);
			}
			WxPayApiConfig config = this.getApiConfig();
			// 获取扫码支付（模式一）url
			String qrCodeUrl = WxPayApi.getCodeUrl(config.getAppId(), config.getMchId(), product_id,
					config.getPaternerKey(), true);
			logger.info(qrCodeUrl);
			// 生成二维码保存的路径
			String name = "payQRCode1.png";
			String path = request.getContextPath();
			String imgCodePath = PathKit.getWebRootPath() + WebConfig.getInstance().rwsourceUrl + name;
			System.out.println(imgCodePath);
			// 图片路径不能变动 无法获取
			Boolean encode = ZxingKit.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 3, ErrorCorrectionLevel.H, "png", 200,
					200, imgCodePath);
			if (encode) {
				// 在页面上显示
				ajax.success(name);
				return GsonUtil.toJson(ajax, false);
			}
		} catch (Exception e) {

			e.printStackTrace();

			logger.error("scanCode1 =" + e.getLocalizedMessage());
			ajax.addError("系统异常：" + e.getMessage());
			return GsonUtil.toJson(ajax, false);
		}
		return "";
	}

	/**
	 * 扫码支付模式一回调 已测试
	 */
	@RequestMapping(value = "/wxpay")
	@ResponseBody
	public String wxpay(HttpServletRequest request, HttpServletResponse response) {
		
		

		String strResult = "";

		try {

			String result = HttpKit.readData(request);
			System.out.println("callBack_xml>>>" + result);
			/**
			 * 获取返回的信息内容中各个参数的值
			 */
			Map<String, String> map = PaymentKit.xmlToMap(result);
			for (String key : map.keySet()) {
				System.out.println("key= " + key + " and value= " + map.get(key));
			}

			String appid = map.get("appid");
			String openid = map.get("openid");
			String mch_id = map.get("mch_id");
			String is_subscribe = map.get("is_subscribe");
			String nonce_str = map.get("nonce_str");
			String product_id = map.get("product_id");
			String sign = map.get("sign");
			Map<String, String> packageParams = new HashMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("openid", openid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("is_subscribe", is_subscribe);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("product_id", product_id);

			String packageSign = PaymentKit.createSign(packageParams,
					WxPayApiConfigKit.getWxPayApiConfig().getPaternerKey());
			// 统一下单文档地址：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1

			String ip = IpKit.getRealIp(request);
			if (StrKit.isBlank(ip)) {
				ip = "127.0.0.1";
			}

			this.getApiConfig();
			Map<String, String> params = WxPayApiConfigKit.getWxPayApiConfig().setAttach("IJPay 扫码模式一测试  -By Javen")
					.setBody("IJPay 扫码模式一测试  -By Javen").setOpenId(openid).setSpbillCreateIp(ip).setTotalFee("100")
					.setTradeType(TradeType.NATIVE).setNotifyUrl(notify_url)
					.setOutTradeNo(String.valueOf(System.currentTimeMillis())).build();

			String xmlResult = WxPayApi.pushOrder(false, params);
			logger.info("prepay_xml>>>" + xmlResult);

			/**
			 * 发送信息给微信服务器
			 */
			Map<String, String> payResult = PaymentKit.xmlToMap(xmlResult);

			String return_code = payResult.get("return_code");
			String result_code = payResult.get("result_code");

			if (StrKit.notBlank(return_code) && StrKit.notBlank(result_code) && return_code.equalsIgnoreCase("SUCCESS")
					&& result_code.equalsIgnoreCase("SUCCESS")) {
				// 以下字段在return_code 和result_code都为SUCCESS的时候有返回
				String prepay_id = payResult.get("prepay_id");

				Map<String, String> prepayParams = new HashMap<String, String>();
				prepayParams.put("return_code", "SUCCESS");
				prepayParams.put("appId", appid);
				prepayParams.put("mch_id", mch_id);
				prepayParams.put("nonceStr", System.currentTimeMillis() + "");
				prepayParams.put("prepay_id", prepay_id);
				String prepaySign = null;
				if (sign.equals(packageSign)) {
					prepayParams.put("result_code", "SUCCESS");
				} else {
					prepayParams.put("result_code", "FAIL");
					prepayParams.put("err_code_des", "订单失效"); // result_code为FAIL时，添加该键值对，value值是微信告诉客户的信息
				}
				prepaySign = PaymentKit.createSign(prepayParams,
						WxPayApiConfigKit.getWxPayApiConfig().getPaternerKey());
				prepayParams.put("sign", prepaySign);
				String xml = PaymentKit.toXml(prepayParams);
				logger.info(xml);
				strResult = xml;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strResult;
	}

	/**
	 * 扫码支付模式二 已测试
	 */
	@RequestMapping(value = "/scanCode2")
	@ResponseBody
	public String scanCode2(HttpServletRequest request, @RequestParam(value = "total_fee") String total_fee) {

		// String openId="o5NJx1dVRilQI6uUVSaBDuLnM3iM";
		String openId = "";// (String) getSession().getAttribute("openId");

		if (StrKit.isBlank(openId)) {
			ajax.addError("openId is null");
			return GsonUtil.toJson(ajax, false);
		}
		if (StrKit.isBlank(total_fee)) {
			ajax.addError("支付金额不能为空");
			return GsonUtil.toJson(ajax, false);
		}

		String ip = IpKit.getRealIp(request);
		if (StrKit.isBlank(ip)) {
			ip = "127.0.0.1";
		}
		this.getApiConfig();
		Map<String, String> params = WxPayApiConfigKit.getWxPayApiConfig().setAttach("IJPay 测试  -By Javen")
				.setBody("IJPay 扫码支付2测试  -By Javen").setOpenId(openId).setSpbillCreateIp(ip).setTotalFee(total_fee)
				.setTradeType(TradeType.NATIVE).setNotifyUrl(notify_url)
				.setOutTradeNo(String.valueOf(System.currentTimeMillis())).build();

		String xmlResult = WxPayApi.pushOrder(false, params);

		logger.info(xmlResult);
		Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

		String return_code = result.get("return_code");
		String return_msg = result.get("return_msg");
		if (!PaymentKit.codeIsOK(return_code)) {
			System.out.println(return_msg);

			return GsonUtil.toJson(xmlResult, false);
		}
		String result_code = result.get("result_code");
		if (!PaymentKit.codeIsOK(result_code)) {
			System.out.println(return_msg);
			return GsonUtil.toJson(xmlResult, false);

		}
		// 生成预付订单success

		String qrCodeUrl = result.get("code_url");
		String name = "payQRCode2.png";
		String imgCodePath = PathKit.getWebRootPath() + WebConfig.getInstance().rwsourceUrl + name;
		System.out.println(imgCodePath);

		Boolean encode = ZxingKit.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 3, ErrorCorrectionLevel.H, "png", 200, 200,
				imgCodePath);
		if (encode) {
			// renderQrCode(qrCodeUrl, 200, 200);
			// 在页面上显示
			ajax.success(name);
			return GsonUtil.toJson(ajax, false);
		} else {
			return "";
		}
	}

	/**
	 * 刷卡支付 已测试
	 */
	@RequestMapping(value = "/micropay")
	@ResponseBody
	public String micropay(HttpServletRequest request, @RequestParam(value = "auth_code") String auth_code,
			@RequestParam(value = "total_fee") String total_fee) {

		if (StrKit.isBlank(total_fee)) {
			ajax.addError("支付金额不能为空");
			return GsonUtil.toJson(ajax, false);
		}
		if (StrKit.isBlank(auth_code)) {
			ajax.addError("auth_code参数错误");
			return GsonUtil.toJson(ajax, false);
		}

		String ip = IpKit.getRealIp(request);
		if (StrKit.isBlank(ip)) {
			ip = "127.0.0.1";
		}
		this.getApiConfig();
		Map<String, String> params = WxPayApiConfigKit.getWxPayApiConfig().setAttach("IJPay 测试  -By Javen")
				.setBody("IJPay 刷卡支付测试 -By Javen").setSpbillCreateIp(ip).setTotalFee(total_fee).setAuthCode(auth_code)
				.setTradeType(TradeType.MICROPAY).setNotifyUrl(notify_url)
				.setOutTradeNo(String.valueOf(System.currentTimeMillis())).build();

		String xmlResult = WxPayApi.micropay(false, params);

		// 同步返回结果
		logger.info("xmlResult:" + xmlResult);

		Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
		String return_code = result.get("return_code");
		String return_msg = result.get("return_msg");
		if (!PaymentKit.codeIsOK(return_code)) {
			// 通讯失败
			String err_code = result.get("err_code");
			// 用户支付中，需要输入密码
			if (err_code.equals("USERPAYING")) {
				// 等待5秒后调用【查询订单API】https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_2

			}
			logger.info("提交刷卡支付失败>>" + xmlResult);
			ajax.addError(return_msg);
			return GsonUtil.toJson(ajax, false);
		}

		String result_code = result.get("result_code");
		if (!PaymentKit.codeIsOK(result_code)) {
			// 支付失败
			// 支付失败
			logger.info("支付失败>>" + xmlResult);
			String err_code_des = result.get("err_code_des");

			ajax.addError(err_code_des);
			return GsonUtil.toJson(ajax, false);
		}

		// 支付成功

		return (xmlResult);
	}

	/**
	 * 微信APP支付 已测试
	 */
	@RequestMapping(value = "/appPay")
	@ResponseBody
	public String appPay(HttpServletRequest request, HttpServletResponse response) {
		
		// 不用设置授权目录域名
		// 统一下单地址 https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1#

		String ip = IpKit.getRealIp(request);
		if (StrKit.isBlank(ip)) {
			ip = "127.0.0.1";
		}
		this.getApiConfig();
		Map<String, String> params = WxPayApiConfigKit.getWxPayApiConfig().setAttach("IJPay 测试  -By Javen")
				.setBody("IJPay App付测试  -By Javen").setSpbillCreateIp(ip).setTotalFee("100")
				.setTradeType(WxPayApi.TradeType.APP).setNotifyUrl(notify_url)
				.setOutTradeNo(String.valueOf(System.currentTimeMillis())).build();

		String xmlResult = WxPayApi.pushOrder(false, params);

		logger.info(xmlResult);
		Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

		String return_code = result.get("return_code");
		String return_msg = result.get("return_msg");
		if (!PaymentKit.codeIsOK(return_code)) {
			ajax.addError(return_msg);
			return GsonUtil.toJson(ajax, false);
		}
		String result_code = result.get("result_code");
		if (!PaymentKit.codeIsOK(result_code)) {
			ajax.addError(return_msg);
			return GsonUtil.toJson(ajax, false);
		}
		// 以下字段在return_code 和result_code都为SUCCESS的时候有返回
		String prepay_id = result.get("prepay_id");
		// 封装调起微信支付的参数 https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12
		Map<String, String> packageParams = new HashMap<String, String>();
		packageParams.put("appid", WxPayApiConfigKit.getWxPayApiConfig().getAppId());
		packageParams.put("mch_id", WxPayApiConfigKit.getWxPayApiConfig().getMchId());
		packageParams.put("prepayid", prepay_id);
		packageParams.put("package", "Sign=WXPay");
		packageParams.put("noncestr", System.currentTimeMillis() + "");
		packageParams.put("timestamp", System.currentTimeMillis() / 1000 + "");
		String packageSign = PaymentKit.createSign(packageParams,
				WxPayApiConfigKit.getWxPayApiConfig().getPaternerKey());
		packageParams.put("sign", packageSign);

		String jsonStr = JSON.toJSONString(packageParams);				
		logger.info("最新返回apk的参数:" + jsonStr);
		return (jsonStr);
	}

	@RequestMapping(value = "/pay_notify")
	@ResponseBody
	public String pay_notify(HttpServletRequest request) {
		// 获取所有的参数
		StringBuffer sbf = new StringBuffer();

		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			sbf.append(o.toString() + "=" + request.getParameter(o.toString()));
		}

		logger.error("支付通知参数：" + sbf.toString());

		// 支付结果通用通知文档: https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_7
		String xmlMsg = HttpKit.readData(request);
		System.out.println("支付通知=" + xmlMsg);
		Map<String, String> params = PaymentKit.xmlToMap(xmlMsg);

		// String appid = params.get("appid");
		// //商户号
		// String mch_id = params.get("mch_id");
		String result_code = params.get("result_code");
		// String openId = params.get("openid");
		// //交易类型
		// String trade_type = params.get("trade_type");
		// //付款银行
		// String bank_type = params.get("bank_type");
		// // 总金额
		// String total_fee = params.get("total_fee");
		// //现金支付金额
		// String cash_fee = params.get("cash_fee");
		// // 微信支付订单号
		// String transaction_id = params.get("transaction_id");
		// // 商户订单号
		// String out_trade_no = params.get("out_trade_no");
		// // 支付完成时间，格式为yyyyMMddHHmmss
		// String time_end = params.get("time_end");

		///////////////////////////// 以下是附加参数///////////////////////////////////

		String attach = params.get("attach");
		// String fee_type = params.get("fee_type");
		// String is_subscribe = params.get("is_subscribe");
		// String err_code = params.get("err_code");
		// String err_code_des = params.get("err_code_des");

		// 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态
		// 避免已经成功、关闭、退款的订单被再次更新
		// Order order = Order.dao.getOrderByTransactionId(transaction_id);
		// if (order==null) {
		if (PaymentKit.verifyNotify(params, WxPayApiConfigKit.getWxPayApiConfig().getPaternerKey())) {
			if (("SUCCESS").equals(result_code)) {
				// 更新订单信息
				logger.warn("更新订单信息:" + attach);

				// 发送通知等

				Map<String, String> xml = new HashMap<String, String>();
				xml.put("return_code", "SUCCESS");
				xml.put("return_msg", "OK");
				return (PaymentKit.toXml(xml));
			}
		}
		// }
		return ("");
	}
}
