package gen;

import java.util.Random;

public class RandomDistributionCallback implements GenerationCallback {
	private int mean;
	private int deviation;

	public RandomDistributionCallback(int mean, int deviation) {
		this.mean = mean;
		this.deviation = deviation;
	}

	@Override
	public String[] generate(int rowId, int totalRows) {
		return new String[] { String
				.valueOf(mean + new Random().nextInt(deviation) * ((new Random().nextInt(2) % 2 == 0) ? -1 : 1)) };
	}

}
