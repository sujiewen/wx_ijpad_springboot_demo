package cn.channel8.mall.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.SnsAccessToken;
import com.jfinal.weixin.sdk.api.SnsAccessTokenApi;
import com.jfinal.weixin.sdk.api.SnsApi;
import com.jfinal.weixin.sdk.api.UserApi;

import cn.channel8.mall.config.PayConfig;
import cn.channel8.mall.utils.GsonUtil;

@Controller
@RequestMapping(value = "/mallpay")
public class MallPayController {

	// 跳转到授权页面
	@RequestMapping(value = "/toOauth")
	@ResponseBody
	public String toOauth(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "payType") String payType) {
		
		Map<String, Object> userMap = new HashMap<String, Object>();
		
		String domain = PayConfig.getInstance().getWXConfiguration().get("notify_url");
		;
		String appId = PayConfig.getInstance().getWXConfiguration().get("appid");
		String calbackUrl = domain + "/mallpay/oauth";
		String url = SnsAccessTokenApi.getAuthorizeURL(appId, calbackUrl, payType, true);
//
//		try {
//			response.sendRedirect(url);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		userMap.put("redirect", url);
		
		return GsonUtil.toJson(userMap, false);
	}

	// 跳转到授权页面
	@RequestMapping(value = "/oauth")
	@ResponseBody
	public String oauth(HttpServletRequest request, HttpServletResponse response) {
		int  subscribe=1;
		
		HttpSession httpSession = request.getSession();
		
		Map<String, String[]> params = request.getParameterMap();
		
		String[] codeA = params.get("code");
		String[] stateA = params.get("state");
		
		StringBuffer codeBuf = new StringBuffer();
		for (int ii = 0; ii < codeA.length; ii++) {
			codeBuf.append(codeA[ii]);
		}
		
		StringBuffer stateBuf = new StringBuffer();
		for (int ii = 0; ii < stateA.length; ii++) {
			stateBuf.append(stateA[ii]);
		}
		
		//用户同意授权，获取code
		String code=codeBuf.toString();
		String state=stateBuf.toString();
		
		if (code != null) {
			
			Map<String, Object> userMap = new HashMap<String, Object>();
			String appId=PayConfig.getInstance().getWXConfiguration().get("appid");
			String secret=PayConfig.getInstance().getWXConfiguration().get("wxpay_appSecret");
			SnsAccessToken snsAccessToken=SnsAccessTokenApi.getSnsAccessToken(appId,secret,code);
			String openId=snsAccessToken.getOpenid();
			String token=snsAccessToken.getAccessToken();
			//拉取用户信息(需scope为 snsapi_userinfo)
			ApiResult apiResult=SnsApi.getUserInfo(token, openId);
			userMap.put("openId", openId);
			
			if (apiResult.isSucceed()) {
				Map<String,Object> map = GsonUtil.fromJson(apiResult.getJson(), Map.class);
				userMap.putAll(map);
				
				//获取用户信息判断是否关注
				ApiResult userInfo = UserApi.getUserInfo(openId);
				if (userInfo.isSucceed()) {
					String userStr = userInfo.toString();
					subscribe=JSON.parseObject(userStr).getIntValue("subscribe");
				}
			}
			
			PayConfig.getInstance().setWXUserInfo(openId, userMap);
			httpSession.setAttribute("openId", openId);
			
			return GsonUtil.toJson(userMap, false);
			
		}else {
			return null;
		}
	}

}
