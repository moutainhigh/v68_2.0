package com.dmg.bairenniuniuserver.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 结算数据
 * @return
 * @author mice
 * @date 2019/7/31
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    private int seatIndex; //座位号
    private BigDecimal winGold = new BigDecimal(0); // 总共赢钱

}
