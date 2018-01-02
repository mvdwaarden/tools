package nl.ordina.tools.ruleparser;

import data.DataUtil;


/**
 * Purpose: - Variable substitution (name - public name) - Variable value
 * 
 * @author mwa17610
 * 
 */
public class Variable {
	private VariableType type;
	private String name;
	private String containername;
	private String publicContainername;
	private String localname;
	private String publicname;
	private String token;
	private String uuid;
	private Object value;
	private boolean fModule;
	private boolean fUsed;

	public Variable(VariableType type, String name, String localname) {
		this.type = type;
		this.name = name;
		this.localname = localname;
		this.uuid = "@#" + DataUtil.getInstance().getUuid() + "#@";
	}

	public VariableType getType() {
		return type;
	}

	public void setType(VariableType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalname() {
		return localname;
	}

	public void setLocalname(String localname) {
		this.localname = localname;
	}

	public String getPublicname() {
		return publicname;
	}

	public void setPublicname(String publicname) {
		this.publicname = publicname;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public enum VariableType {
		PARAMETER_TYPE, RETURN_TYPE, LOCAL_TYPE, PUBLIC_TYPE;

		public static boolean contains(VariableType[] types, VariableType localType) {
			for (VariableType typ : types)
				if (localType.equals(typ))
					return true;

			return false;
		}
	}

	public void setContainername(String containername) {
		this.containername = containername;
	}

	public String getContainername() {
		return containername;
	}

	public String getPublicContainername() {
		return publicContainername;
	}

	public void setPublicContainername(String publiccontainername) {
		this.publicContainername = publiccontainername;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setModule(boolean fModule) {
		this.fModule = fModule;
	}

	public boolean isModule() {
		return fModule;
	}

	public void setUsed(boolean fUsed) {
		this.fUsed = fUsed;
	}

	public boolean isUsed() {
		return this.fUsed;
	}

}
