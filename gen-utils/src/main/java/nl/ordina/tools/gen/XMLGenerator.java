package nl.ordina.tools.gen;

import java.util.Stack;

import nl.ordina.tools.ruleparser.RuleContextQuery;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Element;

import xml.XMLWriter;

public class XMLGenerator extends Generator implements RuleContextQueryGenerator {
	private Stack<Element> stack = new Stack<>();
	private XMLWriter writer;
	private Element root;

	public XMLGenerator(XMLWriter writer, Element root) {
		super(null);
		this.writer = writer;
		if (null == root)
			this.root = writer.getRoot();
		else
			this.root = root;

		stack.push(getRoot());
	}

	public XMLGenerator(XMLWriter writer) {
		this(writer, null);
	}

	@Override
	public void generate(RuleContextQuery query) {
		ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(new ParseTreeListener() {

			@Override
			public void visitTerminal(TerminalNode node) {
				Element el = writer.getDocument().createElement("terminal");
				el.setAttribute("value", node.getSymbol().getText());
				stack.peek().appendChild(el);
			}

			@Override
			public void visitErrorNode(ErrorNode node) {
			}

			@Override
			public void enterEveryRule(ParserRuleContext ctx) {
				Element el = writer.getDocument().createElement("rule");
				el.setAttribute("context", ctx.getClass().getSimpleName());
				stack.peek().appendChild(el);
				stack.push(el);
			}

			@Override
			public void exitEveryRule(ParserRuleContext ctx) {
				stack.pop();
			}
		}, query.getContext());
	}

	public Element getRoot() {
		return root;
	}
}
