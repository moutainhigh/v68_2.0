/**
 * 
 */
package com.zyhy.lhj_server.prcess.yhdd;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.zyhy.common_lhj.Player;
import com.zyhy.common_lhj.process.AbstractHttpMsgProcess;
import com.zyhy.common_server.result.HttpMessageResult;
import com.zyhy.lhj_server.constants.MessageConstants;
import com.zyhy.lhj_server.game.yhdd.YhddBonusInfo;
import com.zyhy.lhj_server.prcess.result.yhdd.YhddBonusCarResult;
import com.zyhy.lhj_server.service.yhdd.YhddBonusService;
import com.zyhy.lhj_server.userservice.UserService;

/**
 * @author ASUS
 *
 */
@Order
@Component
public class YhddBonusLanternProcess extends AbstractHttpMsgProcess{

	@Autowired
	private YhddBonusService bonusService;
	@Autowired
	private UserService userService;
	
	@Override
	public int getMessageId() {
		return MessageConstants.YHDD_BONUS_LANTERN;
	}

	@Override
	public HttpMessageResult handler(String uuid, Map<String, String> body)
			throws Throwable {
		String roleid = body.get("roleid");
		int lantern = Integer.parseInt(body.get("lantern"));
		// 用户信息
		Player userinfo = userService.getUserInfo(roleid, uuid);
		YhddBonusCarResult result = new YhddBonusCarResult();
		if(lantern < 1 || lantern > 6){
			result.setRet(2);
			result.setMsg("参数错误");
			return result;
		}
		YhddBonusInfo bi = bonusService.getData(roleid, uuid);
		result = bonusService.doGameLantern(roleid, lantern,bi);
		if (result.getYhddBonusInfo() != null) {
		bonusService.save(result.getYhddBonusInfo(), roleid, userinfo, uuid);
		}else {
			bonusService.save(bi, roleid, userinfo, uuid);
		}
		
		return result;
	}

}
