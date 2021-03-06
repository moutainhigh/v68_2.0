package com.dmg.gameconfigserver.model.vo.lhj;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Administrator
 * 老虎机库存控制
 */
@Data
public class LhjInventoryControlVO {
	/**
	 * 游戏id
	 */
	@NotNull(message = "gameId不能为空")
	private int gameId;
	/**
	 * 游戏名称
	 */
	private String gameName;
	/**
	 * redis名称
	 */
	private String redisName;
	/**
	 * 当前库存值
	 */
	private double currentInventory;
	/**
	 * 设置库存值(正数:系统赢,负数:系统输)
	 */
	@NotNull(message = "setInventory不能为空")
	private double setInventory;
	/**
	 * 启用模型
	 */
	@NotNull(message = "model不能为空")
	private int model;
	/**
	 * 比较类型(1:大于,2:大于等于,3:小于,4:小于等于)
	 */
	@NotNull(message = "type不能为空")
	private int type;
	/**
	 * 流水值
	 */
	@NotNull(message = "waterValue不能为空")
	private double waterValue;
}
