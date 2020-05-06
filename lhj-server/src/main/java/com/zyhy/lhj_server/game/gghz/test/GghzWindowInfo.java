/**
 * 
 */
package com.zyhy.lhj_server.game.gghz.test;

/**
 * @author linanjun
 * 视窗信息
 */
public class GghzWindowInfo {
	
	// 轴Id
	private int id;
	// 轴位置
	private int index;
	
	private GghzRollerInfo roller;
	

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public GghzRollerInfo getRoller() {
		return roller;
	}


	public void setRoller(GghzRollerInfo roller) {
		this.roller = roller;
	}


	@Override
	public String toString() {
		return "GghzWindowInfo [id=" + id + ", index=" + index + ", roller=" + roller + "]";
	}
	
	
}
