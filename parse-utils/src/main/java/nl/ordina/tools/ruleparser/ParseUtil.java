package nl.ordina.tools.ruleparser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import data.Filter;
import data.LogUtil;
import data.Test;
import data.Util;
import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.Graph;
import metadata.MetaAtom;
import metadata.MetaAtom.BaseType;
import metadata.MetaData;
import metadata.MetaElement;
import object.ObjectFactory;
import object.ObjectUtil;

/**
 * ANTLR parser wrapper for CogNiam rule parsers
 * 
 * The syntaxes are defined in main/antlr4 folder.
 * 
 * Other syntaxes are supported
 * 
 * @author mwa17610
 * 
 */
public class ParseUtil implements Util {
	public static final String GRAMMAR_DIR = "grammar.dir";
	public static final String PARSE_TREE_TYPE_FIELD = "_pu_type_";
	public static final String PARSER_METADATA_FIELD = "_pu_metadata_";
	public static final String PARSER_OPTION_PREFIX = "_pu_";
	public static final String PARSER_OPTION_POSTFIX = "_";
	public static final String PARSER_FLAG_START_CONDITION = "start_condition";
	private static final ThreadLocal<ParseUtil> instance = new ThreadLocal<ParseUtil>();
	private MetaData metadata;

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ParseUtil getInstance() {
		ParseUtil result = instance.get();

		if (null == result) {
			result = new ParseUtil();
			instance.set(result);
		}

		return result;
	}

	public MetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}

	/**
	 * Parse the rule definition.
	 */
	public <T extends ParserRuleContext> T parse(String str, Class<? extends Parser> clsParser,
			Class<? extends Lexer> clsLexer, Class<T> clsRuleContext, List<ParserFlag> flags) {
		return parse(str, clsParser, clsLexer, clsRuleContext, flags.toArray(new ParserFlag[] {}));
	}

	/**
	 * Parse the rule definition.
	 */
	public <T extends ParserRuleContext> T parse(String str, Class<? extends Parser> clsParser,
			Class<? extends Lexer> clsLexer, Class<T> clsRuleContext, ParserFlag... flags) {
		// Get the parser
		Parser parser = getParser(clsParser, clsLexer, new ANTLRInputStream(str));
		// If available set the metadata
		setMetaData(parser, metadata);
		if (null != flags)
			for (ParserFlag flag : flags)
				setParserFlag(parser, flag.getName(), flag.getValue());

		// Pass the start point
		T ruleContext = getRuleContext(parser, clsRuleContext);

		return ruleContext;
	}

	/**
	 * Get a parser object
	 * 
	 * @param clsParser
	 * @param clsLexer
	 * @param tokensSource
	 * @return
	 */
	public <P extends Parser, L extends Lexer> P getParser(Class<P> clsParser, Class<L> clsLexer,
			CharStream tokensSource) {
		Constructor<L> constrLexer = ObjectFactory.getInstance().<L>getConstructor(clsLexer,
				new Class[] { CharStream.class });
		L lexer = null;
		try {
			lexer = constrLexer.newInstance(new Object[] { tokensSource });
		} catch (Exception e) {
			LogUtil.getInstance().error("unable to create lexer", e);
		}

		// Get a list of matched tokens
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// Pass the tokens to the parser
		Constructor<P> constrParser = ObjectFactory.getInstance().<P>getConstructor(clsParser,
				new Class[] { TokenStream.class });

		P parser = null;
		try {
			parser = constrParser.newInstance(new Object[] { tokens });
			// Define error listener
			ANTLRErrorListener lsnr = new MyANTRErrorListener();
			// Register the error listener
			lexer.addErrorListener(lsnr);
			parser.addErrorListener(lsnr);
			// printGrammar(parser);
		} catch (Exception e) {
			LogUtil.getInstance().error("unable to create parser", e);
		}

		return parser;
	}

	@SuppressWarnings("unchecked")
	private <T extends ParserRuleContext> T getRuleContext(Parser parser, Class<T> clsRuleContext) {
		ParserRuleContext result = null;
		Method[] methods = parser.getClass().getDeclaredMethods();

		for (Method method : methods) {
			if (method.getReturnType() == clsRuleContext && method.getGenericParameterTypes().length == 0) {
				try {
					result = (T) method.invoke(parser);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					LogUtil.getInstance().error("error getting context ", e);
				} catch (InvocationTargetException e) {
					LogUtil.getInstance().error("error getting context ", e);
					throw new RuntimeException(e);
				}
			}
		}

		return (T) result;
	}

	/*
	 * Error handler. Throws RuntimeException if a parser error occurs.
	 */
	private static class MyANTRErrorListener implements ANTLRErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			throw new ParserException(
					"" + msg + " symbol[" + ((null != offendingSymbol) ? offendingSymbol.toString() : "?")
							+ "] at line [" + line + "] at [" + charPositionInLine + "]",
					line, charPositionInLine, e);
		}

		@Override
		public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
				BitSet ambigAlts, ATNConfigSet configs) {
		}

		@Override
		public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
				BitSet conflictingAlts, ATNConfigSet configs) {
		}

		@Override
		public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
				ATNConfigSet configs) {
		}
	}

	/* Cache */
	private Map<Class<? extends Parser>, List<String>> tokennamesMap = new HashMap<>();

	private List<String> getTokennames(Class<? extends Parser> clsParser, Class<? extends Lexer> clsLexer) {

		List<String> result = tokennamesMap.get(clsParser);

		if (result == null) {
			result = new ArrayList<>();
			tokennamesMap.put(clsParser, result);

			Parser parser = getParser(clsParser, clsLexer, new NullTokenStream());

			for (String token : parser.getTokenNames()) {
				result.add(token.replace("'", "").toLowerCase());
			}
		}
		return result;
	}

	public String getToken(Variable variable, Class<? extends Parser> clsParser, Class<? extends Lexer> clsLexer) {
		String result = null;
		String cmp = variable.getLocalname().toLowerCase();
		List<String> names = getTokennames(clsParser, clsLexer);
		for (String token : names) {
			if (cmp.equals(token)) {
				result = token;
				break;
			}
		}

		return result;
	}

	public String getText(ParserRuleContext ctx, String name) {
		CommonToken tmp = (CommonToken) ObjectUtil.getInstance().map(ctx, ParserRuleContext.class).get(name);
		String result;
		if (null != tmp)
			 result = tmp.getText();
		else
			result = "";

		return result;
	}

	private static class NullTokenStream implements CharStream {
		@Override
		public void consume() {
		}

		@Override
		public int LA(int i) {
			return 0;
		}

		@Override
		public int mark() {
			return 0;
		}

		@Override
		public void release(int marker) {

		}

		@Override
		public int index() {
			return 0;
		}

		@Override
		public void seek(int index) {
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public String getSourceName() {
			return null;
		}

		@Override
		public String getText(Interval interval) {
			return null;
		}
	}

	public Graph<ParseTreeNode, Edge<ParseTreeNode>> getParseGraph(ParserRuleContext ctx) {
		return getParseGraph(ctx, new ParseGraphCallback() {

			@Override
			public ParseTreeNode createNode(ParseTree ctx) {
				return new ParseTreeNode(ctx);
			}
		});
	}

	public Graph<ParseTreeNode, Edge<ParseTreeNode>> getParseGraph(ParserRuleContext ctx, final ParseGraphCallback cb) {
		final Graph<ParseTreeNode, Edge<ParseTreeNode>> result = new Graph<>();

		if (null != ctx) {
			ParseTreeWalker walker = new ParseTreeWalker();

			walker.walk(new ParseTreeListener() {
				@Override
				public void visitTerminal(TerminalNode node) {
				}

				@Override
				public void visitErrorNode(ErrorNode node) {
				}

				@Override
				public void exitEveryRule(ParserRuleContext ctx) {
					ParseTreeNode source = cb.createNode(ctx);
					if (null != cb) {
						for (int i = 0; i < ctx.getChildCount(); ++i) {
							if (!(ctx.getChild(i) instanceof TerminalNode)) {
								Edge<ParseTreeNode> edge = new Edge<>();
								edge.setSource(source);
								ParseTreeNode target = cb.createNode(ctx.getChild(i));
								if (null != target) {
									edge.setTarget(target);
									result.addEdge(edge, GraphOption.CHECK_DUPLICATES);
								}
							}
						}
					}
				}

				@Override
				public void enterEveryRule(ParserRuleContext ctx) {
				}
			}, ctx);
		}

		return result;
	}

	/**
	 * Plat slaan van een parse tree. Het platslaan wordt uitgevoerd op de
	 * doorgegeven context (er wordt geen copie gemaakt).
	 * 
	 * Verder vind het platslaan plaats vanaf de 'bladeren' naar de root.
	 * 
	 * <pre>
	 * B.v classes : new Class[]{A.class} A A - A * verwijderd, onderliggende
	 * nodes 'omhoog', ergo flatten * -- B - B -- C - C - A - A B B
	 * 
	 * <pre>
	 * 
	 * @see #foldToParent
	 * 
	 * @param query
	 * @param foldTest
	 * @param copyFilter
	 * @param offset
	 * @return
	 */
	public boolean flatten(RuleContextQuery query, Test<ParseTree> foldTest, Filter<ParseTree> copyFilter, int offset) {
		boolean result = false;
		// Recursie
		for (int i = 0; i < query.size(); ++i)
			flatten(query.get(i), foldTest, copyFilter, offset);
		// Huidige
		for (int i = 0; i < query.size(); ++i) {
			ParseTree ctx = query.get(i).getContext();
			if (ctx instanceof ParserRuleContext && foldTest.ok(ctx))
				zap((ParserRuleContext) ctx, copyFilter, offset);
		}
		return result;
	}

	/**
	 * Verplaats de childs van node [ctx] naar de parent.
	 * 
	 * Het verplaatsen gebeurt vanaf een offset. De node zelf verdwijnt hiermee.
	 * 
	 * @param ctx
	 * @param offset
	 * @return
	 */
	public boolean zap(ParserRuleContext ctx, Filter<ParseTree> filter, int offset) {
		boolean result = false;
		findChildInParent: for (int i = 0; i < ctx.getParent().getChildCount(); ++i) {
			if (ctx.getParent().getChild(i).equals(ctx)) {
				// Locatie in parent gevonden, voeg childs toe aan de
				// parent
				ctx.getParent().children.remove(i);
				for (int t = ctx.getChildCount() - 1; t >= offset; --t) {
					if (filter.include(ctx.getChild(t))) {
						ctx.getParent().children.add(i, ctx.getChild(t));
						if (ctx.getChild(t) instanceof RuleContext)
							((RuleContext) ctx.getChild(t)).parent = ctx.getParent();
					}
				}
				result = true;
				break findChildInParent;
			}
		}

		return result;
	}

	/**
	 * Assumes that a specific attribute of type nl.ordina.tools.ruleparser.Type
	 * has been added to a ParserRuleContext
	 * 
	 * @param ctx
	 * @return
	 */
	public BaseType getBaseType(ParseTree ctx) {
		BaseType result = null;
		if (null != ctx) {
			Field field;
			try {
				field = ctx.getClass().getField(PARSE_TREE_TYPE_FIELD);
				if (null != field)
					result = (BaseType) field.get(ctx);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
				LogUtil.getInstance().debug("type not found for [" + ctx.getClass().getSimpleName() + "]", e);
			}
		}

		if (result == null) {
			result = BaseType.UNKNOWN;
		}

		return result;
	}

	public void setMetaData(Parser parser, MetaData metadata) {
		if (null != metadata) {
			Field field;
			try {
				field = parser.getClass().getField(PARSER_METADATA_FIELD);
				if (null != field)
					field.set(parser, metadata);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
				LogUtil.getInstance().debug("no metadata field [" + parser.getClass().getSimpleName() + "]", e);
			}

		}
	}

	public void setParserFlag(Parser parser, String optionName, boolean value) {
		if (null != metadata) {
			Field field;
			try {
				field = parser.getClass().getField(PARSER_OPTION_PREFIX + optionName + PARSER_OPTION_POSTFIX);
				if (null != field)
					field.set(parser, value);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
				LogUtil.getInstance().debug("no option called [" + parser.getClass().getSimpleName() + "]", e);
			}

		}
	}

	public BaseType getBaseType(MetaData metadata, String name) {
		BaseType result = null;
		MetaData metaDataLookup = (null == metadata) ? this.metadata : metadata;

		if (null != metaDataLookup) {
			List<MetaElement> elements = metaDataLookup.findElementsByName(name);

			if (!elements.isEmpty() && elements.get(0).getType() instanceof MetaAtom)
				result = elements.get(0).<MetaAtom>getType().getBaseType();
		}

		return result;
	}

	public ParserException getParserException(Throwable e) {
		Throwable cause = e.getCause();
		while (null != cause && !(cause instanceof ParserException))
			cause = cause.getCause();

		if (cause instanceof ParserException)
			return (ParserException) cause;
		else
			return null;
	}

	public String formatParserException(Exception e) {
		String result;
		ParserException cause = getParserException(e);

		if (null != cause)
			result = "problem at [" + cause.getErrorLine() + "," + cause.getErrorPosition() + "]" + cause.getMessage();
		else
			result = LogUtil.getInstance().formatExceptionInline(e);

		return result;
	}
}
