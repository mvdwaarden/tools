package nl.ordina.tools.ruleparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Collection of variables
 * 
 * @author mwa17610
 * 
 */
public class Variables {
	private Map<String, Variable> mapVariables = new HashMap<>();

	public void addVariable(Variable variable) {
		mapVariables.put(variable.getName(), variable);
	}

	public Variable getVariable(String name) {
		return mapVariables.get(name);
	}

	public Variable[] getVariables() {
		Variable[] result = new Variable[mapVariables.size()];

		int i = 0;
		for (Entry<String, Variable> var : mapVariables.entrySet()) {
			result[i++] = var.getValue();
		}
		return result;
	}
	
	public int size(){
		return mapVariables.size();
	}
}
