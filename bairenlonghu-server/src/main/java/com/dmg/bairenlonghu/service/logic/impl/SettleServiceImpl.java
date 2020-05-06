package com.dmg.bairenlonghu.service.logic.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dmg.data.common.dto.SettleSendDto;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.dmg.bairenlonghu.common.constant.Constant;
import com.dmg.bairenlonghu.common.enums.TableEnum;
import com.dmg.bairenlonghu.common.model.BaseRobot;
import com.dmg.bairenlonghu.common.model.Poker;
import com.dmg.bairenlonghu.common.result.MessageResult;
import com.dmg.bairenlonghu.manager.RoomManager;
import com.dmg.bairenlonghu.manager.TimerManager;
import com.dmg.bairenlonghu.model.Room;
import com.dmg.bairenlonghu.model.RoomStatus;
import com.dmg.bairenlonghu.model.Seat;
import com.dmg.bairenlonghu.model.Table;
import com.dmg.bairenlonghu.model.constants.D;
import com.dmg.bairenlonghu.model.dto.HandPokerDTO;
import com.dmg.bairenlonghu.model.dto.PokerInfo;
import com.dmg.bairenlonghu.model.dto.SettleResultDTO;
import com.dmg.bairenlonghu.quarz.StartGameDelayTask;
import com.dmg.bairenlonghu.service.PushService;
import com.dmg.bairenlonghu.service.cache.PlayerService;
import com.dmg.bairenlonghu.service.cache.RobotCacheService;
import com.dmg.bairenlonghu.service.logic.PokerService;
import com.dmg.bairenlonghu.service.logic.SettleService;
import com.dmg.bairenlonghu.sysconfig.RegionConfig;
import com.dmg.bairenlonghu.tcp.server.MessageIdConfig;
import com.dmg.common.core.config.RedisRegionConfig;
import com.dmg.common.core.util.RedisUtil;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author mice
 * @Date 2019/7/30 16:28
 * @Version V1.0
 **/
@Service
@Slf4j
public class SettleServiceImpl implements SettleService {

    @Autowired
    private PushService pushService;

    @Autowired
    private PokerService pokerService;

    @Autowired
    private PlayerService playerCacheService;

    @Autowired
    private RobotCacheService robotCacheService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void settle(int roomId) {
        log.info("==>房间:{},进入结算", roomId);
        Room room = RoomManager.intance().getRoom(roomId);
        if (room.getRoomStatus() != RoomStatus.DEAL) {
            log.error("==> 发牌失败,房间{} 状态有误,当前状态为{}", roomId, room.getRoomStatus());
            return;
        }
        long start = System.currentTimeMillis();
        Double controlExecuteRate = RoomManager.intance().getControlExecuteRate(room.getLevel());
        // 是否控制
        boolean control = false;
        // false输 true赢
        boolean win = false;
        int random = RandomUtil.randomInt(100);
        // controlExecuteRate>0控制赢 <0控制输
        if (controlExecuteRate >= 0) {
            win = true;
            if (random <= controlExecuteRate) {
                control = true;
            }
        } else {
            if (random <= Math.abs(controlExecuteRate)) {
                control = true;
            }
        }
        if (control) {
            this.dealAlgorithm(room, win);
        } else {
            this.dealNormal(room);
        }
        log.info("场次:{}, 控制率为:{},随机数为:{},当局是否控制:{},控制输赢为:{}", room.getLevel(), controlExecuteRate, random, control, win);
        this.calculateWin(room);
        this.settleResult(room);
        log.info("================> 房间:{},结算耗时:{}", room.getRoomId(), System.currentTimeMillis() - start);
        try {
            room.setRoomStatus(RoomStatus.SETTLE);
            room.setCountdownTime(System.currentTimeMillis() + D.SETTLE_TIME);
            TimerManager.instance().submitDelayWork(StartGameDelayTask.class, D.SETTLE_TIME, roomId);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info("系统当前流水: {} ,场次: {} , 盈利: {} ,抽水: {}", RoomManager.intance().getSystemTurnover(), room.getLevel(), RoomManager.intance().getRoomWinGold(room.getLevel()), RoomManager.intance().getPumpMap().get(room.getLevel()));
        this.redisTemplate.opsForValue().set(RedisRegionConfig.FILE_WIN_GOLD_KEY + ":" + Constant.GAME_ID + "_" + room.getLevel(), RoomManager.intance().getRoomWinGold(room.getLevel()).intValue() + "");
        this.redisTemplate.opsForValue().set(RedisRegionConfig.FILE_PUMP_KEY + ":" + Constant.GAME_ID + "_" + room.getLevel(), RoomManager.intance().getPumpMap().get(room.getLevel()).intValue() + "");
        this.redisTemplate.opsForList().rightPush("bairenlonhu:" + Constant.GAME_ID + "_" + room.getLevel(), RoomManager.intance().getRoomWinGold(room.getLevel()).intValue() + "");
        this.redisTemplate.opsForList().rightPush("bairenlonhu_control:" + Constant.GAME_ID + "_" + room.getLevel(), control + "");
    }

    /**
     * @param room
     * @return void
     * @description: 正常发牌
     * @author mice
     * @date 2019/7/31
     */
    void dealNormal(Room room) {
        log.info("==>房间:{},开始发牌", room.getRoomId());
        // 洗牌
        Collections.shuffle(room.getPokerList());
        LinkedList<Poker> tablePokerList = room.getPokerList();
        int pokerIndex = 0;
        for (Table table : room.getTableMap().values()) {
            if (!TableEnum.getTableEnum(Integer.parseInt(table.getTableIndex())).getLicensing()) {
                continue;
            }
            List<Poker> pokers = new ArrayList<>();
            for (int j = 1; j < 2; j++) {
                pokers.add(tablePokerList.get(pokerIndex++));
            }
            table.setPokerList(pokers);
            int cardType = this.pokerService.getPokerType(pokers);
            table.setCardType(cardType);
        }
        boolean outMaxPayout = this.normalVirtualComputing(room);
        if (outMaxPayout) {
            log.warn("==> 房间:{},超过最大赔付限额,重新发牌", room.getRoomId());
            this.dealNormal(room);
        }
    }

    /**
     * @param room
     * @param win
     * @return void
     * @description: 算法发牌
     * @author mice
     * @date 2019/7/31
     */
    private void dealAlgorithm(Room room, boolean win) {
        log.info("==>房间:{},开始发牌", room.getRoomId());
        LinkedHashMap<Integer, HandPokerDTO> handPokerDTOMap = new LinkedHashMap<>();
        LinkedList<Poker> tablePokerList = room.getPokerList();
        Collections.shuffle(tablePokerList);
        int pokerIndex = 0;
        int i = 1;
        for (Table table : room.getTableMap().values()) {
            if (!TableEnum.getTableEnum(Integer.parseInt(table.getTableIndex())).getLicensing()) {
                continue;
            }
            List<Poker> pokers = new ArrayList<>();
            for (int j = 1; j < 2; j++) {
                pokers.add(tablePokerList.get(pokerIndex++));
            }
            int cardType = this.pokerService.getPokerType(pokers);
            HandPokerDTO handPokerDTO = new HandPokerDTO(cardType, pokers);
            handPokerDTOMap.put(i, handPokerDTO);
            i++;
        }
        // 能赢(输)的牌型 key:全排列key value:PokerInfo
        Map<Integer, PokerInfo> winGoldMap = new HashMap<>();
        for (Integer key : RoomManager.intance().getFullyArrangedMap().keySet()) {
            List<Integer> arrangedList = RoomManager.intance().getFullyArrangedMap().get(key);
            int index = 0;
            for (Table table : room.getTableMap().values()) {
                if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_HE.getTableIndex()))) {
                    continue;
                }
                table.setPokerList(handPokerDTOMap.get(arrangedList.get(index)).getPokerList());
                table.setCardType(handPokerDTOMap.get(arrangedList.get(index++)).getCardType());
            }
            winGoldMap.put(key, new PokerInfo(this.getSysWinGold(room)));
        }
        List<Integer> arrangedList;
        log.warn("==>winGoldMap.size()==" + winGoldMap.size());
        if (winGoldMap.size() == 0) {
            arrangedList = RoomManager.intance().getFullyArrangedMap().get(RandomUtil.randomInt(1, RoomManager.intance().getFullyArrangedMap().size()));
        } else {
            Object[] winGoldKeys = winGoldMap.keySet().toArray();
            int winKey = (int) winGoldKeys[RandomUtil.randomInt(0, winGoldKeys.length)];
            if (!win) {
                // 如果 系统输钱 超过底线 则判定当局系统必赢 重新计算
                if (winGoldMap.get(winKey).getGold().intValue() + RoomManager.intance().getMaxPayout(room.getLevel()) <= 0
                        && RoomManager.intance().getRoomWinGold(room.getLevel()).intValue() < RoomManager.intance().getMaxPayoutReferenceValue(room.getLevel())) {
                    log.warn("==> 房间{} :系统输钱 超过底线 判定当局系统必赢 重新计算", room.getRoomId());
                    this.dealAlgorithm(room, true);
                    return;
                }
            }
            arrangedList = RoomManager.intance().getFullyArrangedMap().get(winKey);
        }
        log.info("计算出的牌型组合为:{}", JSON.toJSONString(arrangedList));
        // 最后放入计算好的牌型
        int index = 0;
        for (Table table : room.getTableMap().values()) {
            if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_HE.getTableIndex()))) {
                continue;
            }
            table.setPokerList(handPokerDTOMap.get(arrangedList.get(index)).getPokerList());
            table.setCardType(handPokerDTOMap.get(arrangedList.get(index++)).getCardType());
        }
        int winCount = room.getWinCountMap().get(win) == null ? 0 : room.getWinCountMap().get(win);
        room.getWinCountMap().put(win, winCount + 1);
        log.info("==> 房间:{},当局输赢判定为:{},总输赢场次为:{}", room.getRoomId(), win, room.getWinCountMap());
    }

    /**
     * @Author liubo
     * @Description //TODO 计算系统赢钱
     * @Date 15:11 2019/10/24
     **/
    private BigDecimal getSysWinGold(Room room) {
        BigDecimal sysWinGold;
        BigDecimal playerWinGold = BigDecimal.ZERO;
        BigDecimal robotWinGold = BigDecimal.ZERO;
        Table longTable = null, huTable = null;
        for (Table table : room.getTableMap().values()) {
            if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_LONG.getTableIndex()))) {
                longTable = table;
            }
            if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_HU.getTableIndex()))) {
                huTable = table;
            }
        }
        TableEnum win = this.judgeWinner(longTable, huTable);
        for (Seat seat : room.getSeatMap().values()) {
            BigDecimal seatWinGold = BigDecimal.ZERO;
            if (seat.getBetChipMap().containsKey(win.getTableIndex())) {
                BigDecimal betChip = seat.getBetChipMap().get(win.getTableIndex());
                seatWinGold = seatWinGold.add(betChip.multiply(BigDecimal.valueOf(win.getTableMang())));
            }
            // 和得情况龙虎各只扣一半
            if (TableEnum.TABLE_HE == win) {
                for (Table table : room.getTableMap().values()) {
                    if (table.getTableIndex().equals(String.valueOf(win.getTableIndex()))) {
                        continue;
                    }
                    if (seat.getBetChipMap().containsKey(table.getTableIndex())) {
                        BigDecimal betChip = seat.getBetChipMap().get(table.getTableIndex());
                        seatWinGold = seatWinGold.add(betChip.divide(BigDecimal.valueOf(2)));
                    }
                }
            }
            if (!(seat.getPlayer() instanceof BaseRobot)) {
                playerWinGold = playerWinGold.add(seatWinGold);
            } else {
                robotWinGold = robotWinGold.add(seatWinGold);
            }
        }
        if (room.isSystemBanker()) {
            sysWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold);
        } else {
            BigDecimal bankerWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold).subtract(robotWinGold);
            sysWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold).subtract(bankerWinGold);
        }
        return sysWinGold;
    }

    /**
     * @param room
     * @return boolean true:超过最大赔付限制 false:正常
     * @description: 正常发牌计算
     * @author mice
     * @date 2019/9/29
     */
    private boolean normalVirtualComputing(Room room) {
        if (RoomManager.intance().getMaxPayout(room.getLevel()) + this.getSysWinGold(room).intValue() <= 0
                && RoomManager.intance().getRoomWinGold(room.getLevel()).intValue() < RoomManager.intance().getMaxPayoutReferenceValue(room.getLevel())) {
            return true;
        }
        return false;
    }

    /**
     * @param room
     * @description: 计算输赢分
     * @author mice
     * @date 2019/7/31
     */
    private void calculateWin(Room room) {
        log.info("==>房间:{},开始计算输赢分", room.getRoomId());
        Seat bankerSeat = room.getBanker();
        Table longTable = null, huTable = null;
        LinkedList<Object> winTableList = new LinkedList<>();
        LinkedList<Object> allTablePokerList = new LinkedList<>();
        for (Table table : room.getTableMap().values()) {
            if (!CollectionUtils.isEmpty(table.getPokerList())) {
                allTablePokerList.add(table.getPokerList().get(0));
            }
            if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_LONG.getTableIndex()))) {
                longTable = table;
            }
            if (table.getTableIndex().equals(String.valueOf(TableEnum.TABLE_HU.getTableIndex()))) {
                huTable = table;
            }
        }
        TableEnum win = this.judgeWinner(longTable, huTable);
        winTableList.add(win.getTableIndex());
        BigDecimal seatWinGold = BigDecimal.ZERO;
        BigDecimal playerWinGold = BigDecimal.ZERO;
        BigDecimal robotWinGold = BigDecimal.ZERO;
        for (Table table : room.getTableMap().values()) {
            if (TableEnum.getTableEnum(Integer.parseInt(table.getTableIndex())) == null) {
                continue;
            }
            // table输赢
            String tableIndex = table.getTableIndex();
            for (Seat seat : room.getSeatMap().values()) {
                if (seat.getBetChipMap().containsKey(tableIndex)) {
                    BigDecimal betChip = seat.getBetChipMap().get(tableIndex);
                    BigDecimal loseGold;
                    if (tableIndex.equals(String.valueOf(win.getTableIndex()))) {
                        loseGold = betChip.multiply(BigDecimal.valueOf(win.getTableMang()));
                        seatWinGold = seatWinGold.add(loseGold);
                    } else {
                        // 和得情况龙虎各只扣一半
                        if (TableEnum.TABLE_HE == win) {
                            loseGold = betChip.divide(BigDecimal.valueOf(2)).negate();
                            seatWinGold = seatWinGold.add(loseGold.negate());
                        } else {
                            loseGold = betChip.negate();
                        }
                    }
                    seat.setTotalWinGold(seat.getTotalWinGold().add(loseGold));
                    seat.getWinGoldMap().put(table.getTableIndex(), loseGold);
                    if (!(seat.getPlayer() instanceof BaseRobot)) {
                        playerWinGold = playerWinGold.add(seatWinGold);
                    } else {
                        robotWinGold = robotWinGold.add(seatWinGold);
                    }
                }
            }
        }
        // 添加龙虎报表记录
        this.redisUtil.lSet(RegionConfig.GAME_REPORT_FROM_MAP + ":" + Constant.GAME_ID + "_" + room.getLevel(), win.getTableIndex());
        if (this.redisUtil.lGetListSize(RegionConfig.GAME_REPORT_FROM_MAP + ":" + Constant.GAME_ID + "_" + room.getLevel()) > 200) {
            this.redisUtil.lTrim(RegionConfig.GAME_REPORT_FROM_MAP + ":" + Constant.GAME_ID + "_" + room.getLevel(), 1, -1);
        }
        BigDecimal sysWinGold;
        if (room.isSystemBanker()) {
            sysWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold);
        } else {
            BigDecimal bankerWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold).subtract(robotWinGold);
            sysWinGold = room.getCurRoundTotalBetChip().subtract(playerWinGold).subtract(bankerWinGold);
        }
        // 系统金币
        RoomManager.intance().addRoomWinGold(room.getLevel(), sysWinGold);
        // 庄家
        BigDecimal bankerWinGold = room.getCurRoundTotalBetChip().subtract(seatWinGold);
        bankerSeat.setCurWinGold(bankerSeat.getCurWinGold().add(bankerWinGold));
        bankerSeat.setTotalWinGold(bankerSeat.getTotalWinGold().add(bankerWinGold));
        bankerSeat.getWinGoldMap().put(String.valueOf(win.getTableIndex()), bankerWinGold);

        synchronized (room) {
            for (Seat seat : room.getSeatMap().values()) {
                if (!seat.isReady()) {
                    continue;
                }
                BigDecimal betChipTotal = seat.getBetChipMap().values().stream().reduce(BigDecimal::add).get();
                BigDecimal changeGold = BigDecimal.ZERO;
                for (String key : seat.getWinGoldMap().keySet()) {
                    changeGold = changeGold.add(seat.getWinGoldMap().get(key));
                }
                if (changeGold.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pump = changeGold.multiply(RoomManager.intance().getPumpRate(room.getLevel()).divide(BigDecimal.valueOf(100)));
                    changeGold = changeGold.subtract(pump);
                    if (!(seat.getPlayer() instanceof BaseRobot) || !room.isSystemBanker()) {
                        RoomManager.intance().addPump(room.getLevel(), pump);
                    }
                }
                changeGold = changeGold.setScale(2, BigDecimal.ROUND_HALF_UP);
                seat.setCurWinGold(changeGold);

                // 加回下注额(因为下注时已经扣除玩家金币)
                changeGold = changeGold.add(betChipTotal);
                seat.getPlayer().setGold(seat.getPlayer().getGold().add(changeGold));
                if (seat.getPlayer() instanceof BaseRobot) {
                    this.robotCacheService.update((BaseRobot) seat.getPlayer());
                } else {
                    this.playerCacheService.updatePlayer(seat.getPlayer());
                    SettleSendDto incGoldSendDto = SettleSendDto.builder()
                            .userId(seat.getPlayer().getUserId())
                            .changeGold(changeGold)
                            .build();
                    playerCacheService.settle(incGoldSendDto);
                }
            }
        }
        BigDecimal pump = bankerSeat.getCurWinGold().multiply(RoomManager.intance().getPumpRate(room.getLevel()).divide(BigDecimal.valueOf(100)));
        bankerSeat.setCurWinGold(bankerSeat.getCurWinGold().subtract(pump));
        if (!room.isSystemBanker()) {
            RoomManager.intance().addPump(room.getLevel(), pump);
        }
        if (!room.isSystemBanker() && bankerSeat.getWinGoldMap().size() != 0) {
            bankerSeat.getPlayer().setGold(bankerSeat.getPlayer().getGold().add(bankerSeat.getCurWinGold()));
            SettleSendDto settleSendDto = SettleSendDto.builder()
                    .userId(bankerSeat.getPlayer().getUserId())
                    .changeGold(bankerSeat.getCurWinGold())
                    .build();
            playerCacheService.settle(settleSendDto);
        }

    }

    /**
     * @Author liubo
     * @Description //TODO 比牌
     * @Date 11:41 2019/10/24
     **/
    private TableEnum judgeWinner(Table longTable, Table huTable) {
        if (longTable.getCardType() > huTable.getCardType()) {
            return TableEnum.TABLE_LONG;
        } else if (longTable.getCardType() == huTable.getCardType()) {
            return TableEnum.TABLE_HE;
        }
        return TableEnum.TABLE_HU;
    }

    /**
     * @param room
     * @return void
     * @description: 结算结果
     * @author mice
     * @date 2019/7/31
     */
    private void settleResult(Room room) {
        log.info("==>房间:{},结算结果", room.getRoomId());
        SettleResultDTO settleResultDTO = new SettleResultDTO();
        // 庄家结算信息
        Seat bankerSeat = room.getBanker();
        SettleResultDTO.SettleInfo bankerSettleInfo = new SettleResultDTO.SettleInfo();
        bankerSettleInfo.setCurWinGold(bankerSeat.getCurWinGold());
        bankerSettleInfo.setWinGoldMap(bankerSeat.getWinGoldMap());
        settleResultDTO.setBankerSettleInfo(bankerSettleInfo);
        settleResultDTO.setTableMap(room.getTableMap());

        if (room.getInfieldSeatMap().size() == 6) {
            // 统计外场人员赢钱数
            BigDecimal outfieldWinGold = new BigDecimal(0);
            synchronized (room) {
                for (Seat seat : room.getSeatMap().values()) {
                    if (seat.getSeatIndex() > 6 && seat.isReady()) {
                        outfieldWinGold = outfieldWinGold.add(seat.getCurWinGold());
                    }
                }
            }
            settleResultDTO.setOutfieldWinGold(outfieldWinGold);
        }
        // 包装内场人员数据
        for (Seat seat : room.getInfieldSeatMap().values()) {
            SettleResultDTO.SettleInfo infieldSettleInfo = this.settleInfoWarpper(seat);
            settleResultDTO.getInfieldSettleInfoMap().put(seat.getSeatIndex() + "", infieldSettleInfo);
        }
        room.setSettleResultDTO(settleResultDTO);
        // 向游戏中的玩家推送数据
        synchronized (room) {
            for (Seat seat : room.getSeatMap().values()) {
                if (seat.getPlayer() instanceof BaseRobot) {
                    continue;
                }

                if (!seat.isReady()) {
                    SettleResultDTO.SettleInfo selfSettleInfo = new SettleResultDTO.SettleInfo();
                    settleResultDTO.setSelfSettleInfo(selfSettleInfo);
                    MessageResult messageResult = new MessageResult(MessageIdConfig.SETTLE_RESULT_NTC, settleResultDTO);
                    this.pushService.push(seat.getPlayer().getUserId(), messageResult);
                    continue;
                }
                SettleResultDTO.SettleInfo selfSettleInfo = this.settleInfoWarpper(seat);
                settleResultDTO.setSelfSettleInfo(selfSettleInfo);
                MessageResult messageResult = new MessageResult(MessageIdConfig.SETTLE_RESULT_NTC, settleResultDTO);
                this.pushService.push(seat.getPlayer().getUserId(), messageResult);
            }
        }
        // 向庄家推送数据
        if (!room.isSystemBanker()) {
            SettleResultDTO.SettleInfo selfSettleInfo = this.settleInfoWarpper(bankerSeat);
            settleResultDTO.setSelfSettleInfo(selfSettleInfo);
            MessageResult messageResult = new MessageResult(MessageIdConfig.SETTLE_RESULT_NTC, settleResultDTO);
            this.pushService.push(bankerSeat.getPlayer().getUserId(), messageResult);
        }
    }

    private SettleResultDTO.SettleInfo settleInfoWarpper(Seat seat) {
        SettleResultDTO.SettleInfo settleInfo = new SettleResultDTO.SettleInfo();
        settleInfo.setGold(seat.getPlayer().getGold());
        settleInfo.setWinGoldMap(seat.getWinGoldMap());
        settleInfo.setCurWinGold(seat.getCurWinGold());
        settleInfo.setSeatIndex(seat.getSeatIndex());
        return settleInfo;
    }
}