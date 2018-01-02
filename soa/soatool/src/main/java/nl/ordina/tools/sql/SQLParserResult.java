package nl.ordina.tools.sql;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import csv.CSVData;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import metadata.MetaData;

public class SQLParserResult {
	private SQLParser parser;
	private ParserRuleContext context;

	public SQLParserResult(SQLParser parser) {
		this.parser = parser;
	}

	public SQLParser getParser() {
		return parser;
	}

	public void setParser(SQLParser parser) {
		this.parser = parser;
	}

	public ParserRuleContext getContext() {
		return context;
	}

	public void setContext(ParserRuleContext context) {
		this.context = context;
	}

	public CSVData getEntityAttributeCSV() {
		return parser.getEntityAttributeCSV(context);
	}

	public Graph<Node, Edge<Node>> getMetaGraph() {
		return parser.getMetaGraph(context);
	}

	public MetaData getMetaData() {
		return parser.getMetaData(context);
	}

	public String getCreateScript() {
		StringBuilder result = new StringBuilder();
		ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(new ParseTreeListener() {
			@Override
			public void enterEveryRule(ParserRuleContext ctx) {
				String[] newLines = { "CreateTableContext", "CreateIndexContex", "AlterTableContext",
						"EnableCommentContext", "CommentContext", "LobContext", "XmltypeContext" };
				String test = ctx.getClass().getSimpleName();

				for (String br : newLines)
					if (result.length() != 0 && test.equals(br))
						result.append(";\r\n");

			}

			@Override
			public void visitErrorNode(ErrorNode node) {

			}

			@Override
			public void visitTerminal(TerminalNode node) {
				result.append(node.getText());
				result.append(" ");
			}

			@Override
			public void exitEveryRule(ParserRuleContext ctx) {

			}
		}, context);

		return result.toString();
	}

	public boolean isEmpty() {
		return null == context;
	}

}
