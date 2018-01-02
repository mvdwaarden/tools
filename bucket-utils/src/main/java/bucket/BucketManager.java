package bucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * Purpose: Manages buckets
 * 
 * @author mwa17610
 * 
 */
@SuppressWarnings({"rawtypes"})
public class BucketManager {
	private Map<String, Bucket> buckets = new HashMap<>();
	private BucketConfig config = new BucketConfig();

	/**
	 * Add a bucket to the bucket manager
	 * 
	 * @param bucket
	 */
	
	public void addBucket(Bucket bucket) {
		Bucket tmp = buckets.get(bucket.getName());

		if (null == tmp) {
			bucket.setConfig(this.config);
			this.buckets.put(bucket.getName(), bucket);
		} else {
			throw new BucketException("bucket [" + bucket.getName()
					+ "] already exist");
		}
	}

	/**
	 * Drop some trash. The manager tries to drop it info all the buckets
	 * 
	 * @param trash
	 */
	public void drop(Trash trash) {
		if (null != trash.getKey()) {
			for (Entry<String, Bucket> e : this.buckets.entrySet()) {
				e.getValue().drop(trash);
			}
		}
	}

	/**
	 * Dispose of a bucket. The bucket is removed from the manager
	 * 
	 * @param name
	 */
	public Bucket dispose(String name) {
		Bucket bucket = this.buckets.get(name);

		if (null != bucket) {
			bucket.dispose();
			this.buckets.remove(name);
		}

		return bucket;
	}

	/**
	 * Return the configuration.
	 * 
	 * @return
	 */
	public BucketConfig getConfig() {
		return this.config;
	}

	/**
	 * Collect all the trash based on a key
	 */

	public <K> Bucket getTrash(K key) {
		return getTrash(key, (List<String>) null);
	}

	/**
	 * Collect all the trash based on key and a bucket list
	 * 
	 * @param key
	 * @return
	 */
	public <K> Bucket getTrash(K key, List<String> bucketList) {
		Bucket result = new Bucket(key.toString());

		if (null != bucketList) {
			for (String name : bucketList) {
				Bucket bucket = this.buckets.get(name);
				if (null != bucket)
					bucket.getTrash(key, result);
			}
		} else {
			for (Entry<String, Bucket> e : this.buckets.entrySet()) {
				e.getValue().getTrash(key, result);

			}
		}

		return result;
	}

	/**
	 * Collect all the trash based on key and a bucket list
	 * 
	 * @param key
	 * @return
	 */
	public <K> Bucket getTrash(K key, String[] bucketList) {
		Bucket result = null;
		if (null != bucketList) {
			List<String> tmp = new ArrayList<>();

			for (String str : bucketList) {
				tmp.add(str);
			}
			result = getTrash(key, tmp);
		} else
			result = getTrash(key);

		return result;
	}
	
	public Stream<Bucket> stream(){
		return buckets.values().stream();
	}

}
