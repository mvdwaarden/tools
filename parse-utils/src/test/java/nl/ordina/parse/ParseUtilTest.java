package nl.ordina.parse;

import data.ConfigurationUtil;
import data.DataUtil;
import junit.framework.TestCase;
import nl.ordina.tools.parse.StatemachineLexer;
import nl.ordina.tools.parse.StatemachineParser;
import nl.ordina.tools.parse.StatemachineParser.AndContext;
import nl.ordina.tools.parse.StatemachineParser.ConditionContext;
import nl.ordina.tools.parse.StatemachineParser.GuardedTransitionContext;
import nl.ordina.tools.parse.StatemachineParser.OrContext;
import nl.ordina.tools.parse.StatemachineParser.StateContext;
import nl.ordina.tools.parse.StatemachineParser.StatemachineContext;
import nl.ordina.tools.ruleparser.ParseUtil;
import nl.ordina.tools.ruleparser.RuleContextQuery;
import nl.ordina.tools.ruleparser.RuleContextQuery.Axis;
import nl.ordina.tools.writer.ParseTreeWriter;

public class ParseUtilTest extends TestCase {
	private StatemachineContext readStatemachine(String name) {
		DataUtil.getInstance().getCurrentWorkingdir();
		String str = DataUtil.getInstance()
				.readFromFile(ConfigurationUtil.getInstance().getTestResourcesPath() + name + ".stm");
		StatemachineContext result = ParseUtil.getInstance().parse(str, StatemachineParser.class,
				StatemachineLexer.class, StatemachineContext.class);

		return result;
	}

	public void testStatemachine() {
		StatemachineContext ctx = readStatemachine("elevator");
		ParseTreeWriter gen = new ParseTreeWriter();

		StringBuilder buf = gen.write(ctx);
		DataUtil.getInstance().writeToFile("elevator.out", buf.toString());

		assertTrue(true);
		return;
	}

	/**
	 * Navigation based on statemachine, see also the syntax Statemachine.g4
	 * 
	 * <pre>
	 * 	transitions elevator
	 * 	 button2 floor1 AND button1 floor2 AND OR: 
	 * 		stop -> start
	 * 	 button1 floor1 AND button2 floor2 AND OR: 
	 * 		start -> stop
	 * </pre>
	 */

	public void testNavigation1() {
		StatemachineContext ctx = readStatemachine("elevator");

		RuleContextQuery query = new RuleContextQuery(ctx);

		assertEquals("number of states", query.filter(q -> q.isA(StateContext.class), Axis.CHILD).size(), 4);
		assertEquals("number of ands", query.filter(q -> q.isA(AndContext.class), Axis.CHILD).size(), 4);
		assertEquals("number of ors", query.filter(q -> q.isA(OrContext.class), Axis.CHILD).size(), 2);

		assertEquals("no left sibling",
				query.findFirst(StateContext.class).filter(q -> q.isA(StateContext.class), Axis.SIBLING_LEFT).size(),
				0);
		assertEquals("1 right sibling",
				query.findFirst(StateContext.class).filter(q -> q.isA(StateContext.class), Axis.SIBLING_RIGHT).size(),
				1);
		assertEquals("right sibling is start", query.findFirst(StateContext.class)
				.findFirst(q -> q.isA(StateContext.class), Axis.SIBLING_RIGHT).concat(0), "start");

		assertEquals(
				"right sibling of stop is start", query
						.findFirst(q -> q.isA(StateContext.class) && q.concat(0).equals("start")
								&& q.getParent().indexOf(q) == 0, Axis.CHILD)
				.findFirst(q -> q.isA(StateContext.class), Axis.SIBLING_RIGHT).concat(0), "stop");

		assertEquals(
				"right sibling of start is stop", query
						.findFirst(q -> q.isA(StateContext.class) && q.concat(0).equals("stop")
								&& q.getParent().indexOf(q) == 0, Axis.CHILD)
				.findFirst(q -> q.isA(StateContext.class), Axis.SIBLING_RIGHT).concat(0), "start");

		assertEquals(
				"left sibling of stop is start", query
						.findFirst(q -> q.isA(StateContext.class) && q.concat(0).equals("stop")
								&& q.getParent().indexOf(q) > 0, Axis.CHILD)
				.findFirst(q -> q.isA(StateContext.class), Axis.SIBLING_LEFT).concat(0), "start");

		assertFalse("condition context exist as a parent of the first state",
				query.findFirst(q -> q.isA(StateContext.class), Axis.CHILD)
						.findFirstParent(new Class[] { GuardedTransitionContext.class }).isEmpty());

		assertEquals("condition context exist as a parent of the first state",
				query.findFirst(q -> q.isA(ConditionContext.class), Axis.CHILD)
						.findFirstParent(new Class[] { GuardedTransitionContext.class }).indexOf(OrContext.class,0)

		, 0);
		return;
	}
}
