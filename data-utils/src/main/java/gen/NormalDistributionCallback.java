package gen;

import java.util.Random;

public class NormalDistributionCallback implements GenerationCallback {
	private int mean;
	private int stddev;
	private NormalDistribution nd;

	public NormalDistributionCallback(int mean, int stddev) {
		this.mean = mean;
		this.stddev = stddev;
	}

	@Override
	public String[] generate(int rowId, int totalRows) {
		return new String[] { String.valueOf(getNormalDistribution(1000).get(new Random().nextInt(1000))) };
	}

	public NormalDistribution getNormalDistribution(int totalRows) {
		if (null == this.nd)
			this.nd = new NormalDistribution(mean, stddev, totalRows);
		return this.nd;
	}
}
