package stat;

public class Counter {
	private int okCount;
	private int faultCount;

	public int getOkCount() {
		return okCount;
	}

	public int getFaultCount() {
		return faultCount;
	}

	public void incrementFault() {
		++faultCount;
	}

	public void incrementFault(int n) {
		faultCount += n;
	}

	public void incrementOk() {
		++okCount;
	}

	public void incrementOk(int n) {
		okCount += n;
	}

}