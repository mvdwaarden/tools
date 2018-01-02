package bucket;

/**
 * Bucket Exception
 * @author mwa17610
 *
 */
public class BucketException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BucketException(String msg, Exception e) {
		super(msg, e);
	}

	public BucketException(String msg) {
		super(msg);
	}
}
