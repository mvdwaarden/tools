package json;

import java.util.Stack;

import data.StringUtil;

public class Path {
	public enum Option {
		MAKE_PATH
	}

	private Stack<Part<?>> parts;

	public static Path newPath(String path) {
		Path result = new Path();
		String[] parts = path.split("\\.|\\[");

		for (String part : parts) {
			if (part.endsWith("]")) {
				result.pushIndex(Integer.parseInt(part.substring(0, part.length() - 1)));
			} else {
				result.pushName(part);
			}
		}
		return result;
	}

	public String getPathString() {
		StringBuilder result = new StringBuilder();
		if (null != parts && parts.size() > 0) {
			for (int i = 0; i < parts.size(); ++i) {
				Part<?> part = parts.get(i);
				if (null != part.getValue()) {
					if (part instanceof IndexPart)
						StringUtil.getInstance().stripEnd(result, ".");
					result.append(part.getPathString());
					result.append(".");
				}
			}
			StringUtil.getInstance().stripEnd(result, ".");
		}
		return result.toString();
	}

	public void pushName(String name) {
		getParts().push(new NamePart(name));
	}

	public void pushIndex(int idx) {
		getParts().push(new IndexPart(idx));
	}

	public void pop() {
		parts.pop();
	}

	public Stack<Part<?>> getParts() {
		if (null == parts)
			parts = new Stack<>();
		return parts;
	}

	public abstract class Part<T> {
		private T value;

		public Part(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		abstract public String getPathString();
	}

	public class IndexPart extends Part<Integer> {
		public IndexPart(int idx) {
			super(idx);
		}

		public String getPathString() {
			return "[" + getValue() + "]";
		}
	}

	public class NamePart extends Part<String> {
		public NamePart(String name) {
			super(name);
		}

		public String getPathString() {
			return getValue();
		}
	}
}
