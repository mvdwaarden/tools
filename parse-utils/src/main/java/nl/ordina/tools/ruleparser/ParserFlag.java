package nl.ordina.tools.ruleparser;

public class ParserFlag {
	protected String name;
	protected boolean value;

	public ParserFlag(String name, boolean value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
}
