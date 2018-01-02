package org.oracle.plsql.analyzer;

import csv.CSVData;
import csv.CSVUtil;
import data.DataUtil;
import data.LogUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.fs.FileNode;
import graph.ext.dm.fs.FileSystemNode;
import graph.ext.mod.fs.FileSystemReader;
import nl.ordina.tools.ruleparser.ParseUtil;
import nl.ordina.tools.ruleparser.RuleContextQuery;
import nl.ordina.tools.soa.parsers.PLSQLLexer;
import nl.ordina.tools.soa.parsers.PLSQLParser;
import nl.ordina.tools.soa.parsers.PLSQLParser.Function_headingContext;
import nl.ordina.tools.soa.parsers.PLSQLParser.Procedure_headingContext;
import nl.ordina.tools.writer.ParseTreeWriter;

public class PLSQLAnalyzer {
	public static void main(String[] args) {
		PLSQLAnalyzer analyzer = new PLSQLAnalyzer();

		analyzer.analyze("D:/Data/mwa17610/Desktop/venj/1.64/ZMS-64/Database", "d:/tmp/zms");
	}

	public void analyze(String sourcedir, String targetdir) {
		CSVData result = new CSVData();
		result.setHeader(new String[] { "PATH", "NAME", "ERROR", "DETAIL" });
		DataUtil.getInstance().makeDirectories(targetdir + DataUtil.PATH_SEPARATOR);
		FileSystemReader fsr = new FileSystemReader();
		CSVData methods = new CSVData();

		Graph<FileSystemNode, Edge<FileSystemNode>> fs = fsr.read(sourcedir, "(.*\\.bdy)|(.*\\.sql)|(.*\\.pls)");

		for (FileSystemNode f : fs.filterNodes(f -> f instanceof FileNode)) {
			String body = DataUtil.getInstance().readFromFile(f.getId());
			try {
				PLSQLParser.FileContext ctx = ParseUtil.getInstance().parse(body.toLowerCase(), PLSQLParser.class,
						PLSQLLexer.class, PLSQLParser.FileContext.class);
				ParseTreeWriter wri = new ParseTreeWriter();
				StringBuilder res = wri.write(ctx);
				RuleContextQuery query = new RuleContextQuery(ctx);
				query.filter(
						_ctx -> _ctx.isA(Function_headingContext.class) || _ctx.isA(Procedure_headingContext.class),
						RuleContextQuery.Axis.CHILD)
						.forEach(_ctx -> methods.add(new String[] { f.getName(), _ctx.getText(1) }));
				DataUtil.getInstance().writeToFile(
						targetdir + DataUtil.PATH_SEPARATOR
								+ DataUtil.getInstance().getFilenameWithoutExtension(f.getId()) + ".xml",
						res.toString());
				LogUtil.getInstance().info("parsed [" + f.getId() + "]");
				result.add(new String[] { f.getId(), f.getId(), "ok" });
			} catch (Exception e) {
				String info = ParseUtil.getInstance().formatParserException(e);
				String detail = LogUtil.getInstance().formatExceptionInline(e);
				result.add(new String[] { f.getId(), f.getId(), info, detail });
				LogUtil.getInstance().error("problem parsing [" + f.getId() + "]", e);
			}
		}
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "problems.csv", result, ';');
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "methods.csv", methods, ';');
	}
}
