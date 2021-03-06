/**
 * 
 */
package com.dmg.clubserver.process;

import com.alibaba.fastjson.JSONObject;
import com.dmg.clubserver.result.MessageResult;


/**
 * @author linanjun
 * 消息处理器通用继承接口
 */
public interface AbstractMessageHandler {

	/**
	 * 消息号
	 * @return
	 */
	String getMessageId();

	/**
	 * 消息处理器
	 * @param userid 用户ID
	 * @param params 参数列表
	 * @return
	 */
	void messageHandler(String userid, JSONObject params,MessageResult result);
}
