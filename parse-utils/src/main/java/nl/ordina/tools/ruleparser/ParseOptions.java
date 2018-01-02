package nl.ordina.tools.ruleparser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;

public class ParseOptions {
	public static final int USE_PREPROCESSED_CODE = 0x0001;
	public static final int CASE_INSENSITIVE = 0x0002;
	private List<ParseStep> parseSteps;
	private boolean caseInsensitive;

	public ParseOptions(int optionFlags) {
	}

	public ParseOptions() {
	}

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	public void addParseStep(Class<? extends org.antlr.v4.runtime.Parser> clsParser, Class<? extends Lexer> clsLexer,
			Class<? extends ParserRuleContext> clsContext, int flags) {
		getParseSteps().add(new ParseStep(clsParser, clsLexer, clsContext, flags));
	}

	public List<ParseStep> getParseSteps() {
		if (null == parseSteps)
			parseSteps = new ArrayList<>();
		return parseSteps;
	}

	public static class ParseStep {
		private boolean usePreprocessedCode;
		private boolean caseInsensitive;
		private Class<? extends org.antlr.v4.runtime.Parser> clsParser;
		private Class<? extends Lexer> clsLexer;
		private Class<? extends ParserRuleContext> clsContext;

		public ParseStep(Class<? extends org.antlr.v4.runtime.Parser> clsParser, Class<? extends Lexer> clsLexer,
				Class<? extends ParserRuleContext> clsContext, int flags) {
			this.clsParser = clsParser;
			this.clsLexer = clsLexer;
			this.clsContext = clsContext;
			if ((flags & USE_PREPROCESSED_CODE) > 0)
				this.usePreprocessedCode = true;
			if ((flags & CASE_INSENSITIVE) > 0)
				this.caseInsensitive = true;
		}

		public Class<? extends org.antlr.v4.runtime.Parser> getParserClass() {
			return clsParser;
		}

		public void setParserClass(Class<? extends org.antlr.v4.runtime.Parser> clsParser) {
			this.clsParser = clsParser;
		}

		public Class<? extends Lexer> getLexerClass() {
			return clsLexer;
		}

		public void setLexerClass(Class<? extends Lexer> clsLexer) {
			this.clsLexer = clsLexer;
		}

		public Class<? extends ParserRuleContext> getContextClass() {
			return clsContext;
		}

		public void setContextClass(Class<? extends ParserRuleContext> clsContext) {
			this.clsContext = clsContext;
		}

		public boolean isUsePreprocessedCode() {
			return usePreprocessedCode;
		}

		public void setUsePreprocessedCode(boolean usePreprocessedCode) {
			this.usePreprocessedCode = usePreprocessedCode;
		}

		public boolean isCaseInsensitive() {
			return caseInsensitive;
		}

		public void setCaseInsensitive(boolean caseInsensitive) {
			this.caseInsensitive = caseInsensitive;
		}

	}

	public boolean isReplaceContainername() {
		return true;
	}

	public boolean isTildeCorrection() {
		return true;
	}
}
