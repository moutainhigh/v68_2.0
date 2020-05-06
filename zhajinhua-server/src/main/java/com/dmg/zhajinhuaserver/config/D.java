//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.dmg.zhajinhuaserver.config;

public class D {
    public static final int HALL_NEED_FORWARD = 1;
    public static final int ZJH_NEED_FORWARD = 2;
    public static final int MJ_NEED_FORWARD = 3;
    public static final int NIOU_NIOU_NEED_FORWARD = 4;
    public static final int DDZ_NEED_FORWARD = 5;
    public static final int FREE_FIELD = 1;
    public static final int PRIVATE_FIELD = 2;
    public static final int MATCH_FIELD = 3;
    public static final int ROOMTYPE_RIGHT_MOVE = 28;
    public static final int ROOMCARD_RIGHT_MOVE = 4;
    public static final int PLAT_FORM_WEI_CHAT = 1;
    public static final int PLAT_FORM_TOURIST = 2;
    public static final int ROOM_MAX_TURNS = 15;
    public static final String LOBBY = "lobby";
    public static final String GAME = "game";
    public static final String PLAYER_DISSCONNECT = "disconnect";
    public static final String GAME_RULE = "rule";
    public static final String GAME_EXTRA_RULE = "extrarule";
    public static final String PLAYER_ISONLINE = "player_onlineNTC";
    public static final String PLAYER_MESSAGE_ERROR = "msgerror";
    public static final String CMD = "cmd";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String HEARTBEAT_TIME = "heart";
    public static final String HALL_HEART_TIME = "time";
    public static final String HALL_WHETHER_FORWARD = "forward";
    public static final String HALL_ERROR_NO_THIS_GAME_PLAYER = "The room was not found";
    public static final String HALL_MESSAGE_ERROR = "message_error";
    public static final String LEAVE_ROOM_MESSAGE_REASON = "leave_reason";
    public static final int ROOM_REWARD_MULTIPLE = 5;
    public static final String LOBBY_LOGIN_REQUEST = "entergame";
    public static final String LOGIN_SIGN_ERROR = "login authentication cannot be passed";
    public static final String LOGIN_ERROR_BY_NO_PLAYER = "did not find the corresponding player";
    public static final String HALL_GAME_NOTICE = "noticeNTC";
    public static final String HALL_GAME_NOTICE_INFO = "noticeInfo";
    public static final String HALL_GET_FREE_FIELD_ROOM_LIST = "freeFieldRoomList";
    public static final String HALL_GAME_TYPE = "gameType";
    public static final String CREATE_PRIVATE_ROOM = "createPrivateRoom";
    public static final String HALL_JOIN_ROOM = "cjoinRoom";
    public static final String HALL_ROOM_ID = "roomId";
    public static final String HALL_JOIN_ROOM_ADDRESS = "address";
    public static final String HALL_JOIN_ROOM_ERROR_NO_PSOITION = "there is no place in this position";
    public static final String HALL_JOIN_ROOM_ERROR_POSITION_NOT_EMPTY = "this position is not empty";
    public static final String HALL_JOIN_ROOM_ERROR_EXCEED_PEOPLE_NOT_EMPTY = "exceed the upper limit";
    public static final String HALL_JOIN_ROOM_ERROR_NOT_FOUNT_ROOM = "the room was not found";
    public static final String ROOM_LIST = "roomList";
    public static final String ROOM_ADD_REMOVE_ROBOT = "robotaddremove";
    public static final String ROOM = "room";
    public static final String JION_ROOM = "enterroom";
    public static final String QUICK_CHANGE_ROOM = "switchroom";
    public static final String SITDOWN = "sitdown";
    public static final String SITDOWNNTC = "sitdownNTC";
    public static final String PLAYERREADY = "gameready";
    public static final String PLAYERREADYNTC = "gamereadyNTC";
    public static final String PLAYER_LEAVEROOM = "leaveroom";
    public static final String GM_CHANGE_POKER = "gmchangepoker";
    public static final String PLAYER_FORCE_LEAVEROOM = "forceleaveroom";
    public static final String PLAYER_LEAVEROOMNTC = "leaveroomNTC";
    public static final String RESULT_ALL_INFO = "resultallNTC";
    public static final String GAME_START = "gamestartNTC";
    public static final String GAME_SEND_CARDS = "dealcardsNTC";
    public static final String TURN_ACTION = "todoNTC";
    public static final String ACTION_TYPE = "action_type";
    public static final String ACTION_TO_TIME = "action_to_time";
    public static final String XIA_ZHU_CHIPS = "xiazhu_chips";
    public static final String QIANG_ZHUANG_CHIPS = "qiangzhuang_chips";
    public static final String PLAYER_PLAY = "doaction";
    public static final String PLAYER_PLAY_RESULT = "doactionresultNTC";
    public static final String GAME_RESULT_MSG = "balanceNTC";
    public static final String GAME_CHAT_MSG = "chatMsg";
    public static final String GAME_CHAT_MSGNTC = "chatMsgNTC";
    public static final String INIT_ROOM_LIST = "initRoomList";
    public static final int DEFINENUM = 255;
    public static final String SYNCHRONOUS_CORRESPONDENCE = "synchronousCorrespondence";
    public static final String REMOVE_PRIVATE_ROOM = "removeRoom";
    public static final String SYNCHRONOUS_RECORD = "synchronousRecord";
    public static final String UPDATE_HALL_PLAYER_GOLD = "updateHallGold";
    public static final String UPDATE_GAME_PLAYER_GOLD = "updateGameGold";
    public static final String HALL_CONNECT_GAME = "connection";
    public static final String GOLD = "gold";
    public static final String DIAMOND = "diamond";
    public static final String SCORE = "score";
    public static final String DISOLUT_ROOM = "dissolut_room";
    public static final String ANSWER_DISOLUT_ROOM = "answer_dissolut_room";
    public static final String DISOLUT_ROOM_NTC = "dissolut_roomNTC";
    public static final String ANSWER_DISOLUT_ROOM_NTC = "answer_dissolut_roomNTC";
    public static final String SUCCESS_DISOLUT_ROOM_NTC = "success_dissolutNTC";
    public static final String TABLE_INFO = "table_info";
    public static final String TABLE_BASEINFO = "table_baseinfo";
    public static final String TABLE_INDEX = "table_index";
    public static final String TABLE_STATE = "room_state";
    public static final String TABLE_ROOM_TYPE = "room_type";
    public static final String TABLE_GAME_TYPE = "game_type";
    public static final String TABLE_CREATE_RID = "create_rid";
    public static final String TABLE_CREATE_ROLENAME = "create_rolename";
    public static final String TABLE_CREATE_TIME = "create_time";
    public static final String TABLE_LEVEL = "level";
    public static final String TABLE_MIN_CARRY = "min_carry";
    public static final String TABLE_LEAVE_CARRY = "leave_carry";
    public static final String TABLE_BASE_SCORE = "base_score";
    public static final String TABLE_TIP = "tip";
    public static final String TABLE_ALL_BETS = "all_bets";
    public static final String TABLE_MAX_PLAYER_NUM = "max_player_num";
    public static final String TABLE_CUR_PLAYER_NUM = "cur_player_num";
    public static final String TABLE_ACTION_SEAT_INDEX = "seat_index";
    public static final String TABLE_ACTION_SEAT_INDEX_LIST = "index_list";
    public static final String BANKER_MULTIPLE = "multipleBanker";
    public static final String TABLE_CUR_JUSHU = "cur_jushu";
    public static final String TABLE_CUR_TURN_NUM = "cur_turn_num";
    public static final String TABLE_MAX_TURN_NUM = "max_turn_num";
    public static final String TABLE_FENG_DING = "feng_ding";
    public static final String SEAT_INFO = "seat_info";
    public static final String SEAT_INDEX = "index";
    public static final String SEAT_STATE = "seat_state";
    public static final String SEAT_IS_TUOGUAN = "is_tuoguan";
    public static final String SEAT_DIAMOND = "diamond";
    public static final String SEAT_GOLD = "gold";
    public static final String SEAT_CARDSNUM = "cardsnum";
    public static final String SEAT_SEECARDS = "seecards";
    public static final String SEAT_AUTO_CALL = "autocall";
    public static final String SEAT_IS_READY = "is_ready";
    public static final String SEAT_READY_TO_TIME = "ready_to_time";
    public static final String SEAT_ACTION_TO_TIME = "action_to_time";
    public static final String PLAYER_CHIPS_LIST = "chipsList";
    public static final String SEAT_ALL_BETS = "seat_all_bets";
    public static final String SEAT_MESSAGE = "seat_msg";
    public static final String SEAT_PLAYER_ONLINE = "is_online";
    public static final String SEAT_IS_AUTOCALL = "is_autocall";
    public static final String SEAT_HAND_CARDS = "hand_cards";
    public static final String SEAT_ACTION_INFO = "action_info";
    public static final String SEAT_FOLLOW_CHIPS = "follow_chips";
    public static final String BIT_MONEY = "betMoney";
    public static final String PLAYER_SEAT_PLAYERINFO = "seat_playerinfo";
    public static final String PRIVATE_ROOM_RULES = "privateRoomRules";
    public static final String PLAYER_RID = "rid";
    public static final String PLAYER_ROLENAME = "rolename";
    public static final String PLAYER_LOGO = "logo";
    public static final String PLAYER_SEX = "sex";
    public static final String PLAYER_FANGKA = "fangka";
    public static final String PLAYER_INTRODUCE = "introduce";
    public static final String PLAYER_BASE_ACTION_OPER = "actiontype";
    public static final String PLAYER_CANCOMPARE_CARD_PLAYERLIST = "cancompareplayerslist";
    public static final String APPLY_LOOK_POKER = "lookPoker";
    public static final String POKER_VALUE = "pokerValue";
    public static final String LOOK_POKER_INDEX = "lookPlayerIndex";
    public static final String PLAYER_LOOK_POKER = "playerLookPokerIndex";
    public static final String OPEN_POKER = "openPoker";
    public static final String SURPLUS_MONEY = "surplusMoney";
    public static final String HAND_CARDS_TYPE = "cards_type";
    public static final String INTEGER_NUMBER_CARD_LIST = "main_cards";
    public static final String POINT_NUMBER_CARD_LIST = "sub_cards";
    public static final String READY_TO_TIME = "ready_to_time";
    public static final String ROLE_ID = "rid";
    public static final String NTC_SEND_READ_INFO = "toreadyNTC";
    public static final String DEAL_STAGE = "deal_stage";
    public static final String PLAYER_ACTION_TIMES = "times";
    public static final String DO_ACTION_RESULT_NTC = "doactionresultNTC";
    public static final String DO_TABLE_ACTION_NTC = "dotableactionNTC";
    public static final String IS_BANKER = "is_banker";
    public static final String QIANG_ZHUANG_TIMES = "qiangzhuang_times";
    public static final String XIA_ZHU_TIMES = "xiazhu_times";
    public static final String BANKER_FLAG = "banker_flag";
    public static final String ROOM_HAS_NO_EXIST = "1000";
    public static final String ROOM_HAS_STARTED = "1001";
    public static final String ROOM_HAS_EMPTY = "1002";
    public static final String ROOM_HAS_FULL = "1003";
    public static final String ROOM_IS_GAME = "1004";
    public static final String PLAYER_HAS_NO_EXIST = "10000";
    public static final String PLAYER_HAS_NO_SEAT = "10001";
    public static final String PLAYER_HAS_NO_MONEY = "10002";
    public static final String PLAYER_HAS_TOOMUCH_MONEY = "10003";
    public static final String PLAYER_HAS_SEEN_CARDS = "10004";
    public static final String PLAYER_HAS_PLAYING = "10005";
    public static final String PLAYER_HAS_NO_CARDS = "10006";
    public static final String PLAYER_HAS_ACTION_ERROR = "10007";
    public static final String PLAYER_HAS_NOT_INROOM = "10008";
    public static final String PLAYER_HAS_NOT_SEAT = "10009";
    public static final String REPEAT_TO_ROB_BANKER = "10009";
    public static final String PLAYER_NOT_ONSEAT = "10012";
    public static final String TOTAL_ROUND = "totalround";
    public static final String COST = "cost";
    public static final String TABLE_MAX_BETCHIP = "bet_chip_max";

    public D() {
    }
}
