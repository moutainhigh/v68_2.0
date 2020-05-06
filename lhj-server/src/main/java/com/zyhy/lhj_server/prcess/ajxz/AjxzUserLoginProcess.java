/**
 * 
 */
package com.zyhy.lhj_server.prcess.ajxz;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.zyhy.common_lhj.Player;
import com.zyhy.common_lhj.process.AbstractHttpMsgProcess;
import com.zyhy.common_server.model.UserLhjLog;
import com.zyhy.common_server.result.HttpMessageResult;
import com.zyhy.lhj_server.bgmanagement.config.MessageIdEnum;
import com.zyhy.lhj_server.bgmanagement.entity.SoltGameInfo;
import com.zyhy.lhj_server.bgmanagement.service.imp.BgManagementServiceImp;
import com.zyhy.lhj_server.constants.Constants;
import com.zyhy.lhj_server.constants.MessageConstants;
import com.zyhy.lhj_server.prcess.result.ajxz.AjxzLoginResult;
import com.zyhy.lhj_server.userservice.UserService;

/**
 * 用户登录
 */
@Order
@Component
public class AjxzUserLoginProcess extends AbstractHttpMsgProcess{

	@Autowired
	private BgManagementServiceImp bgManagementServiceImp;
	@Autowired
	private UserService userService;
	
	//@Autowired
	//private RocketmqTemplate rocketmqTemplate;
	
	@Override
	public int getMessageId() {
		return MessageConstants.AJXZ_USER_APPLY_LOGIN;
	}

	@Override
	public HttpMessageResult handler(String uuid, Map<String, String> body)
			throws Throwable {
		AjxzLoginResult result = new AjxzLoginResult();
		//获取用户信息
		String roleid = body.get("roleid");
		Player user = userService.getUserInfo(roleid, uuid);
		if(user == null){
			result.setRet(2);
			result.setMsg("登录失败");
			return result;
		}
		
		SoltGameInfo gameInfo = bgManagementServiceImp.queryGameInfo(MessageIdEnum.AJXZ.getRedisName());
		if (user.getGold() < gameInfo.getInAmount()) {
			result.setUuid(String.valueOf(gameInfo.getInAmount()));
			result.setGold(user.getGold());
			return result;
		}
		
		result.setGold(user.getGold());
		
		
		UserLhjLog log = UserLhjLog.build(Constants.AJXZ_SERVER_NAME, "login", roleid, user.getNickname(), user.getGold());
		//rocketmqTemplate.pushLog(Tags.LHJ_USER, JSONObject.toJSONString(log));
		return result;
	}
}
