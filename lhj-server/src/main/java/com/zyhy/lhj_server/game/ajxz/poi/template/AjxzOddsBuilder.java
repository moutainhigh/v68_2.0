/**
 * 
 */
package com.zyhy.lhj_server.game.ajxz.poi.template;

import org.apache.poi.ss.usermodel.Row;

import com.zyhy.lhj_server.game.ajxz.poi.impl.BaseXlsTemplateBuilder;
import com.zyhy.lhj_server.game.ajxz.poi.impl.TemplateObject;



/**
 * @author linanjun
 *
 */
public class AjxzOddsBuilder implements BaseXlsTemplateBuilder {

	@Override
	public TemplateObject buildTemplate(Row row) {
		AjxzOdds f = new AjxzOdds();
		f.setId((int)row.getCell(0).getNumericCellValue());
		f.setCol((int)row.getCell(1).getNumericCellValue());
		f.setName(row.getCell(2).getStringCellValue());
		f.setType((int)row.getCell(3).getNumericCellValue());
		return f;
	}

}
