package nl.ordina.tools.soa.test;

import java.io.File;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Assert;
import org.junit.Test;

import data.ConfigurationUtil;
import data.DataUtil;
import nl.ordina.tools.ruleparser.ParseUtil;
import nl.ordina.tools.soa.parsers.SQLOracleLexer;
import nl.ordina.tools.soa.parsers.SQLOracleParser;
import nl.ordina.tools.soa.parsers.SQLOracleParser.FileContext;
import nl.ordina.tools.writer.ParseTreeWriter;

public class SoaSyntaxTest {
	@Test
	public void testSyntax() {
		String[] files = new String[] { "create1.ddl","create2.ddl" };

		for (String file : files) {
			String ddl = DataUtil.getInstance()
					.readFromFile(ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + file);
			ParserRuleContext ctx = ParseUtil.getInstance().parse(ddl.toLowerCase(), SQLOracleParser.class, SQLOracleLexer.class,
					FileContext.class);
			Assert.assertTrue("problem parsing [" + file + "]", null != ctx);
			ParseTreeWriter wri = new ParseTreeWriter();

			DataUtil.getInstance().writeToFile("d:/tmp/" + file + ".xml", wri.write(ctx).toString());

		}
	}
}
