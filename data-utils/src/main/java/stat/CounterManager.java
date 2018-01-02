package stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import data.LogUtil;

public class CounterManager {
	private Map<String, Counter> counters = new HashMap<>();

	public void resetCounters() {
		counters = new HashMap<>();
	}

	public void incrementOk(String name) {
		incrementOk(name, 1);
	}

	public void incrementOk(String name, int n) {
		Counter counter = counters.get(name);

		if (null != counter)
			counter.incrementOk(n);
		else {
			counter = new Counter();
			counter.incrementOk(n);
			counters.put(name, counter);
		}
	}

	public void incrementFault(String name) {
		incrementFault(name, 1);
	}

	public void incrementFault(String name, int n) {
		Counter counter = counters.get(name);

		if (null != counter)
			counter.incrementFault(n);
		else {
			counter = new Counter();
			counter.incrementFault(n);
			counters.put(name, counter);
		}
	}

	public void printStat(int okCount, int faultCount) {
		LogUtil.getInstance().info("Total [" + (okCount + faultCount) + "]");
		LogUtil.getInstance().info("   OK [" + okCount + "]");
		LogUtil.getInstance().info("FAULT [" + faultCount + "]");
		LogUtil.getInstance().info("Score [ " + getPercentage(okCount, faultCount) + "]");
	}

	public void printStats() {
		int totalOk = 0, totalFault = 0;
		for (Entry<String, Counter> e : counters.entrySet()) {
			LogUtil.getInstance().info("*** STATS[" + e.getKey() + " - " + e.getKey() + "]***");
			printStat(e.getValue().getOkCount(), e.getValue().getFaultCount());
			totalOk += e.getValue().getOkCount();
			totalFault += e.getValue().getFaultCount();
		}
		LogUtil.getInstance().info("*** TOTALS ***");
		printStat(totalOk, totalFault);
	}

	public int getFaultTotal() {
		int result = 0;
		for (Entry<String, Counter> e : counters.entrySet()) {
			result += e.getValue().getFaultCount();
		}

		return result;
	}

	public int getOkTotal() {
		int result = 0;
		for (Entry<String, Counter> e : counters.entrySet()) {
			result += e.getValue().getOkCount();
		}

		return result;
	}

	public int getPercentage(int okCount, int faultCount) {
		if (okCount == 0 && faultCount == 0)
			return 0;
		else
			return (okCount * 100) / (okCount + faultCount);
	}

}
