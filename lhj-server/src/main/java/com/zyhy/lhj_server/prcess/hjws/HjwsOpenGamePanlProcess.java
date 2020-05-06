/**
 * 
 */
package com.zyhy.lhj_server.prcess.hjws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.zyhy.common_lhj.Bet;
import com.zyhy.common_lhj.BetLimitInfo;
import com.zyhy.common_lhj.Icon;
import com.zyhy.common_lhj.IconInfo;
import com.zyhy.common_lhj.Window;
import com.zyhy.common_lhj.WindowInfo;
import com.zyhy.common_lhj.process.AbstractHttpMsgProcess;
import com.zyhy.common_server.result.HttpMessageResult;
import com.zyhy.lhj_server.bgmanagement.config.MessageIdEnum;
import com.zyhy.lhj_server.bgmanagement.service.imp.BgManagementServiceImp;
import com.zyhy.lhj_server.constants.MessageConstants;
import com.zyhy.lhj_server.game.hjws.HjwsBetEnum;
import com.zyhy.lhj_server.game.hjws.HjwsIconEnum;
import com.zyhy.lhj_server.game.hjws.HjwsScatterInfo;
import com.zyhy.lhj_server.game.hjws.HjwsWindowEnum;
import com.zyhy.lhj_server.prcess.result.hjws.HjwsGamePanlResult;
import com.zyhy.lhj_server.service.hjws.HjwsGameService;

/**
 * @author linanjun
 * 打开游戏页面
 */
@Order
@Component
public class HjwsOpenGamePanlProcess extends AbstractHttpMsgProcess{
	@Autowired
	private BgManagementServiceImp bgManagementServiceImp;
	public Map<Double, Integer> betenum = new HashMap<>();
	@Autowired
	private HjwsGameService gameService;
	@Override
	public int getMessageId() {
		return MessageConstants.HJWS_OPEN_GAME_PANL;
	}

	@Override
	public HttpMessageResult handler(String uuid, Map<String, String> body)
			throws Throwable {
		HjwsGamePanlResult result = new HjwsGamePanlResult();
		
		// 获取后台管理赌注配置
		List<Double> betList = bgManagementServiceImp.queryBetList(MessageIdEnum.HJWS.getRedisName());
		for (int i = 0; i < betList.size(); i++) {
			for (HjwsBetEnum e : HjwsBetEnum.values()) {
				if (e.getId() == i) {
					e.setBetcoin(betList.get(i));
					betenum.put(e.getBetcoin(), e.getId());
				}
			}
		}
		
		result.setBetlimitinfos(betinfos(ArrayUtils.subarray(HjwsBetEnum.values(), 0,betList.size())));
		result.setWindowinfos(windowinfos(HjwsWindowEnum.values()));
		result.setZooIconInfos(iconinfos(HjwsIconEnum.values()));
		String roleid = body.get("roleid");
		//判断是否免费游戏中
		HjwsScatterInfo scatter = gameService.getData(roleid, uuid);
		if(scatter != null){
			if (scatter != null) {
				double lastbet = scatter.getBetInfo().getGold();
				if (betenum.containsKey(lastbet)) {
					scatter.setBetLine( betenum.get(lastbet));
				} else {
					betenum.put(lastbet, betenum.size());
					scatter.setBetLine( betenum.size());
				}
			}
			result.setScatter(scatter);
		}
		return result;
	}
	
	private List<BetLimitInfo> betinfos(Bet[] bs) {
		List<BetLimitInfo> is = new ArrayList<BetLimitInfo>();
		for(Bet b : bs) {
			is.add(new BetLimitInfo(b));
		}
		return is;
	}
	
	private List<List<Window>> windowinfos(Window[] ws){
		List<List<Window>> ls = new ArrayList<List<Window>>();
		int i,j;
		for(i = 0;i < 5; i++){
			ls.add(new ArrayList<Window>());
			for(j = 0;j < 3;j++){
				ls.get(i).add(new WindowInfo(ws[i*j]));
			}
		}
		return ls;
	}
	
	private List<IconInfo> iconinfos(Icon[] is) {
		List<IconInfo> ls = new ArrayList<IconInfo>();
		for(Icon i : is) {
			ls.add(new IconInfo(i));
		}
		return ls;
	}

}
