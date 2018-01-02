package nl.ordina.tools.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import csv.CSVData;
import csv.CSVUtil;
import data.DataUtil;
import gen.CSVCallback;
import gen.DataGenerationUtil;
import gen.GenerationCallback;
import gen.NameCallback;
import gen.RandomDistributionCallback;
import gen.TimeIntervalCallback;
import tool.Tool;

public class DataGenerationTool extends Tool {
	public static final String FUNCTION_GENERATION_TEST = "test";

	@Override
	public void dispatch(String function, String configuration, String sourcedir, String targetdir, String sourcefile,
			String[] args, Option... options) {

		if (function.equals(FUNCTION_GENERATION_TEST)) {
			CSVData csv = DataGenerationUtil.getInstance().generateData(

					new String[] { "Voornaam", "Achternaam", "Start", "Einde", "Locatie", "Zone" },
					new GenerationCallback[] { new NameCallback(),
							new TimeIntervalCallback("2016-10-15", "2016-12-16", 1000, 40),
							new RandomDistributionCallback(500, 50),
							new CSVCallback(sourcedir + DataUtil.PATH_SEPARATOR + "zones.csv", 0) },
					100);

			CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "csvout.csv", csv, ';');
			generateZoneData("zones.csv", "dayhourzone_zones_cummu.csv",
					new int[] { 20, 100, 50, 20, 30, 40, 80, 300, 1000, 3000, 2000, 4000, 6000, 7000, 3000, 2000, 1000,
							4000, 2000, 300, 200, 1000, 100, 50 },
					function, configuration, sourcedir, targetdir, sourcefile);
			generateZoneData("toilet.csv",
					"dayhourzone_toilet_cummu.csv", new int[] { 20, 100, 50, 20, 30, 40, 80, 300, 40, 30, 20, 40, 60,
							70, 3000, 20, 10, 40, 20, 30, 200, 10, 10, 50 },
					function, configuration, sourcedir, targetdir, sourcefile);

		}

	}

	public void generateZoneData(String infile, String outfile, int[] amount, String function, String configuration,
			String sourcedir, String targetdir, String sourcefile) {

		List<GenerationCallback> callbacks = new ArrayList<>();
		for (int val : amount) {
			callbacks.add(new RandomDistributionCallback(val, (int) ((val * .10) + new Random().nextInt(val))));
		}

		CSVData csvZone = CSVUtil.getInstance().readFromFile(sourcedir + DataUtil.PATH_SEPARATOR + infile, ';',
				CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		CSVData csvOut = new CSVData();
		csvOut.setHeader(new String[] { "day", "hour", "zone", "amount" });

		for (int dag = 0; dag < 7; ++dag) {
			for (int uur = 0; uur < 24; ++uur) {
				int zone = 0;
				for (String[] line : csvZone.getLines()) {
					csvOut.add(new String[] { "" + dag, "" + uur, line[0],
							String.valueOf(Double
									.parseDouble(callbacks.get((uur + zone) % callbacks.size()).generate(-1, -1)[0])
									* Double.parseDouble(line[1].replace(",","."))) });
					++zone;
				}
			}
		}
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + outfile, csvOut, ';');
	}

	public static void main(String[] args) {
		DataGenerationTool tool = new DataGenerationTool();

		tool.run(args);
	}

}
