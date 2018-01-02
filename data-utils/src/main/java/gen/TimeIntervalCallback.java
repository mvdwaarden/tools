package gen;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TimeIntervalCallback implements GenerationCallback {
	private Date start;
	private int durationSeconds;
	private int deviationSeconds;
	private NormalDistribution nd;
	private SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
	private SimpleDateFormat fmtIn = new SimpleDateFormat("yyyy-MM-dd");

	public TimeIntervalCallback(String start, String end, int durationSeconds, int deviationSeconds) {
		init(fmtIn.parse(start, new ParsePosition(0)), durationSeconds,
				deviationSeconds);
	}

	public TimeIntervalCallback(Date start, Date end, int durationSeconds, int deviationSeconds) {
		init(start, durationSeconds, deviationSeconds);
	}

	protected void init(Date start, int durationSeconds, int deviationSeconds) {
		this.start = start;
		this.durationSeconds = durationSeconds;
		this.deviationSeconds = deviationSeconds;
	}

	@Override
	public String[] generate(int rowId, int totalRows) {
		double duration = getNormalDistribution(1000).get(new Random().nextInt(1000));
		return new String[] { fmtOut.format(start), fmtOut.format(new Date(start.getTime() + (long)(duration*1000))) };
		
	}

	public NormalDistribution getNormalDistribution(int totalRows) {
		if (null == this.nd)
			this.nd = new NormalDistribution(durationSeconds, deviationSeconds, totalRows);
		return this.nd;
	}
}
