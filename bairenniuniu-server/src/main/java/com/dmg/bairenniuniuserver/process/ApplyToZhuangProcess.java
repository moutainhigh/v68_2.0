package com.dmg.bairenniuniuserver.process;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dmg.bairenniuniuserver.common.model.BasePlayer;
import com.dmg.bairenniuniuserver.common.result.ResultEnum;
import com.dmg.bairenniuniuserver.manager.RoomManager;
import com.dmg.bairenniuniuserver.model.Room;
import com.dmg.bairenniuniuserver.service.PushService;
import com.dmg.bairenniuniuserver.service.cache.PlayerService;
import com.dmg.bairenniuniuserver.tcp.server.AbstractMessageHandler;
import com.dmg.bairenniuniuserver.tcp.server.MessageIdConfig;

/**
 * @Description 申请上庄
 * @Author mice
 * @Date 2019/8/5 16:34
 * @Version V1.0
 **/
@Service
public class ApplyToZhuangProcess implements AbstractMessageHandler {
    @Autowired
    private PlayerService playerCacheService;
    @Autowired
    private PushService pushService;

    @Override
    public String getMessageId() {
        return MessageIdConfig.APPLY_TO_ZHUANG;
    }

    @Override
    public void messageHandler(int userId, JSONObject params) {
        BasePlayer basePlayer = this.playerCacheService.getPlayer(userId);
        Room room = RoomManager.intance().getRoom(basePlayer.getRoomId());
        int bankerLimit = RoomManager.intance().getBankerLimitMap().get(room.getLevel() + "");
        if (basePlayer.getGold().compareTo(new BigDecimal(bankerLimit)) < 0) {
            this.pushService.push(userId, MessageIdConfig.APPLY_TO_ZHUANG, ResultEnum.GOLD_NEED_10000.getCode());
            return;
        }

        for (BasePlayer basePlayer1 : room.getApplyToZhuangPlayerList()) {
            if (basePlayer1.getUserId() == userId) {
                this.pushService.push(userId, MessageIdConfig.APPLY_TO_ZHUANG, ResultEnum.HAS_APPLY_SHANGZHUANG.getCode());
                return;
            }
        }
        if (room.getBanker().getPlayer().getUserId() == userId) {
            this.pushService.push(userId, MessageIdConfig.APPLY_TO_ZHUANG, ResultEnum.BE_BANKER.getCode());
            return;
        }
        room.getApplyToZhuangPlayerList().add(basePlayer);
        this.pushService.push(userId, MessageIdConfig.APPLY_TO_ZHUANG);
    }
}