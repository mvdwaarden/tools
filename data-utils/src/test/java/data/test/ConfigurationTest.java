package data.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import data.ConfigurationUtil;

public class ConfigurationTest {
	@Before
	public void setUp() {
		ConfigurationUtil.getInstance().clear();
		ConfigurationUtil.getInstance()
				.init(ConfigurationUtil.getInstance().getTestResourcesPath() + "configuration.test.properties");
	}

	@Test
	public void testSubstitution() {
		Assert.assertEquals("helloworld",ConfigurationUtil.getInstance().getSetting("main"));
		Assert.assertEquals("project_helloworld",ConfigurationUtil.getInstance().getSetting("project.name"));
		Assert.assertEquals("d:/whatever/project_helloworld",ConfigurationUtil.getInstance().getSetting("project.outdir"));
		Assert.assertEquals("d:/whatever/project_helloworld",ConfigurationUtil.getInstance().getSetting("project.outdir"));
		Assert.assertEquals("project_helloworld_helloworld//helloworld//jaja",ConfigurationUtil.getInstance().getSetting("project.name.more"));
		Assert.assertEquals("project_helloworld/part1/part2",ConfigurationUtil.getInstance().getSetting("total"));
	}
	
	@Test
	public void testRecursion() {
		Assert.assertEquals("test/test/test/${project.cycle.name2}",ConfigurationUtil.getInstance().getSetting("project.cycle.name2"));		
	}
}
