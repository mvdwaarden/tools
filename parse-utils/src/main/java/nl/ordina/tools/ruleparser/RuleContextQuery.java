package nl.ordina.tools.ruleparser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import data.Filter;
import metadata.MetaAtom.BaseType;
import object.ObjectUtil;

/**
 * Convenience class for iterating through an ANTLR parse tree
 * 
 * @author mwa17610
 * @see RuleContextQueryList
 *
 */
public class RuleContextQuery {
	public enum Option {
		NOP, OPTION_FIND_FIRST, OPTION_FIND_ALL;
	}

	public enum Axis {
		PARENT, CHILD, SIBLING_LEFT, SIBLING_RIGHT, SIBLING_CHILD_LEFT, SIBLING_CHILD_RIGHT, SIBLING, SIBLING_CHILD
	}

	public enum StopOnFirst {
		YES, NO;
	}

	public enum IncludeStart {
		YES, NO;
	}

	public enum Direction {
		DOWN, UP, LEFT, RIGHT, BOTH;
	}

	private ParseTree ctx;

	public RuleContextQuery(ParseTree ctx) {
		this.ctx = ctx;
	}

	public RuleContextQuery findFirst(final Class<? extends ParserRuleContext> cls) {
		return findFirstParseTree(obj -> ObjectUtil.getInstance().isA(obj, new Class[] { cls }, true));
	}

	public boolean exists(final Class<? extends ParserRuleContext> cls) {
		return !findFirst(cls).isEmpty();
	}

	public int size() {
		int result = 0;

		if (ctx instanceof ParserRuleContext)
			result = ctx.getChildCount();

		return result;
	}

	public boolean isA(Class<?>[] classes) {
		boolean result = ObjectUtil.getInstance().isA(ctx, classes, true);

		return result;
	}

	public boolean isA(Class<?> cls) {
		boolean result = ObjectUtil.getInstance().isA(ctx, new Class[] { cls }, true);

		return result;
	}

	public boolean exists(final Class<? extends ParserRuleContext>[] classes) {
		return !findFirst(classes).isEmpty();
	}

	/**
	 * Find the first parser rule context which is of one of the specific
	 * classes provided
	 * 
	 * @param classes
	 * @return
	 */
	public RuleContextQuery findFirst(final Class<?>[] classes) {
		return findFirstParseTree(obj -> {
			boolean result = false;
			for (Class<?> cls : classes) {
				if (cls.isAssignableFrom(obj.getClass())) {
					result = true;
					break;
				}
			}
			return result;
		});
	}

	public boolean exists(final Class<?>[] classes, final String terminal) {
		return !findFirst(classes, terminal).isEmpty();
	}

	/**
	 * Find the first occurrence of a specific ParserRuleContext containing a
	 * terminal
	 * 
	 * @param cls
	 * @return
	 */
	public RuleContextQuery findFirst(final Class<?>[] classes, final String terminal) {
		return findFirstParseTree(obj -> {
			boolean result = false;
			for (Class<?> cls : classes) {
				if (cls.isAssignableFrom(obj.getClass())) {
					RuleContextQuery query = new RuleContextQuery((ParserRuleContext) obj);
					if (query.indexOf(terminal, 0) >= 0) {
						result = true;
						break;
					}
				}
			}
			return result;
		});
	}

	public boolean exists(final Class<? extends ParserRuleContext>[] classes, final String[] terminals) {
		return !findFirst(classes, terminals).isEmpty();
	}

	/**
	 * Find the first occurrence of a specific ParserRuleContext
	 * 
	 * @param classes
	 * @param terminals
	 * @return
	 */
	public RuleContextQuery findFirst(final Class<? extends ParserRuleContext>[] classes, final String[] terminals) {
		return findFirstParseTree(obj -> {
			boolean result = false;
			for (Class<? extends ParserRuleContext> cls : classes) {
				if (ObjectUtil.getInstance().isA(obj, new Class[] { cls }, true)) {
					RuleContextQuery query = new RuleContextQuery((ParserRuleContext) obj);
					if (query.contains(terminals, 0) >= 0) {
						result = true;
						break;
					}
				}
			}
			return result;
		});
	}

	/**
	 * Find the first occurrence of a specific ParserRuleContext based on a
	 * filter.
	 * 
	 * @param filter
	 * @return
	 */
	private RuleContextQuery findFirstParseTree(final Filter<ParseTree> filter) {
		class Locals {
			ParseTree result = null;
		}
		final Locals _locals = new Locals();

		walk(ctx, ctx -> {
			boolean result = false;
			if (filter.include(ctx)) {
				_locals.result = ctx;
				result = true;
			}
			return result;
		}, Axis.CHILD, StopOnFirst.YES);

		return new RuleContextQuery(_locals.result);
	}

	public boolean exists(final Filter<RuleContextQuery> filter, Axis axis) {
		return !findFirst(filter, axis).isEmpty();
	}

	/**
	 * Find the first occurrence of a specific ParserRuleContext based on a
	 * filter
	 * 
	 * @param filter
	 * @return
	 */
	public RuleContextQuery findFirst(final Filter<RuleContextQuery> filter, Axis axis) {
		class Locals {
			RuleContextQuery result = null;
			boolean found = false;
		}
		final Locals _locals = new Locals();

		walk(ctx, ctx -> {
			_locals.result = new RuleContextQuery(ctx);
			if (filter.include(_locals.result)) {
				_locals.found = true;
			}
			return _locals.found;
		}, axis, StopOnFirst.YES);

		if (!_locals.found)
			_locals.result = new RuleContextQuery(null);

		return _locals.result;
	}

	/**
	 * Find all ParserRuleContext occurences based on a filter.
	 * 
	 * @param filter
	 * @return
	 */
	public RuleContextQueryList filter(final Filter<RuleContextQuery> filter, Axis axis) {
		class Locals {
			List<RuleContextQuery> result = new ArrayList<>();
		}

		final Locals _locals = new Locals();

		walk(ctx, lctx -> {
			RuleContextQuery query = new RuleContextQuery(lctx);
			boolean result = false;
			if (filter.include(new RuleContextQuery(lctx))) {
				result = true;
				_locals.result.add(query);
			}
			return result;
		}, axis, StopOnFirst.NO);

		return new RuleContextQueryList(_locals.result);
	}

	public int indexOf(String token, int startIdx) {
		int result = -1;

		if (!isEmpty() && startIdx >= 0 && startIdx < ctx.getChildCount()) {
			for (int i = startIdx; i < ctx.getChildCount(); ++i) {
				org.antlr.v4.runtime.tree.ParseTree child = ctx.getChild(i);

				if (child instanceof TerminalNode && child.getText().equals(token)) {
					result = i;
					break;
				}
			}
		}

		return result;
	}

	public int indexOf(Class<? extends ParserRuleContext> cls, int startIdx) {
		int result = -1;

		if (!isEmpty() && startIdx >= 0 && startIdx < ctx.getChildCount()) {
			for (int i = startIdx; i < ctx.getChildCount(); ++i) {
				org.antlr.v4.runtime.tree.ParseTree child = ctx.getChild(i);

				if (child.getClass().isAssignableFrom(cls)) {
					result = i;
					break;
				}
			}
		}

		return result;
	}

	public int indexOf(RuleContextQuery query) {
		int result = -1;

		if (!isEmpty()) {
			for (int i = 0; i < ctx.getChildCount(); ++i) {
				org.antlr.v4.runtime.tree.ParseTree child = ctx.getChild(i);

				if (query.getContext().equals(child)) {
					result = i;
					break;
				}
			}
		}

		return result;
	}

	public int indexOf(String[] tokens, int startIdx) {
		int idxToken = 0;
		int idxFound = -1;
		while ((idxFound = indexOf(tokens[idxToken++], startIdx)) < 0 && idxToken < tokens.length)
			;

		return idxFound;
	}

	public int contains(String[] tokens, int startIdx) {
		int idxCurr;
		int idxToken = 0;
		int idxFound = -1;
		while (startIdx >= 0 && idxToken < tokens.length) {
			idxCurr = indexOf(tokens[idxToken], startIdx);
			if (idxCurr >= 0) {
				if (idxFound < 0)
					idxFound = idxCurr;

				startIdx = idxCurr + 1;

				++idxToken;
			} else {
				break;
			}
		}

		if (idxToken == tokens.length)
			return idxFound;
		else
			return -1;

	}

	public boolean isEmpty() {
		return ctx == null;
	}

	@SuppressWarnings("unchecked")
	public <T extends ParseTree> T getContext() {
		return (T) ctx;
	}

	public RuleContextQuery get(int idx) {
		RuleContextQuery result = null;
		if (null != ctx && idx >= 0 && idx < ctx.getChildCount()) {
			ParseTree pt = ctx.getChild(idx);
			if (pt instanceof ParserRuleContext)
				result = new RuleContextQuery((ParserRuleContext) pt);

		}
		if (null == result)
			result = new RuleContextQuery(null);

		return result;
	}

	public int getChildCount() {
		return ctx.getChildCount();
	}

	public String getText(int idx) {
		String result = "";
		if (null != ctx && idx >= 0 && idx < ctx.getChildCount())
			result = ctx.getChild(idx).getText();

		return result;
	}

	public String concat(int idx) {
		String result = "";

		if (null != ctx && idx >= 0 && idx < ctx.getChildCount()) {
			for (int i = idx; i < ctx.getChildCount(); ++i) {
				result += ctx.getChild(i).getText();
			}
		}

		return result;
	}

	public RuleContextQuery findFirstParent(Class<?>[] includeClasses,
			Class<? extends ParserRuleContext>[] ignoreClasses) {
		RuleContextQuery result = null;
		if (null != ctx) {
			ParseTree tmp = ctx;

			searchFirstParent: while (tmp.getParent() instanceof ParserRuleContext) {
				for (Class<?> cls : includeClasses) {
					if (tmp.getParent().getClass().equals(cls)) {
						result = new RuleContextQuery((ParserRuleContext) tmp.getParent());
						break searchFirstParent;
					}
				}
				if (null == ignoreClasses || ignoreClasses.length == 0
						|| ObjectUtil.getInstance().isA(tmp.getParent(), ignoreClasses, false))
					tmp = tmp.getParent();
				else
					break searchFirstParent;
			}
		}
		if (null == result)
			result = new RuleContextQuery(null);
		return result;
	}

	public RuleContextQuery findFirstParent(Class<?>[] includeClasses) {
		return findFirstParent(includeClasses, null);
	}

	public RuleContextQuery getParent() {
		RuleContextQuery result = null;

		if (null != ctx)
			result = new RuleContextQuery(ctx.getParent());
		else
			result = new RuleContextQuery(null);

		return result;
	}

	public List<RuleContextQuery> getChildren() {
		List<RuleContextQuery> result = new ArrayList<>();

		if (ctx instanceof ParserRuleContext)
			for (ParseTree child : ((ParserRuleContext) ctx).children)
				result.add(new RuleContextQuery(child));

		return result;
	}

	public RuleContextQuery getParent(Class<? extends ParserRuleContext>[] ignore) {
		RuleContextQuery result = new RuleContextQuery(null);
		ParseTree tmp = ctx;

		searchFirstParent: while (tmp.getParent() instanceof ParserRuleContext) {
			tmp = tmp.getParent();
			boolean found = false;
			findIgnore: for (Class<? extends ParserRuleContext> cls : ignore) {
				if (tmp.getClass().equals(cls)) {
					found = true;
					break findIgnore;
				}
			}
			if (!found) {
				result = new RuleContextQuery(tmp);
				break searchFirstParent;
			}

		}

		return result;
	}

	public RuleContextQueryList getPath() {
		return filter(query -> true, Axis.PARENT);
	}

	public RuleContextQuery getChild(Class<? extends ParserRuleContext>[] ignore) {
		RuleContextQuery result = null;

		if (ctx instanceof ParserRuleContext) {
			ParserRuleContext tmp = (ParserRuleContext) ctx;
			searchFirstChild: while (tmp.getChildCount() > 0 && null != getFirstParserRuleContextChild(tmp)) {
				tmp = (ParserRuleContext) getFirstParserRuleContextChild(tmp);
				boolean found = false;
				findIgnore: for (Class<? extends ParserRuleContext> cls : ignore) {
					if (tmp.getClass().equals(cls)) {
						found = true;
						break findIgnore;
					}
				}
				if (!found) {
					result = new RuleContextQuery(tmp);
					break searchFirstChild;
				}

			}
		}
		if (null == result)
			result = new RuleContextQuery(null);

		return result;
	}

	private ParserRuleContext getFirstParserRuleContextChild(ParserRuleContext tmp) {
		ParserRuleContext result = null;

		for (int i = 0; i < tmp.getChildCount(); ++i) {
			if (tmp.getChild(i) instanceof ParserRuleContext) {
				result = (ParserRuleContext) tmp.getChild(i);
				break;
			}
		}

		return result;
	}

	private boolean walk(ParseTree ctx, ParseTreeTest test, Axis direction, StopOnFirst stop) {
		boolean result;
		ParseTree ctxSiblingStart = ctx;
		Direction directionChild;
		IncludeStart includeStart;
		int maxDepth;
		switch (direction) {
		case SIBLING:
			directionChild = Direction.BOTH;
			includeStart = IncludeStart.NO;
			if (null != ctx)
				ctx = ctx.getParent();
			maxDepth = 1;
			break;
		case SIBLING_LEFT:
			directionChild = Direction.LEFT;
			includeStart = IncludeStart.NO;
			if (null != ctx)
				ctx = ctx.getParent();
			maxDepth = 1;
			break;
		case SIBLING_RIGHT:
			directionChild = Direction.RIGHT;
			includeStart = IncludeStart.NO;
			if (null != ctx)
				ctx = ctx.getParent();
			maxDepth = 1;
			break;
		case SIBLING_CHILD:
			directionChild = Direction.BOTH;
			includeStart = IncludeStart.NO;
			maxDepth = 1;
			break;
		case SIBLING_CHILD_LEFT:
			directionChild = Direction.LEFT;
			includeStart = IncludeStart.NO;
			maxDepth = 1;
			break;
		case SIBLING_CHILD_RIGHT:
			directionChild = Direction.RIGHT;
			includeStart = IncludeStart.NO;
			maxDepth = 1;
			break;
		case PARENT:
			directionChild = Direction.UP;
			includeStart = IncludeStart.YES;
			maxDepth = -1;
			break;
		case CHILD:
			directionChild = Direction.DOWN;
			maxDepth = -1;
			includeStart = IncludeStart.YES;
			break;
		default:
			directionChild = Direction.DOWN;
			includeStart = IncludeStart.YES;
			maxDepth = -1;
			break;
		}
		result = walk(ctx, ctxSiblingStart, test, 0, directionChild, includeStart, stop, maxDepth);

		return result;

	}

	/**
	 * Traverse the parse tree and execute a test
	 * 
	 * @param ctx
	 * @param test
	 * @param depth
	 *            (current depth)
	 * @param includeStart
	 * @param stop
	 * @param maxDepth
	 *            (-1 is recursive)
	 * @return
	 */
	private boolean walk(ParseTree ctx, ParseTree ctxSiblingStart, ParseTreeTest test, int depth, Direction direction,
			IncludeStart includeStart, StopOnFirst stop, int maxDepth) {
		boolean result = (null == ctx || includeStart != IncludeStart.YES) ? false : test.ok(ctx);

		if ((maxDepth == -1 || depth < maxDepth) && null != ctx && (!result || stop != StopOnFirst.YES)) {
			switch (direction) {
			case DOWN:
			case BOTH:
				for (int i = 0; i < ctx.getChildCount(); ++i) {
					result = walk(ctx.getChild(i), ctxSiblingStart, test, depth + 1, Direction.DOWN, IncludeStart.YES,
							stop, maxDepth);
					if (result && stop == StopOnFirst.YES)
						break;
				}
				break;
			case LEFT: {
				int i;
				for (i = 0; i < ctx.getChildCount(); ++i) {
					if (ctx.getChild(i).equals(ctxSiblingStart))
						break;
				}
				--i;
				for (; i >= 0; --i) {
					result = walk(ctx.getChild(i), ctxSiblingStart, test, depth + 1, Direction.DOWN, IncludeStart.YES,
							stop, maxDepth);
					if (result && stop == StopOnFirst.YES)
						break;
				}
			}
				break;
			case RIGHT: {
				int i;
				for (i = 0; i < ctx.getChildCount(); ++i) {
					if (ctx.getChild(i).equals(ctxSiblingStart))
						break;
				}
				++i;
				for (; i < ctx.getChildCount(); ++i) {
					result = walk(ctx.getChild(i), ctxSiblingStart, test, depth + 1, Direction.DOWN, IncludeStart.YES,
							stop, maxDepth);
					if (result && stop == StopOnFirst.YES)
						break;
				}
			}
				break;
			default:
				result = walk(ctx.getParent(), ctxSiblingStart, test, depth + 1, Direction.UP, IncludeStart.YES, stop,
						maxDepth);
				break;

			}
		}

		return result;

	}

	private interface ParseTreeTest {
		boolean ok(ParseTree parseTree);
	}

	public BaseType getResultType() {
		return ParseUtil.getInstance().getBaseType(ctx);
	}
}
