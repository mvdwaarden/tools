package nl.ordina.tools.writer;

import metadata.MetaAtom.BaseType;
import nl.ordina.tools.ruleparser.ParseUtil;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Generic generator which creates a text based AST representation.
 * 
 * @author mwa17610
 *
 */
public class ParseTreeWriter {
	public StringBuilder write(ParserRuleContext ctx) {
		class Local {
			int depth;
			StringBuilder result;
		}
		final Local _local = new Local();
		_local.result = new StringBuilder();
		ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(new ParseTreeListener() {

			@Override
			public void visitTerminal(TerminalNode node) {
				appendDepth();
				_local.result.append("T_.[" + node.getSymbol().getText() + "]\n");
			}

			@Override
			public void visitErrorNode(ErrorNode node) {
			}

			@Override
			public void enterEveryRule(ParserRuleContext ctx) {
				appendDepth();
				_local.result.append("+");
				_local.result.append("R_.[" + ctx.getClass().getSimpleName() + "]");
				BaseType type = ParseUtil.getInstance().getBaseType(ctx);
				if (null != type)
					_local.result.append(" -> BaseType [" + type.name() + "]");
				_local.result.append("\n");
				++_local.depth;
			}

			@Override
			public void exitEveryRule(ParserRuleContext ctx) {
				--_local.depth;

			}

			private void appendDepth() {
				for (int i = 0; i < _local.depth; ++i)
					_local.result.append("--");
			}
		}, ctx);

		return _local.result;
	}
}
