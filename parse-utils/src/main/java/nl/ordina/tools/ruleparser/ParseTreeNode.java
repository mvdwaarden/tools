package nl.ordina.tools.ruleparser;

import graph.dm.Node;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import data.StringUtil;

public class ParseTreeNode extends Node {
	ParseTree ctx;

	public ParseTreeNode(ParseTree ctx) {
		this.ctx = ctx;
	}

	public ParseTree getParseTree() {
		return ctx;
	}

	@Override
	public boolean treatAsSame(Node node) {
		boolean result = false;
		if (node instanceof ParseTreeNode && ((ParseTreeNode) node).getParseTree().equals(getParseTree()))
			result = true;

		return result;
	}

	@Override
	public String getId() {
		return "" + ctx.hashCode();
	}

	private String customDescription;

	@Override
	public String getDescription() {
		if (null == customDescription) {
			String append = "";
			for (int i = 0; i < getParseTree().getChildCount(); ++i) {
				if (getParseTree().getChild(i) instanceof TerminalNode) {
					if (append.length() > 0)
						append += "_";
					append += getParseTree().getChild(i).getText();
				}
			}
			if (!append.isEmpty())
				customDescription = StringUtil.getInstance().camelCaseFormat(append,
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.+=#$@!0123456789<>()[]", "_");
			if (null == customDescription || customDescription.isEmpty())
				customDescription = getParseTree().getClass().getSimpleName();
		}
		return customDescription;
	}

	@Override
	public String getName() {
		return getId();
	}
}
