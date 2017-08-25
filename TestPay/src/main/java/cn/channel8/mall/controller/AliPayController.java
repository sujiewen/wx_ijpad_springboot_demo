package cn.channel8.mall.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCancelModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.domain.AlipayTradeOrderSettleModel;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.jpay.alipay.AliPayApi;
import com.jpay.alipay.AliPayApiConfig;
import com.jpay.alipay.AliPayApiConfigKit;
import com.jpay.util.StringUtils;
import com.jpay.vo.AjaxResult;

import cn.channel8.mall.config.PayConfig;
import cn.channel8.mall.utils.GsonUtil;

@Controller
@RequestMapping(value = "/alipay")
public class AliPayController {
	private static Logger logger = LoggerFactory.getLogger(AliPayController.class);
	
	private  String charset = "UTF-8";
	private  String private_key = "";
	private  String alipay_public_key = "";
	private  String service_url = "";
	private  String app_id = "";
	private  String sign_type = "RSA2";
	private  String notify_domain = "";
	
	//构造函数获取AliPay配置
	AliPayController(){
		Map<String,String> map= PayConfig.getInstance().getAliConfiguration();	
		alipay_public_key= map.get("alipay_public_key");
		private_key = map.get("private_key");
		service_url = map.get("service_url");
		app_id = map.get("app_id");
		notify_domain = map.get("notify_domain");
		this.getApiConfig();
	}
	
	
	
	private AjaxResult result = new AjaxResult();
	
	public AliPayApiConfig getApiConfig() {
		AliPayApiConfig aliPayApiConfig = AliPayApiConfig.New()
		.setAppId(app_id)
		.setAlipayPublicKey(alipay_public_key)
		.setCharset(charset)
		.setPrivateKey(private_key)
		.setServiceUrl(service_url)
		.setSignType(sign_type)
		.build();
		AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
		return aliPayApiConfig;
	}
	
	@RequestMapping(value = "/")
	@ResponseBody
	public String index() {
		return "欢迎使用IJPay 中的支付宝支付 -By Javen";
	}

	/**
	 * app支付
	 */
	@ResponseBody
	@RequestMapping(value = "/apppay")
	public String appPay() {
		try {
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			model.setBody("我是测试数据-By Javen");
			model.setSubject("App支付测试-By Javen");
			model.setOutTradeNo(StringUtils.getOutTradeNo());
			model.setTimeoutExpress("30m");
			model.setTotalAmount("0.01");
			model.setPassbackParams("callback params");
			model.setProductCode("QUICK_MSECURITY_PAY");
			String orderInfo = AliPayApi.startAppPayStr(model, notify_domain + "/alipay/app_pay_notify");
			result.success(orderInfo);

		} catch (AlipayApiException e) {
			e.printStackTrace();
			result.addError("system error");
		}
		return result.toString();
	}

	/**
	 * Wap快速支付
	 */
	@ResponseBody
	@RequestMapping(value = "/wappay")
	public String wapPay(HttpServletRequest request,HttpServletResponse response,@RequestParam(value="total_fee") String total_fee) {
		this.getApiConfig();
		String body = "我是测试数据-By Javen";
		String subject = "Javen Wap支付测试";
		//支付金额
		String totalAmount = total_fee;
		String passbackParams = "1";
		String returnUrl = notify_domain + "/alipay/return_url";
		String notifyUrl = notify_domain + "/alipay/notify_url";

		AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
		model.setBody(body);
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setPassbackParams(passbackParams);
		String outTradeNo = StringUtils.getOutTradeNo();
		System.out.println("wap outTradeNo>"+outTradeNo);
		model.setOutTradeNo(outTradeNo);
		model.setProductCode("QUICK_WAP_PAY");

		try {
			//待验证
			AliPayApi.wapPay(response, model, returnUrl, notifyUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	/**
	 * PC支付
	 */
	@ResponseBody
	@RequestMapping(value = "/pcpay")
	public String pcPay(HttpServletRequest request,HttpServletResponse response){
		try {
			String totalAmount = "88.88"; 
			String outTradeNo =StringUtils.getOutTradeNo();
			logger.info("pc outTradeNo>"+outTradeNo);
			
			String returnUrl = notify_domain + "/alipay/return_url";
			String notifyUrl = notify_domain + "/alipay/notify_url";
			AlipayTradePayModel model = new AlipayTradePayModel();
			
			model.setOutTradeNo(outTradeNo);
			model.setProductCode("FAST_INSTANT_TRADE_PAY");
			model.setTotalAmount(totalAmount);
			model.setSubject("Javen PC支付测试");
			model.setBody("Javen IJPay PC支付测试");
			
			AliPayApi.tradePage(response,model , notifyUrl, returnUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}

	/**
	 * 条形码支付
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.Yhpibd&
	 * treeId=194&articleId=105170&docType=1#s4
	 */
	@ResponseBody
	@RequestMapping(value = "/tradepay")
	public String tradePay(@RequestParam(value="auth_code") String auth_code) {
		String authCode = auth_code;
		String subject = "Javen 支付宝条形码支付测试";
		String totalAmount = "100";
		String notifyUrl = notify_domain + "/alipay/notify_url";
		String resultStr = "";

		AlipayTradePayModel model = new AlipayTradePayModel();
		model.setAuthCode(authCode);
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setOutTradeNo(StringUtils.getOutTradeNo());
		model.setScene("bar_code");
		try {
			resultStr = AliPayApi.tradePay(model,notifyUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}
	
	
	
	/**
	 * 声波支付
	 * https://doc.open.alipay.com/docs/doc.htm?treeId=194&articleId=105072&docType=1#s2
	 */
	@ResponseBody
	@RequestMapping(value = "/tradewavepay")
	public String tradeWavePay(@RequestParam(value="auth_code") String auth_code) {
		String authCode = auth_code;
		String subject = "Javen 支付宝声波支付测试";
		String totalAmount = "100";
		String notifyUrl = notify_domain + "/alipay/notify_url";
		String resultStr = "";

		AlipayTradePayModel model = new AlipayTradePayModel();
		model.setAuthCode(authCode);
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setOutTradeNo(StringUtils.getOutTradeNo());
		model.setScene("wave_code");
		try {
			resultStr = AliPayApi.tradePay(model,notifyUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}

	/**
	 * 扫码支付
	 */
	@ResponseBody
	@RequestMapping(value = "/tradeprecreatepay")
	public String tradePrecreatePay() {
		String subject = "Javen 支付宝扫码支付测试";
		String totalAmount = "86";
		String storeId = "123";
		String notifyUrl = notify_domain + "/alipay/precreate_notify_url";
		String qr_code = "";

		AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setStoreId(storeId);
		model.setTimeoutExpress("5m");
		model.setOutTradeNo(StringUtils.getOutTradeNo());
		try {
			String resultStr = AliPayApi.tradePrecreatePay(model, notifyUrl);
			JSONObject jsonObject = JSONObject.parseObject(resultStr);
			qr_code = jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return qr_code;
	}

	/**
	 * 单笔转账到支付宝账户
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.54Ty29&
	 * treeId=193&articleId=106236&docType=1
	 */
	@ResponseBody
	@RequestMapping(value = "/transfer")
	public String transfer() {
		Map<String, Object> mapD = new HashMap<String, Object>();
		boolean isSuccess = false;
		String total_amount = "66";
		AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
		model.setOutBizNo(StringUtils.getOutTradeNo());
		model.setPayeeType("ALIPAY_LOGONID");
		model.setPayeeAccount("abpkvd0206@sandbox.com");
		model.setAmount(total_amount);
		model.setPayerShowName("测试退款");
		model.setPayerRealName("沙箱环境");
		model.setRemark("javen测试单笔转账到支付宝");

		try {
			isSuccess = AliPayApi.transfer(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mapD.put("success", isSuccess);
		
		return GsonUtil.toJson(mapD, false);
	}

	/**
	 * 下载对账单
	 */
	@ResponseBody
	@RequestMapping(value = "/dataDataserviceBill")
	public String dataDataserviceBill(@RequestParam(value="billDate") String billDate) {
		String para =billDate+"";
		String resultStr = "";
		try {
			AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
			model.setBillType("trade");
			model.setBillDate(para);
			resultStr = AliPayApi.billDownloadurlQuery(model);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}

	/**
	 * 退款
	 */
	@ResponseBody
	@RequestMapping(value = "/traderefund")
	public String tradeRefund() {
		String resultStr = "";
		try {
			AlipayTradeRefundModel model = new AlipayTradeRefundModel();
//			model.setOutTradeNo("042517111114931");
//			model.setTradeNo("2017042521001004200200236813");
			model.setOutTradeNo("081014283315023");
			model.setTradeNo("2017081021001004200200273870");
			model.setRefundAmount("86.00");
			model.setRefundReason("正常退款");
			resultStr = AliPayApi.tradeRefund(model);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}

	

	/**
	 * 交易查询
	 */
	@ResponseBody
	@RequestMapping(value = "/traderequery")
	public String tradeQuery() {
		boolean isSuccess = false;
		Map<String, Object> mapD = new HashMap<String, Object>();
		try {
			AlipayTradeQueryModel model = new AlipayTradeQueryModel();
			model.setOutTradeNo("081014283315023");
			model.setTradeNo("2017081021001004200200273870");

			isSuccess = AliPayApi.isTradeQuery(model);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		mapD.put("success", isSuccess);
		
		return GsonUtil.toJson(mapD, false);
	}

	@ResponseBody
	@RequestMapping(value = "/traderequerybytradeno")
	public String tradeQueryByStr(@RequestParam(value="out_trade_no") String out_trade_no) {
		
		String resultStr = "";
		
		String out_tradeNo = out_trade_no;
		// String trade_no = getPara("trade_no");

		AlipayTradeQueryModel model = new AlipayTradeQueryModel();
		model.setOutTradeNo(out_tradeNo);

		try {
			resultStr = AliPayApi.tradeQuery(model).getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}
	/**
	 * 创建订单
	 * {"alipay_trade_create_response":{"code":"10000","msg":"Success","out_trade_no":"081014283315033","trade_no":"2017081021001004200200274066"},"sign":"ZagfFZntf0loojZzdrBNnHhenhyRrsXwHLBNt1Z/dBbx7cF1o7SZQrzNjRHHmVypHKuCmYifikZIqbNNrFJauSuhT4MQkBJE+YGPDtHqDf4Ajdsv3JEyAM3TR/Xm5gUOpzCY7w+RZzkHevsTd4cjKeGM54GBh0hQH/gSyhs4pEN3lRWopqcKkrkOGZPcmunkbrUAF7+AhKGUpK+AqDw4xmKFuVChDKaRdnhM6/yVsezJFXzlQeVgFjbfiWqULxBXq1gqicntyUxvRygKA+5zDTqE5Jj3XRDjVFIDBeOBAnM+u03fUP489wV5V5apyI449RWeybLg08Wo+jUmeOuXOA=="}
	 */
	@ResponseBody
	@RequestMapping(value = "/tradecreate")
	public String tradeCreate(@RequestParam(value="out_trade_no") String out_trade_no){
		String outTradeNo = out_trade_no;
		
		String notifyUrl = notify_domain+ "/alipay/notify_url";
		
		String resultStr = "";
		
		AlipayTradeCreateModel model = new AlipayTradeCreateModel();
		model.setOutTradeNo(outTradeNo);
		model.setTotalAmount("88.88");
		model.setBody("Body");
		model.setSubject("Javen 测试统一收单交易创建接口");
		model.setBuyerLogonId("abpkvd0206@sandbox.com");//买家支付宝账号，和buyer_id不能同时为空
		try {
			
			AlipayTradeCreateResponse response = AliPayApi.tradeCreate(model,notifyUrl);
			resultStr = response.getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}
	
	/**
	 * 撤销订单
	 */
	@ResponseBody
	@RequestMapping(value = "/tradecancel")
	public String tradeCancel() {
		boolean isSuccess = false;
		Map<String, Object> mapD = new HashMap<String, Object>();
		try {
			AlipayTradeCancelModel model = new AlipayTradeCancelModel();
			model.setOutTradeNo("081014283315033");
			model.setTradeNo("2017081021001004200200274066");

			isSuccess = AliPayApi.isTradeCancel(model);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		mapD.put("success", isSuccess);
		
		return GsonUtil.toJson(mapD, false);
	}
	
	/**
	 * 关闭订单
	 */
	@ResponseBody
	@RequestMapping(value = "/tradeclose")
	public String tradeClose(@RequestParam(value="out_trade_no") String out_trade_no,@RequestParam(value="trade_no") String trade_no){
		String outTradeNo = out_trade_no;
		String tradeNo = trade_no;
		String resultStr = "";
		try {
			AlipayTradeCloseModel model = new AlipayTradeCloseModel();
			model.setOutTradeNo(outTradeNo);
			
			model.setTradeNo(tradeNo);
			
			resultStr = AliPayApi.tradeClose(model).getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}
	
	/**
	 * 结算
	 */
	@ResponseBody
	@RequestMapping(value = "/tradeordersettle")
	public String tradeOrderSettle(@RequestParam(value="trade_no") String trade_no){
		String tradeNo = trade_no;//支付宝订单号
		String resultStr = "";
		try {
			AlipayTradeOrderSettleModel model = new AlipayTradeOrderSettleModel();
			model.setOutRequestNo(StringUtils.getOutTradeNo());
			model.setTradeNo(tradeNo);
			
			resultStr = AliPayApi.tradeOrderSettle(model ).getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		return resultStr;
	}
	
	@ResponseBody
	@RequestMapping(value = "/return_url")
	public String return_url(HttpServletRequest request) {
		try {
			// 获取支付宝GET过来反馈信息
			Map<String, String> map = AliPayApi.toMap(request);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}

			boolean verify_result = AlipaySignature.rsaCheckV1(map, alipay_public_key, charset,
					sign_type);

			if (verify_result) {// 验证成功
				// TODO 请在这里加上商户的业务逻辑程序代码
				System.out.println("return_url 验证成功");
				return ("success");
			} else {
				System.out.println("return_url 验证失败");
				// TODO
				return ("failure");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return ("failure");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/notify_url")
	public String notify_url(HttpServletRequest request) {
		try {
			// 获取支付宝POST过来反馈信息
			Map<String, String> params = AliPayApi.toMap(request);

			for (Map.Entry<String, String> entry : params.entrySet()) {
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}

			boolean verify_result = AlipaySignature.rsaCheckV1(params, alipay_public_key, charset,
					sign_type);

			if (verify_result) {// 验证成功
				// TODO 请在这里加上商户的业务逻辑程序代码 异步通知可能出现订单重复通知 需要做去重处理
				System.out.println("notify_url 验证成功succcess");
				return ("success");
			} else {
				System.out.println("notify_url 验证失败");
				return ("failure");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return ("failure");
		}
	}
	//=======其实异步通知实现的方法都一样  但是通知中无法区分支付的方式(没有提供支付方式的参数)======================================================================
	/**
	 * App支付支付回调通知
	 * https://doc.open.alipay.com/docs/doc.htm?treeId=54&articleId=106370&
	 * docType=1#s3
	 */
	@ResponseBody
	@RequestMapping(value = "/app_pay_notify")
	public String app_pay_notify(HttpServletRequest request) {
		try {
			// 获取支付宝POST过来反馈信息
			Map<String, String> params = AliPayApi.toMap(request);
			for (Map.Entry<String, String> entry : params.entrySet()) {
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}
			// 切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
			// boolean AlipaySignature.rsaCheckV1(Map<String, String> params,
			// String publicKey, String charset, String sign_type)
			boolean flag = AlipaySignature.rsaCheckV1(params, alipay_public_key, charset,
					sign_type);
			if (flag) {
				// TODO
				System.out.println("success");
				return ("success");
			} else {
				// TODO
				System.out.println("failure");
				return ("failure");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return ("failure");
		}
	}
	/**
	 * 扫码支付通知
	 */
	@ResponseBody
	@RequestMapping(value = "/precreate_notify_url")
	public String precreate_notify_url(HttpServletRequest request){
		try {
			Map<String, String> map = AliPayApi.toMap(request);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				System.out.println(entry.getKey()+" = "+entry.getValue());
			}
			boolean flag = AlipaySignature.rsaCheckV1(map, alipay_public_key, charset,
					sign_type);
			if (flag) {
				// TODO
				System.out.println("precreate_notify_url success");
				return ("success");
			} else {
				// TODO
				System.out.println("precreate_notify_url failure");
				return ("failure");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return ("failure");
		}
	}

	
}