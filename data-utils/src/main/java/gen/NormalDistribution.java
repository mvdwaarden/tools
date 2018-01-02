package gen;

public class NormalDistribution {
	public static final double SQRT_2_PI = Math.sqrt(2 * Math.PI);
	private double[] samples;

	public NormalDistribution(double mean, double stddev, int sampleLength) {
		this.samples = new double[sampleLength];

		for (int i = 0; i < samples.length; ++i) {
			samples[i] = Math.exp(-0.5*(Math.pow((2*mean*i/samples.length - mean) / stddev,2))) / (SQRT_2_PI * mean);
		}
	}

	public double get(int idx) {
		double result = 0.0;
		if (samples.length > idx && idx >= 0)
			result = samples[idx];

		return result;
	}
}
