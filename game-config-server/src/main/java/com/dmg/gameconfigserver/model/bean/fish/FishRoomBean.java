package com.dmg.gameconfigserver.model.bean.fish;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dmg.gameconfigserverapi.fish.dto.FishRoomDTO;

/**
 * 捕鱼房间配置
 */
@TableName("f_fish_room")
public class FishRoomBean extends FishRoomDTO {
    /** 串行版本标识 */
    private static final long serialVersionUID = 1L;

    /**
     * 获取：串行版本标识
     *
     * @return 串行版本标识
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
