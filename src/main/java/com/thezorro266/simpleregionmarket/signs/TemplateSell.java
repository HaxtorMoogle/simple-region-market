package com.thezorro266.simpleregionmarket.signs;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.handlers.LanguageHandler;

/**
 * @author theZorro266
 * 
 */
public class TemplateSell extends TemplateMain {
	public TemplateSell(SimpleRegionMarket plugin, LanguageHandler langHandler, TokenManager tokenManager, String tplId) {
		super(plugin, langHandler, tokenManager);
		id = tplId;
		load();
	}
}
