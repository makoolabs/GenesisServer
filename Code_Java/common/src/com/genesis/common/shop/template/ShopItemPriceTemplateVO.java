package com.genesis.common.shop.template;

import com.genesis.core.template.annotation.ExcelRowBinding;
import com.genesis.core.template.exception.TemplateConfigException;
import com.genesis.core.template.TemplateObject;
import com.genesis.core.template.annotation.ExcelCellBinding;

/**
 * 物品价格模板
 * 
 * @author CodeGenerator, don't modify this file please.
 */

@ExcelRowBinding
public abstract class ShopItemPriceTemplateVO extends TemplateObject {
	
	/** 普通商店货币类型 */
	@ExcelCellBinding(offset = 1)
	protected com.genesis.common.currency.Currency generalShopCurrencyType;

	/**  普通商店价格 */
	@ExcelCellBinding(offset = 2)
	protected long generalShopPrice;


	public com.genesis.common.currency.Currency getGeneralShopCurrencyType() {
		return this.generalShopCurrencyType;
	}

	public void setGeneralShopCurrencyType(com.genesis.common.currency.Currency generalShopCurrencyType) {
		if (generalShopCurrencyType == null) {
			throw new TemplateConfigException(this.getSheetName(), this.getId(),
					0, "[普通商店货币类型]generalShopCurrencyType不可以为空");
		}	
		this.generalShopCurrencyType = generalShopCurrencyType;
	}
	
	public long getGeneralShopPrice() {
		return this.generalShopPrice;
	}

	public void setGeneralShopPrice(long generalShopPrice) {
		this.generalShopPrice = generalShopPrice;
	}
	

	@Override
	public String toString() {
		return "ShopItemPriceTemplateVO{generalShopCurrencyType=" + generalShopCurrencyType + ",generalShopPrice=" + generalShopPrice + ",}";

	}
}