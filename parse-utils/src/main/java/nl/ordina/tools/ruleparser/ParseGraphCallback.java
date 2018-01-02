package nl.ordina.tools.ruleparser;

import org.antlr.v4.runtime.tree.ParseTree;

public interface ParseGraphCallback {
	ParseTreeNode createNode(ParseTree ctx);
}
