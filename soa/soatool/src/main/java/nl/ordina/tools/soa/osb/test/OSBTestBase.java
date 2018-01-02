package nl.ordina.tools.soa.osb.test;

import data.ConfigurationUtil;

public class OSBTestBase {
	public void readConfig() {
		ConfigurationUtil.getInstance().init(null, "tool");
	}
}
