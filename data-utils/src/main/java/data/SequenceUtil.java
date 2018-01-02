package data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sequence numbers
 * 
 * @author mwa17610
 *
 */
public class SequenceUtil implements Util {
	private Map<String, SequenceDefinition> definitions;
	private static final ThreadLocal<SequenceUtil> instance = new ThreadLocal<SequenceUtil>();
	private static final String SEQUENCE_FORMAT_CONFIG = "sequence.format";
	private static final String SEQUENCE_START_CONFIG = "sequence.start";
	private static final String SEQUENCE_END_CONFIG = "sequence.end";
	private static final String SEQUENCE_INCREMENT_CONFIG = "sequence.increment";
	private static final String SEQUENCE_TYPE_CONFIG = "sequence.type";
	private static final String SEQUENCE_LOCATION_CONFIG = "sequence.location";
	private static final String SEQUENCE_CUSTOM_TYPE_CONFIG = "sequence.custom.type";

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static SequenceUtil getInstance() {
		SequenceUtil result = instance.get();

		if (null == result) {
			result = new SequenceUtil();
			instance.set(result);
		}

		return result;
	}

	public boolean isDefined(String name) {
		SequenceDefinition def = getDefinition(name);

		return def.isValid();
	}

	/**
	 * Get the next sequence based on the a sequence name
	 */
	public String getNext(String name) {
		return getNext(getDefinition(name));
	}

	/**
	 * Get the next sequence based on the a sequence definition
	 */
	public String getNext(SequenceDefinition definition) {
		long next = getSequenceStore(definition).getNext();

		String result = format(definition.getFormat(), next);

		return result;
	}

	/**
	 * Delete a sequence based on the a sequence definition
	 */
	public void delete(SequenceDefinition definition) {
		getSequenceStore(definition).delete();
	}

	/**
	 * Delete a sequence based on the a sequence name
	 */
	public void delete(String name) {
		delete(getDefinition(name));
	}

	/**
	 * Make a store for the sequence
	 */
	public void makeStore(SequenceDefinition definition) {
		getSequenceStore(definition).make();
	}

	/**
	 * Make a store for the sequence
	 */
	public void makeStore(String name) {
		makeStore(getDefinition(name));
	}

	/**
	 * Factory method for sequence generator creation
	 * 
	 * @param definition
	 * @return
	 */
	private SequenceStore getSequenceStore(SequenceDefinition definition) {
		SequenceStore result = null;
		switch (definition.getType()) {
		case FILE:
			result = new FileSequenceStore();
			result.setDefinition(definition);
			break;
		default:
			LogUtil.getInstance().error("invalid type for sequence definition [" + definition.getName() + "]");
			break;
		}
		return result;
	}

	/**
	 * Get a sequence definition from the configuration
	 * 
	 * @param name
	 * @return
	 */
	public SequenceDefinition getDefinition(String name) {
		if (null == definitions)
			definitions = new HashMap<>();
		SequenceDefinition result = definitions.get(name);

		if (null == result) {
			result = new SequenceDefinition();
			result.setName(name);
			result.setFormat(ConfigurationUtil.getInstance().getSetting(SEQUENCE_FORMAT_CONFIG + "." + name));
			result.setLocation(ConfigurationUtil.getInstance().getSetting(SEQUENCE_LOCATION_CONFIG + "." + name));
			result.setStart(ConfigurationUtil.getInstance().getIntegerSetting(SEQUENCE_START_CONFIG + "." + name, 0));
			result.setEnd(ConfigurationUtil.getInstance().getIntegerSetting(SEQUENCE_END_CONFIG + "." + name,
					Integer.MAX_VALUE));
			result.setIncrement(
					ConfigurationUtil.getInstance().getIntegerSetting(SEQUENCE_INCREMENT_CONFIG + "." + name, 1));
			String tmpValue = ConfigurationUtil.getInstance().getSetting(SEQUENCE_TYPE_CONFIG + "." + name);
			try {
				result.setType((Type) EnumUtil.getInstance().getByName(Type.class, tmpValue));
			} catch (IllegalArgumentException e) {
				LogUtil.getInstance().error("invalid enumeration for sequence definition [" + tmpValue + "]", e);
			}
			result.setCustomType(ConfigurationUtil.getInstance().getSetting(SEQUENCE_CUSTOM_TYPE_CONFIG + "." + name));
			definitions.put(name, result);
		}

		return result;
	}

	/**
	 * Format the sequence
	 * 
	 * @param format
	 * @param current
	 * @return
	 */
	protected String format(String format, long current) {
		return format.replace("${sequence}", "" + current);
	}

	private enum Type {
		FILE, CUSTOM;
	}

	public class SequenceDefinition {
		private String name;
		private Type type;
		private String customType;
		private String location;
		private long start;
		private long end;
		private long increment;
		private String format;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public String getCustomType() {
			return customType;
		}

		public void setCustomType(String customType) {
			this.customType = customType;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public long getIncrement() {
			return increment;
		}

		public void setIncrement(long increment) {
			this.increment = increment;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public boolean isValid() {
			return null != this.type;
		}
	}

	public interface SequenceStore {
		void setDefinition(SequenceDefinition definition);

		long getNext();

		void delete();

		void make();

	}

	public class FileSequenceStore implements SequenceStore {
		private SequenceDefinition definition;

		@Override
		public void setDefinition(SequenceDefinition definition) {
			this.definition = definition;
		}

		@Override
		public long getNext() {
			long current = read();

			if (current < definition.getStart())
				current = definition.getStart();
			else
				current += definition.getIncrement();

			if (current > definition.getEnd())
				current = definition.getStart();

			write(current);

			return current;
		}

		@Override
		public void delete() {
			File file = new File(getFilename());
			if (file.exists())
				file.delete();
		}

		@Override
		public void make() {
			DataUtil.getInstance().makeDirectories(getFilename());
		}

		private long read() {
			long result = 0;

			String raw = DataUtil.getInstance().readFromFile(getFilename());

			if (null != raw && !raw.isEmpty())
				result = Long.parseLong(raw);
			else
				result = -1;

			return result;
		}

		private void write(long current) {
			DataUtil.getInstance().writeToFile(getFilename(), "" + current);
		}

		private String getFilename() {
			return definition.getLocation() + DataUtil.PATH_SEPARATOR + definition.getName() + ".seq";
		}
	}
}
