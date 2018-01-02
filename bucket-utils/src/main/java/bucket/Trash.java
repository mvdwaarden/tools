package bucket;

/**
 * Trash, typically something to be put into a bucket
 * 
 * @author mwa17610
 * 
 */
public class Trash<K, C> {
	public static final String DEFAULT_TYPE = "not specified";
	public static final String SOURCE = "TRASH_SOURCE";
	private K key;
	private C content;
	private String type;

	public Trash(K key, C content) {
		this(key, content, DEFAULT_TYPE);
	}

	public Trash(K key, C content, String type) {
		this.key = key;
		this.content = content;
		this.type = type;
	}

	public K getKey() {
		return key;
	}

	public C getContent() {
		return content;
	}

	public String getType() {
		return type;
	}
}
