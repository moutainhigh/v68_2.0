/**
 * 
 */
package com.zyhy.lhj_server.prcess.bqtp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.zyhy.common_lhj.BetInfo;
import com.zyhy.common_lhj.Player;
import com.zyhy.common_lhj.WindowInfo;
import com.zyhy.common_lhj.pool.JackPool;
import com.zyhy.common_lhj.pool.JackPoolManager;
import com.zyhy.common_lhj.process.AbstractHttpMsgProcess;
import com.zyhy.common_server.model.GameLhjLog;
import com.zyhy.common_server.result.HttpMessageResult;
import com.zyhy.common_server.util.DateUtils;
import com.zyhy.common_server.util.NumberTool;
import com.zyhy.common_server.util.RandomUtil;
import com.zyhy.lhj_server.bgmanagement.config.MessageIdEnum;
import com.zyhy.lhj_server.bgmanagement.entity.PayoutLimit;
import com.zyhy.lhj_server.bgmanagement.entity.SoltGameInfo;
import com.zyhy.lhj_server.bgmanagement.manager.WorkManager;
import com.zyhy.lhj_server.bgmanagement.prcess.work.updaeInventory;
import com.zyhy.lhj_server.bgmanagement.service.imp.BgManagementServiceImp;
import com.zyhy.lhj_server.constants.Constants;
import com.zyhy.lhj_server.constants.MessageConstants;
import com.zyhy.lhj_server.game.GameOddsEnum;
import com.zyhy.lhj_server.game.ShowRecordResult;
import com.zyhy.lhj_server.game.bqtp.BqtpIconEnum;
import com.zyhy.lhj_server.game.bqtp.BqtpReplenish;
import com.zyhy.lhj_server.game.bqtp.BqtpScatterInfo;
import com.zyhy.lhj_server.prcess.result.bqtp.BqtpGameBetResult;
import com.zyhy.lhj_server.service.bqtp.BqtpGameService;
import com.zyhy.lhj_server.service.bqtp.BqtpReplenishService;
import com.zyhy.lhj_server.userservice.UserService;

/**
 * @author linanjun
 * 开始游戏
 */
@Order
@Component
public class BqtpStartGameInfoProcess extends AbstractHttpMsgProcess{
	@Autowired
	private BgManagementServiceImp bgManagementServiceImp;
	@Autowired
	private BqtpOpenGamePanlProcess openGamePanlProcess;
	@Autowired
	private BqtpGameService gameService;
	@Autowired
	private BqtpReplenishService replenishService;
	@Autowired
	private UserService userService;
	//@Autowired
	//private RocketmqTemplate rocketmqTemplate;
	@Autowired
	private JackPoolManager jackPoolManager;
	@Override
	public int getMessageId() {
		return MessageConstants.BQTU_START_GAME_INFO;
	}
	@Override
	public HttpMessageResult handler(String uuid, Map<String, String> body)
			throws Throwable {
		// 返回信息
		BqtpGameBetResult result = new BqtpGameBetResult();
		String roleid = body.get("roleid");
		double lineBet = Double.parseDouble(body.get("jetton"));// 线注
		int betnum = Integer.parseInt(body.get("num"));// 硬币数
		
		// 判断下注信息
		Map<Double, Integer> betList = openGamePanlProcess.betenum;
		if (betList.size() <= 0) {
			result.setRet(2);
			result.setMsg("投注信息错误");
			return result;
		}
		boolean betEnum = false;
		if (betList.containsKey(lineBet)) {
			betEnum = true;
		}
		
		if(!betEnum || betnum < 1 || betnum > 10){
			result.setRet(2);
			result.setMsg("投注错误");
			return result;
		}
		// 赌注
		double bet = lineBet*50;
		// 用户信息
		Player userinfo = userService.getUserInfo(roleid, uuid);
		if(userinfo == null){
			result.setRet(2);
			result.setMsg("登录错误");
			return result;
		}
		
		//判断是否在掉落
		BqtpReplenish rep = replenishService.getReplenishData(roleid, uuid);
		if(rep != null){
			result.setRet(2);
			result.setMsg("掉落中");
			return result;
		}
		// 玩家游戏币
		double usercoin = userinfo.getGold();
		// 投注前金币
		double startbalance = usercoin;
		//总额
		double totalBet = new BigDecimal(bet * betnum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		// 检查游戏币合理性
		if (usercoin < totalBet) {
			result.setRet(4);
			result.setMsg("游戏币不足");
			return result;
		}
		BetInfo betInfo = new BetInfo(bet, betnum ,totalBet);
		
		//判断是否免费游戏中
		BqtpScatterInfo bi = gameService.getData(roleid, uuid);
		boolean freeGame = false;
		if(bi != null && bi.getNum() > 0){//正在进行免费游戏
			betInfo = bi.getBetInfo();
			freeGame = true;
		}
		
		// 回合id
		JackPool p = jackPoolManager.getPool();
		 /*int randomMd5 = RandomUtil.getRandom(10000000, 99999999);
		String md5 = Md5Utils.md5(String.valueOf(randomMd5));
		int random = RandomUtil.getRandom(0, md5.length() - 6);
		String substring = md5.substring(random, random + 5);
		String roundId = roleid + substring;*/
		String roundId = MessageIdEnum.BQTP.getGameId() + "|" + userService.getOrderId();
		//不是免费游戏扣除金币
		if(!freeGame){
			// 下注
//			userService.bet(roleid, totalBet, roundId, uuid, Constants.BETTYPE1);
		}else {
			// 免费游戏扣除次数
			bi.setNum(bi.getNum() - 1);
			// 免费游戏下注
//			userService.bet(roleid, 0, roundId, uuid, Constants.BETTYPE0);
		}
		
		// 查询当前赔率
		SoltGameInfo gameInfo = bgManagementServiceImp.queryGameInfo(MessageIdEnum.BQTP.getRedisName());
		double odds = bgManagementServiceImp.queryLhjCurrentOdds(gameInfo);
		//System.out.println("gameInfo" + gameInfo);
		//System.out.println("当前游戏的赔率: " + gameInfo.getOdds());
		int iconId = GameOddsEnum.getIdByOdds(odds);
		//System.out.println("使用第几套图标" + iconId);
		if (iconId == 0) {
			iconId = 4;
			//System.out.println("没有查询到赔率,使用第"+  iconId +"套图标" );
		}
		// 查询是否中奖池游戏
		PayoutLimit queryWinlimit = bgManagementServiceImp.queryWinlimit();
		//System.out.println("当前派奖条件" + queryWinlimit);
		// 是否中赔率奖池奖励
		boolean isOddsPool = false;
		// 中奖金额
		double oddsReward = 0;
		if (queryWinlimit != null) {
			// 查询玩家是否满足赔率奖池中奖条件
			isOddsPool = bgManagementServiceImp.queryPlayerWinlimit(roleid);
			//System.out.println("是否中赔率奖池奖励" + isOddsPool);
		}
		
		if (isOddsPool) {
			// 中奖概率
			double odds2 = queryWinlimit.getOdds() * 10000;
			//System.out.println("当前赔率奖池奖励的中奖率" + odds2);
			double random = RandomUtil.getRandom(1.0, 10000.0);
			if (random > 10000.0 - odds2) {
				oddsReward = queryWinlimit.getPayLowLimit() * queryWinlimit.getPayRatio();
				iconId = 8;
			}
		}
		//System.out.println("赔率奖池的奖励为为=========>" + oddsReward);
		//System.out.println("当前使用第几套图标: " + iconId);
		// 执行游戏逻辑
		if (oddsReward > 0) {
			int count = 0;
			while (true) {
				result = gameService.doGameProcess(roleid, betInfo,freeGame,iconId);
				count ++;
				if (result.getRewardcoin() >= oddsReward - oddsReward*0.1 && result.getRewardcoin() <= oddsReward
						|| (count > 200 && result.getRewardcoin() > 0)) {
					//System.out.println("本次赔率奖池的奖励为: " + result.getRewardcoin());
					break;
				}
			}
		} else {
			result = gameService.doGameProcess(roleid, betInfo,freeGame,iconId);
			// 验证大奖金额
			double checkAmount = gameInfo.getCheckAmount();
			if (result.getRewardcoin() >= checkAmount && checkAmount > 0) {
				boolean checkBigReward = bgManagementServiceImp.checkBigReward(gameInfo,result.getRewardcoin());
				if (!checkBigReward) {
					while (true) {
						result = gameService.doGameProcess(roleid, betInfo,freeGame,iconId);
						if (result.getRewardcoin() < checkAmount) {
							break;
						}
					}
				}
			}
		}
		
		/*// 当前使用那套图标
		result.setIcon(iconId);
		// 当前的赔率
		System.out.println("总派彩" + gameInfo.getTotalPay());
		System.out.println("总投注" + gameInfo.getTotalBet());
		double odds1 = gameInfo.getTotalPay()/gameInfo.getTotalBet();
		result.setOdds(odds1);
		System.out.println("总赔率" + odds1);*/
		
		//result = gameService.doGameProcess(roleid, betInfo,freeGame,1);
		
		
		/*// 判断通杀
		LOG.info("POOL :" + p.getCurrentnum()  + "|" +"POOL_STATUS :" + p.getStatus() 
		+ "|" + "CURRENT_REWARD :" + result.getRewardcoin());
		if ((p.getStatus() == JackPool.STATUS_KILL && result.getRewardcoin() > 0)
				|| (p.getStatus() == JackPool.STATUS_KILL && result.isScatter())) {
			LOG.info("STATUS_KILL_BEFORE :" + result.getRewardcoin());
			int count = 0;
			while (true) {
				result = gameService.doGameProcess(roleid, betInfo,freeGame);
				count ++ ;
				if (result.getRewardcoin() == 0 || count == 100) {
					LOG.info("STATUS_KILL_AFTER :" + result.getRewardcoin() + "|" + "COUNT : " + count);
					break;
				}
			}
		}
		// 判断放水
		if (p.getStatus() == JackPool.STATUS_WIN && result.getRewardcoin() == 0) {
			LOG.info("STATUS_WIN_BEFORE :" + result.getRewardcoin());
			int count = 0;
			while (true) {
				result = gameService.doGameProcess(roleid, betInfo,freeGame);
				count ++ ;
				if ((result.getRewardcoin() > 0  
						&& userService.checkBigReward(betInfo.getTotalBet(), result.getRewardcoin(), p.getCurrentnum()))|| count == 100) {
					LOG.info("STATUS_WIN_AFTER:" + result.getRewardcoin() + "|" + "COUNT : " + count);
					break;
				}
			}
		}
		// 正常模式
		if (result.getRewardcoin() > 0 && 
				!userService.checkBigReward(betInfo.getTotalBet(), result.getRewardcoin(), p.getCurrentnum())) {
			LOG.info("STATUS_NORMAL_BEFORE :" + result.getRewardcoin());
			int count = 0;
			while (true) {
				result = gameService.doGameProcess(roleid, betInfo,freeGame);
				count ++ ;
				if (userService.checkBigReward(betInfo.getTotalBet(), result.getRewardcoin(), p.getCurrentnum()) || count == 100) {
					LOG.info("STATUS_NORMAL_AFTER:" + result.getRewardcoin() + "|" + "COUNT : " + count);
					break;
				}
			}
		}*/
		
		// 计算免费游戏奖励
		if (freeGame && bi != null) {
			if(result.getRewardcoin() > 0){
				List<List<WindowInfo>> win = result.getWin();
				int scatter = 0;
				int other = 0;
				for (List<WindowInfo> list : win) {
					for (WindowInfo wl : list) {
						if (wl != null) {
							if (wl.getIcon() == BqtpIconEnum.SCATTER) {
								scatter ++;
							} else {
								other ++;
							}
						}
					}
				}
				if (scatter == 0 || other > 0) {
					bi.setCount(bi.getCount() + 1);
				} 
			}
			if (bi.getCount() > 1 && bi.getCount() < 11) {
				result.setRewardcoin(NumberTool.multiply(result.getRewardcoin(),bi.getCount()).doubleValue());
			}
		}
		
		// 保存补充数据
		if(result.isReplenish()){
			BqtpReplenish newRep = result.getRep();
			replenishService.saveReplenishInfoCache(newRep, roleid, uuid);
		}
		
		//金币变化
		double change = NumberTool.subtract(result.getRewardcoin(), totalBet).doubleValue();
		
		//更新免费游戏总奖励
		if(freeGame ){
			change = NumberTool.subtract(result.getRewardcoin(), 0.0).doubleValue();
			bi.setGold(NumberTool.add(bi.getGold(), result.getRewardcoin()).doubleValue());
			bi.setSingleReward(result.getRewardcoin());
			gameService.saveFreeInfoCache(bi, roleid, userinfo, uuid);
			result.setScatterInfo(bi);
			result.setScatterNum(bi.getNum());
		}
		
		// 是否有免费游戏
		if(result.isScatter() && !freeGame){
			BqtpScatterInfo sinfo = new BqtpScatterInfo(betInfo);
			sinfo.setNum(result.getScatterNum());
			sinfo.setGold(0);
			sinfo.setSingleReward(0);
			sinfo.setCount(1);
			result.setScatterInfo(sinfo);
			gameService.saveFreeInfoCache(sinfo, roleid, userinfo, uuid);
		}
		
		if(bi != null && bi.getNum() == 0 && !result.isReplenish()){
			gameService.delFreeInfoCache(roleid, uuid);
		}
		
		// 派彩
		double payout = 0;
//		userService.payout(roleid, result.getRewardcoin(), roundId, uuid);
		payout = result.getRewardcoin();
		result.setUsercoin(NumberTool.add(usercoin, change).doubleValue());
		
		// 记录日志
		//gameService.saveBigReward(Constants.BQTP_SERVER_NAME, betInfo, result.getRewardcoin(), roleid);
		ShowRecordResult srr = new ShowRecordResult();
		int ispool = 0;
		if (oddsReward > 0) {
			ispool = 1;
		}
		int type = 0;
		if (freeGame) {
			type = 2;
		}else {
			type = 1;
		}
		srr.setGameName(Constants.BQTP_GAME_NAME);
		srr.setDbId("DMG17-" + roundId);
		if (freeGame) {
			srr.setRecordType(Constants.RECORDTYPE2);
		}else {
			srr.setRecordType(Constants.RECORDTYPE1);
		}
		String date = DateUtils.format(new Date(), DateUtils.fp1);
		srr.setDate(date);
		srr.setStartbalance(startbalance);
		double bet1 = 0;
		if (freeGame) {
			bet1 = 0;
		}else {
			bet1 = totalBet;
		}
		srr.setBet(bet1);
		srr.setReward(result.getRewardcoin());
		srr.setEndbalance(result.getUsercoin());
		srr.setGameresult(result);
		//srr.setGametime(gametime);
		srr.setRolenick(userinfo.getNickname());
		srr.setRoundId(roundId);
		GameLhjLog log = GameLhjLog.build(Constants.BQTP_GAME_NAME, "start", roleid,userinfo.getNickname(),uuid,bet1,payout,type,ispool, JSON.toJSONString(srr));
		//rocketmqTemplate.pushLog(Tags.LHJ_GAME, JSONObject.toJSONString(log));
		WorkManager.instance().submit(updaeInventory.class, log);
		return result;
	}

}
