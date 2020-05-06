package com.dmg.clubserver.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author mice
 * @Date 2019/5/28 11:01
 * @Version V1.0
 **/
@Data
public class RecommendClubVO implements Serializable {
    private Integer roleId;
    private Integer clubId;
}