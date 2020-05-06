/**
 * 
 */
package com.zyhy.lhj_server.game.fkmj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.zyhy.common_lhj.Window;
import com.zyhy.common_lhj.WindowInfo;
import com.zyhy.common_server.util.RandomUtil;
import com.zyhy.lhj_server.game.fkmj.poi.template.FkmjOdds;

/**
 * @author ASUS
 * 轮子权重
 */
public enum FkmjRollerWeightEnum {
	;
	//轴
	private int id;
	//位置
	private int index;
	//图标
	private FkmjIconEnum icon;
	//权重
	private int weight;
	
	private FkmjRollerWeightEnum(int id, int index, FkmjIconEnum icon, int weight){
		this.id = id;
		this.index = index;
		this.icon = icon;
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public FkmjIconEnum getIcon() {
		return icon;
	}

	public int getWeight() {
		return weight;
	}

	/**
	 * 获取图标
	 * @param i
	 * @return
	 **/
	public static Collection<? extends Window> windowInfo(int c, List<FkmjOdds> all) {
		List<FkmjWindowInfo> ws = new ArrayList<FkmjWindowInfo>();
		List<FkmjOdds> col = new ArrayList<>();
		List<FkmjIconEnum> icons = new ArrayList<>();
		// 取出一列的图标
		for (FkmjOdds odds : all) {
			if(odds.getCol() == c){
				col.add(odds);
			}
		}
		//System.out.println("col" + col);
		for (FkmjOdds odds : col) {
			FkmjIconEnum icon = null;
			for (FkmjIconEnum e : FkmjIconEnum.values()) {
				if (e.getName().equalsIgnoreCase(odds.getName())) {
					icon = e;
				}
			}
			icons.add(icon);
		}
		//System.out.println("icons" + icons);
		
		// 根据随机数取图标
		int random = RandomUtil.getRandom(0, icons.size() - 1);
		for (int i = 1; i <= 3; i++) {
			FkmjIconEnum icon = icons.get(random);
			ws.add(new FkmjWindowInfo(c, i, icon));
			random ++ ;
			if (random > (icons.size() - 1)) {
				random = 0;
			}
		}
		//System.out.println("本次取到的图标:" + ws);
		return ws;
	}
	
	/**
	 * 获取一个指定位置图标,并排除指定图标
	 * @param i
	 * @return
	 */
	public static FkmjWindowInfo windowInfo(int c,int d,List<FkmjWindowInfo> list, List<FkmjOdds> all) {
		List<FkmjWindowInfo> ws = new ArrayList<FkmjWindowInfo>();
		List<FkmjOdds> col = new ArrayList<>();
		List<FkmjIconEnum> icons = new ArrayList<>();
		// 取出一列的图标
		for (FkmjOdds odds : all) {
			if(odds.getCol() == c){
				col.add(odds);
			}
		}
		//System.out.println("col" + col);
		for (FkmjOdds odds : col) {
			FkmjIconEnum icon = null;
			for (FkmjIconEnum e : FkmjIconEnum.values()) {
				if (e.getName().equalsIgnoreCase(odds.getName())) {
					icon = e;
				}
			}
			icons.add(icon);
		}
		//System.out.println("icons" + icons);
		
		// 根据随机数取图标
		int random = RandomUtil.getRandom(0, icons.size() - 1);
		FkmjIconEnum icon = icons.get(random);
		
		if (list.size() == 0) {
			ws.add(new  FkmjWindowInfo(c, d, icon));
		} else {
			int count = 0;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getIcon() == FkmjIconEnum.BONUS && icon == FkmjIconEnum.BONUS) {
					count ++;
				} 
			}
			if (count > 0) {
				ws.add(windowInfo(c,d,list,all));
			}else {
				ws.add(new  FkmjWindowInfo(c, d, icon));
			}
		}
		//System.out.println("本次取到的图标:" + ws);
		return ws.get(0);
	}
}
