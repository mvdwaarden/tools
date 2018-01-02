package metadata;

public class MetaInterval {
	private Long begin;
	private Long end;
	private boolean includeBegin;
	private boolean includeEnd;

	public Long getBegin() {
		return begin;
	}

	public void setBegin(Long begin) {
		this.begin = begin;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public boolean isIncludeBegin() {
		return includeBegin;
	}

	public void setIncludeBegin(boolean includeBegin) {
		this.includeBegin = includeBegin;
	}

	public boolean isIncludeEnd() {
		return includeEnd;
	}

	public void setIncludeEnd(boolean includeEnd) {
		this.includeEnd = includeEnd;
	}

}
