package extract;

import java.util.ArrayList;
import java.util.List;

public class StartStopExtracter<S, T> {
	TargetFactory<T> targetFactory;

	public StartStopExtracter(TargetFactory<T> targetFactory) {
		this.targetFactory = targetFactory;
	}

	public List<StartStopExtractResult> extract(LineSource src) {
		List<StartStopExtractResult> result = new ArrayList<>();
		// int nr;
		// class LoopVar {
		// boolean started;
		// boolean stopped;
		// FileOutputStream fos;
		// int nr;
		// int sequence;
		// }
		// ;
		// final LoopVar lvI = new LoopVar();
		// lvI.started = false;
		// lvI.stopped = false;
		// lvI.nr = nr;
		// lvI.sequence = 0;
		// try {
		// String line;
		// int lineNumber = 0;
		// while (null != (line = src.nextElement())){
		//
		// String correctedLine = line;
		//
		// try {
		// if (!lvI.started) {
		// int idxStart = line.indexOf("<?xml");
		// if (idxStart >= 0) {
		// DataUtil.getInstance().close(lvI.fos);
		// String filename = targetdir + DataUtil.PATH_SEPARATOR
		// + DataUtil.getInstance().getFilenameWithoutExtension(fn.getId()) +
		// "."
		// + (lvI.nr + lvI.sequence++) + ".request.xml";
		// lvI.fos = new FileOutputStream(filename);
		// lvI.started = true;
		// correctedLine = line.substring(idxStart);
		// } else
		// correctedLine = null;
		// }
		// if (lvI.started) {
		// int idxStop = line.indexOf("Envelope>");
		// if (idxStop >= 0) {
		// lvI.stopped = true;
		// correctedLine = line.substring(0, idxStop + "Envelope>".length());
		// }
		// }
		// if (null != correctedLine) {
		// lvI.fos.write(line.getBytes());
		// }
		// if (lvI.stopped) {
		// lvI.stopped = false;
		// lvI.started = false;
		// DataUtil.getInstance().close(lvI.fos);
		//
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// });
		//
		// } finally {
		// DataUtil.getInstance().close(lvI.fos);
		// }
		// nr += lvI.sequence;
		// LogUtil.getInstance().info("Created [" + nr + "] files");
		return result;
	}
}
