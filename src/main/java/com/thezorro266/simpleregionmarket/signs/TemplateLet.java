package com.thezorro266.simpleregionmarket.signs;

/**
 * @author theZorro266
 * 
 */
public class TemplateLet extends TemplateMain {
	/**
	 * Rent template attributes
	 */
	public int rentTimeMin = 0;
	public int rentTimeMax = -1;

	public TemplateLet(String tplId) {
		id = tplId;
		load();
	}
}
