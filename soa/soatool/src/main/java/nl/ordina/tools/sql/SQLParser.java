package nl.ordina.tools.sql;

import java.util.Arrays;

import org.antlr.v4.runtime.ParserRuleContext;

import csv.CSVData;
import data.StringUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import metadata.MetaAtom;
import metadata.MetaAtom.BaseType;
import metadata.MetaComposite;
import metadata.MetaData;
import metadata.MetaElement;
import metadata.MetaRelation;
import metadata.MetaType;
import nl.ordina.tools.ruleparser.ParseUtil;
import nl.ordina.tools.ruleparser.RuleContextQuery;
import nl.ordina.tools.ruleparser.RuleContextQuery.Axis;

public abstract class SQLParser {
	public static final String[] getCsvEntityHeader() {
		return Arrays.copyOf(CSV_ENTITY_HEADER, CSV_ENTITY_HEADER.length);
	}

	private static final String[] CSV_ENTITY_HEADER = new String[] { "ENTITY", "ATTRIBUTE", "TYPE" };

	public abstract SQLParserResult parseDDL(String filename);

	public abstract Graph<Node, Edge<Node>> getMetaGraph(ParserRuleContext ctx);

	public abstract CSVData getEntityAttributeCSV(ParserRuleContext ctx);

	public abstract MetaData getMetaData(ParserRuleContext ctx);

	public CSVData getEntityAttributeCSV(ParserRuleContext ctx, Class<?> clsCreateTable, Class<?> clsAttribute,
			Class<?> clsType) {
		CSVData result = new CSVData();

		result.add(SQLParser.getCsvEntityHeader());

		RuleContextQuery query = new RuleContextQuery(ctx);

		if (!query.isEmpty()) {
			// For the CREATE TABLE definitions
			for (RuleContextQuery createTableQuery : query.filter(lctx -> lctx.isA(clsCreateTable), Axis.CHILD)
					.getQueries()) {
				boolean somethingAdded = false;
				// For all the attribute definitions within CREATE TABLE
				for (RuleContextQuery attributeQuery : createTableQuery
						.filter(lctx -> lctx.isA(clsAttribute), Axis.CHILD).getQueries()) {
					// Get the row (also getting the type)
					String[] row = new String[] { makeValidName(createTableQuery.getText(2)),
							makeValidName(attributeQuery.getText(0)), makeValidName(
									attributeQuery.filter(lctx -> lctx.isA(clsType), Axis.CHILD).get(0).getText(0))

					};
					result.add(row);
					somethingAdded = true;
				}
				// Make sure we also add tables/entities without any attributes
				if (!somethingAdded) {
					result.add(new String[] { makeValidName(createTableQuery.getText(2)), "empty", "empty" });
				}
			}
		}

		return result;
	}

	public Graph<Node, Edge<Node>> getMetaGraph(ParserRuleContext ctx, Class<?> clsCreateTable, Class<?> clsAlterTable,
			Class<?> clsForeignKey) {
		Graph<Node, Edge<Node>> result = new Graph<>();

		RuleContextQuery query = new RuleContextQuery(ctx);

		if (!query.isEmpty()) {
			// find the tables using CREATE TABLE
			for (RuleContextQuery lquery : query.filter(lctx -> lctx.isA(clsCreateTable), Axis.CHILD).getQueries()) {
				Node node = new Node();
				String tableName = lquery.getText(lquery.indexOf("table", 0) + 1);
				node.setId(tableName);
				node.setDescription(makeValidName(tableName));
				node.setName(makeValidName(tableName));
				result.addNode(node);
			}
			// find the foreignkeys either on ALTER TABLE OR CREATE TABLE
			for (RuleContextQuery lquery : query.filter(lctx -> lctx.isA(clsForeignKey), Axis.CHILD).getQueries()) {
				String target = lquery.getText(lquery.indexOf("references", 0) + 1);
				String source = lquery
						.filter(lctx -> lctx.isA(new Class[] { clsAlterTable, clsCreateTable }), Axis.PARENT)
						.getQueries().get(0).getText(2);

				Node sn = new Node();
				sn.setId(source);
				sn.setDescription(makeValidName(source));
				sn.setName(makeValidName(source));
				Node tn = new Node();
				tn.setId(target);
				tn.setDescription(makeValidName(target));
				tn.setName(makeValidName(target));
				// String sourceAttr = lquery.getText(3);
				// String targetAttr = lquery.getText(8);
				Edge<Node> edge = new Edge<>(sn, tn, "REFERENCES");
				// edge.setDescription(sourceAttr + " -> " + targetAttr);
				result.addEdge(edge);
			}
		}

		return result;
	}

	private String makeValidName(String str) {
		return StringUtil.getInstance().camelCaseFormat(str, "àáâãäåçèéêëìíîïòóôõö", "aaaaaaceeeeiiiiooooo",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._", " ", false);
	}

	public MetaData getMetaData(ParserRuleContext ctx, Class<?> clsCreateTable, Class<?> clsAlterTable,
			Class<?> clsAttributeContext, Class<?> clsDeleteOnCascade, Class<?> clsForeignKeyConstraint) {
		final MetaData result = new MetaData();

		RuleContextQuery qry = new RuleContextQuery(ctx);

		qry.filter(q -> q.isA(clsCreateTable), Axis.CHILD).forEach(q -> {
			MetaComposite type = new MetaComposite();
			type.setName(makeValidName(q.getText(2)));
			q.filter(e -> e.isA(clsAttributeContext), Axis.CHILD).forEach(a -> {
				MetaElement el = new MetaElement();
				el.setName(makeValidName(a.getText(0)));
				String name = makeValidName(a.concat(1));
				MetaType tmp = result.getTypeByName(name);
				if (null == tmp) {
					MetaAtom atom = (MetaAtom) tmp;
					atom = new MetaAtom();
					atom.setName(name);
					if (name.toLowerCase().contains("varchar"))
						atom.setBaseType(BaseType.STRING);
					else if (name.toLowerCase().contains("number"))
						atom.setBaseType(BaseType.FLOAT);
					else if (name.toLowerCase().contains("integer"))
						atom.setBaseType(BaseType.INT);
					else if (name.toLowerCase().contains("datetime"))
						atom.setBaseType(BaseType.DATETIME);
					else if (name.toLowerCase().contains("date"))
						atom.setBaseType(BaseType.DATE);
					else
						atom.setBaseType(BaseType.STRING);
					result.addType(atom);
					tmp = atom;
				}
				el.setType(tmp);
				type.addElement(el);
			});
			result.addType(type);

		});

		qry.filter(q -> q.isA(clsForeignKeyConstraint), Axis.CHILD).forEach(fk -> {
			ParserRuleContext fkc = fk.getContext();
			ParserRuleContext tmp = fk.filter(p -> p.isA(clsCreateTable) || p.isA(clsAlterTable), Axis.PARENT).get(0)
					.getContext();
			String typeFrom = makeValidName(ParseUtil.getInstance().getText(tmp, "name"));
			MetaComposite from = result.<MetaComposite>getTypeByName(typeFrom);
			String typeTo = makeValidName(ParseUtil.getInstance().getText(fkc, "table"));
			String elTo = makeValidName(ParseUtil.getInstance().getText(fkc, "to"));
			MetaComposite to = result.<MetaComposite>getTypeByName(typeTo);
			if (null == to || null == from)
				return;
			boolean containment = (fk.filter(c -> c.isA(clsDeleteOnCascade), Axis.CHILD).isEmpty()) ? false : true;
			// add constraint <name> foreign key (<attr>) references (<attr>)
			// <table> (<attr>)
			// means (~90% the foreign key source is a child)
			MetaRelation rel = new MetaRelation(
					makeValidName(ParseUtil.getInstance().getText(fk.getParent().getContext(), "name")));
			MetaElement el = new MetaElement();
			el.setName(rel.getName());
			el.setType(from);
			rel.addSource(to.getElementByName(elTo));
			to.getElements().add(el);
			rel.setTarget(el);
			rel.setContainment(containment);
			result.addRelation(rel);
		});
		result.resolveTypes();
		return result;
	}
}
