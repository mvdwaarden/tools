package nl.ordina.tools.ruleparser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public interface FoldTest {
	boolean doFold(ParserRuleContext ctx);

	boolean doCopy(ParseTree child);
}