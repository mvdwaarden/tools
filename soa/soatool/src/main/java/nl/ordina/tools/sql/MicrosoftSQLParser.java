package nl.ordina.tools.sql;

import org.antlr.v4.runtime.ParserRuleContext;

import csv.CSVData;
import data.DataUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import metadata.MetaData;
import nl.ordina.tools.ruleparser.ParseUtil;
import nl.ordina.tools.soa.parsers.SQLMicrosoftLexer;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.AlterTableContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.AttributeContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.CreateTableContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.FileContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.ForeignKeyConstraintContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.OnDeleteCascadeContext;
import nl.ordina.tools.soa.parsers.SQLMicrosoftParser.TypeContext;

public class MicrosoftSQLParser extends SQLParser {
	public SQLParserResult parseDDL(String filename) {
		String ddl = DataUtil.getInstance().readFromFile(filename);
		FileContext ctx = ParseUtil.getInstance().<FileContext>parse(ddl.toLowerCase(), SQLMicrosoftParser.class,
				SQLMicrosoftLexer.class, FileContext.class);

		SQLParserResult result = new SQLParserResult(this);
		result.setContext(ctx);

		return result;
	}

	@Override
	public Graph<Node, Edge<Node>> getMetaGraph(ParserRuleContext ctx) {
		return getMetaGraph(ctx, CreateTableContext.class, AlterTableContext.class, ForeignKeyConstraintContext.class);
	}

	@Override
	public CSVData getEntityAttributeCSV(ParserRuleContext ctx) {
		return getEntityAttributeCSV(ctx, CreateTableContext.class, AttributeContext.class, TypeContext.class);
	}

	@Override
	public MetaData getMetaData(ParserRuleContext ctx) {
		return getMetaData(ctx, CreateTableContext.class, AlterTableContext.class, AttributeContext.class,
				OnDeleteCascadeContext.class, ForeignKeyConstraintContext.class);
	}
}
