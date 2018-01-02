package csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVRuleExec {
	public static final String COL_NAME_TABLE = "TABLE";
	public static final String COL_NAME_LEVEL = "LEVEL";
	public static final String COL_NAME_RULE = "RULE";
	public static final String COL_NAME_NAME = "NAAM";
	public static final String RULE_KEY = "KEY";
	public static final String RULE_VALUE = "VALUE";
	private CSVData ruleConfig;
	private Map<String, List<String[]>> cacheRuleConfig;

	public CSVRuleExec(CSVData ruleConfig) {
		this.ruleConfig = ruleConfig;
		cacheRuleConfig = new HashMap<>();
	}

	public String[] getKeyValue(String[] headers, String[] row) {
		String[] result = new String[2];

		CSVColumnHash rowColumnHash = new CSVColumnHash(headers);

		Integer rowTableIdx = rowColumnHash.get(COL_NAME_TABLE);
		Integer configTableIdx = ruleConfig.getColumnIndex(COL_NAME_TABLE);
		Integer configRuleIdx = ruleConfig.getColumnIndex(COL_NAME_RULE);
		Integer configNameIdx = ruleConfig.getColumnIndex(COL_NAME_NAME);
		Integer configLevelIdx = ruleConfig.getColumnIndex(COL_NAME_LEVEL);
		if (null != rowTableIdx && rowTableIdx >= 0 && row.length > rowTableIdx) {
			String table = row[rowTableIdx];
			if (null != table && !table.isEmpty()) {
				List<String[]> tableConfig = cacheRuleConfig.get(table);
				if (null == tableConfig) {
					tableConfig = ruleConfig.filter(line -> table.equals(line[configTableIdx]));
					if (null != tableConfig && !tableConfig.isEmpty())
						cacheRuleConfig.put(table, tableConfig);
				}
				if (null != tableConfig && !tableConfig.isEmpty()) {
					result[0] = getKeyValue(table, tableConfig, rowColumnHash, configRuleIdx, configNameIdx,
							configLevelIdx, RULE_KEY, row);
					result[1] = getKeyValue(table, tableConfig, rowColumnHash, configRuleIdx, configNameIdx,
							configLevelIdx, RULE_VALUE, row);
				}
			}
		}

		return result;

	}

	/**
	 * <pre>
	 * Concatenate values in a row based on:
	 *  - Data - 
	 * ----------------------------------------------------------
	 * |   TABLE   |   COL1    |   COL2   |   COL3   |   COL4   |
	 * ----------------------------------------------------------
	 * |   TAB_A   |  CELL11   |  CELL12  |  CELL13  |   CELL14 |
	 * |   TAB_A   |  CELL21   |  CELL22  |  CELL23  |   CELL24 |
	 * |   TAB_B   |  CELL31   |  CELL32  |  CELL33  |   CELL34 |
	 * ----------------------------------------------------------
	 * - Configuration - 
	 *  ----------------------------------------------------------
	 * |   TABLE   |   NAME    |  LEVEL   |  AANTAL  |   RULE   |
	 *  ----------------------------------------------------------
	 * |   TAB_A   |   VELD_1  |   COL1   |    3     |    KEY   |     
	 * |   TAB_A   |   VELD_2  |   COL2   |    3     |    KEY   |
	 * |   TAB_A   |   VELD_3  |   COL3   |    3     |   VALUE  |
	 * |   TAB_B   |   VELD_1  |   COL1   |    3     |   VALUE  |     
	 * |   TAB_B   |   VELD_2  |   COL2   |    3     |    KEY   |
	 *  ----------------------------------------------------------
	 * 
	 * Voor ieder rij in de data wordt gekeken in de configuratie of:
	 * 1) Bepaald specifieke tabel configuratie op basis van de waarde in de TABLE kolom : [Data].[TABLE] = [Configuration].[TABLE] 
	 * 2) Voor een specifiek tabel configuratie filter de regels op basis van de RULE kolom : [Configuration].[RULE] = <rule parameter>
	 * 3) Concateneer de waarden uit de data rij voor alle regels uit (2), waarvoor geld [Data].[VELD = [Configuration].[LEVEL]]
	 * </pre>
	 * 
	 * @param table
	 * @param tableConfig
	 * @param rowColumnHash
	 * @param configRuleIdx
	 * @param configLevelIdx
	 * @param rulename
	 * @param row
	 * @return
	 */
	public String getKeyValue(String table, List<String[]> tableConfig, CSVColumnHash rowColumnHash, int configRuleIdx,
			int configNameIdx, int configLevelIdx, String rulename, String[] row) {
		List<String[]> ruleConfig = tableConfig.stream().filter(line -> line[configRuleIdx].contains(rulename))
				.collect(Collectors.toList());
		String result = "";
		String name = "";

		if (ruleConfig.size() > 0) {
			for (String[] ruleLine : ruleConfig) {
				String col = ruleLine[configLevelIdx];
				Integer idx = rowColumnHash.get(col);
				try {
					result += row[idx];
					if (rulename.contains(RULE_KEY)) {
						name += ruleLine[configNameIdx];
					}

				} catch (Exception ex) {
					result += "[E" + col + "]";
					break;
				}
			}
			if (rulename.equals(RULE_KEY))
				result = table + name + ":" + result;
		} else if (tableConfig.size() == 2) {
			try {
				if (rulename.contains(RULE_KEY)) {
					String col = tableConfig.get(0)[configLevelIdx];
					Integer idx = rowColumnHash.get(col);
					result = table + tableConfig.get(0)[configNameIdx] + row[idx];
				} else if (rulename.contains(RULE_VALUE)) {
					String col = tableConfig.get(1)[configLevelIdx];
					Integer idx = rowColumnHash.get(col);
					result = table + tableConfig.get(1)[configNameIdx] + row[idx];
				}
			} catch (Exception ex) {
				result = "";
			}
		}
		return result;
	}

}
