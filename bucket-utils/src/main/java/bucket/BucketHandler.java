package bucket;

/**
 * Interface for the handler.
 * 
 * @author mwa17610
 * 
 * @param <K>
 *            key type
 * @param <C>
 *            content type
 */
public interface BucketHandler<K,C> {
	/**
	 * Called when trash is dropped into a bucket.
	 * 
	 * @param bucket
	 * @param trash
	 */
	void dropped(Bucket bucket, Trash<K, C> trash);

	/**
	 * Called to verify if a bucket accepts the trash.
	 * 
	 * @param bucket
	 * @param trash
	 * @return
	 */
	boolean accepts(Bucket bucket, Trash<K, C> trash);

	/**
	 * Called when trash is disposed of.
	 * 
	 * @param bucket
	 * @param trash
	 */
	void dispose(Bucket bucket, Trash<K, C> trash);
}
