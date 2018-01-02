package bucket.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import bucket.Bucket;
import bucket.BucketHandler;
import bucket.BucketManager;
import bucket.BucketTypeHandler;
import bucket.Trash;
import data.LogUtil;

public class BucketUtilTest {

	/**
	 * Uses bucket classes. The bucket contains Trash<Integer,Integer> items.
	 * 
	 * Test case creates two buckets one for 7's (sum of key and content) and
	 * one for non 7' (sum of key and content)
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testBuckets() {
		BucketManager mgr = new BucketManager();
		Bucket bucket7 = new Bucket("7");

		bucket7.registerHandler(new BucketTypeHandler<Integer, Integer>(Integer.class, Integer.class) {
			@Override
			public void dropped(Bucket bucket, Trash<Integer, Integer> trash) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean accepts(Bucket bucket, Trash<Integer, Integer> trash) {
				return ((trash.getKey() + trash.getContent()) == 7);
			}

			@Override
			public void dispose(Bucket bucket, Trash<Integer, Integer> trash) {
				// TODO Auto-generated method stub

			}
		});
		mgr.addBucket(bucket7);
		Bucket bucketN7 = new Bucket("N7");
		bucketN7.registerHandler(new BucketTypeHandler<Integer, Integer>(Integer.class, Integer.class) {

			@Override
			public void dropped(Bucket bucket, Trash<Integer, Integer> trash) {
			}

			@Override
			public boolean accepts(Bucket bucket, Trash<Integer, Integer> trash) {
				return ((trash.getKey() + trash.getContent()) != 7);
			}

			@Override
			public void dispose(Bucket bucket, Trash<Integer, Integer> trash) {
			}
		});
		mgr.addBucket(bucketN7);
		Bucket bucketObject = new Bucket("bucket");
		mgr.addBucket(bucketObject);
		bucketObject.registerHandler(new BucketTypeHandler<Integer, Bucket>(Integer.class, Bucket.class) {

			@Override
			public void dropped(Bucket bucket, Trash<Integer, Bucket> trash) {

			}

			@Override
			public boolean accepts(Bucket bucket, Trash<Integer, Bucket> trash) {
				LogUtil.getInstance().info(trash.getContent().getClass().getName());
				return true;
			}

			@Override
			public void dispose(Bucket bucket, Trash<Integer, Bucket> trash) {

			}
		});

		bucketObject.registerHandler(new BucketHandler() {

			@Override
			public void dropped(Bucket bucket, Trash trash) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean accepts(Bucket bucket, Trash trash) {
				LogUtil.getInstance().info(trash.getContent().getClass().getName());
				if (trash.getContent().toString().trim().equals("5") && trash.getKey().toString().trim().equals("5"))
					return true;
				else
					return false;
			}

			@Override
			public void dispose(Bucket bucket, Trash trash) {
				// TODO Auto-generated method stub

			}

		});

		mgr.drop(new Trash<>(1, 2));
		mgr.drop(new Trash<>(2, 5));
		mgr.drop(new Trash<>(3, 4));
		mgr.drop(new Trash<>(5, 5));
		mgr.drop(new Trash<>(3, 2));
		mgr.drop(new Trash<>(3, bucket7));

		Assert.assertTrue("fail count check bucket7.trash.length = '" + bucket7.size() + "' != 2 '",
				bucket7.size() == 2);

		Assert.assertTrue("fail count check bucketN7.trash.length = '" + bucketN7.size() + "'" + " != 3 '",
				bucketN7.size() == 3);

		// check if 7 bucket only contains '7' trash
		bucket7.<Integer, Integer> stream().forEach(
				trash -> {
					Assert.assertTrue("fail content check bucket7[" + trash.getKey() + "] '"
							+ (int) (trash.getKey() + trash.getContent()) + "' != 7",
							(int) (trash.getKey() + trash.getContent()) == 7);
				});
		// check if non 7 bucket only contains 'non 7' trash
		bucketN7.<Integer, Integer> stream().forEach(
				trash -> {
					Assert.assertFalse("fail content check bucketN7[" + trash.getKey() + "] '"
							+ (int) (trash.getKey() + trash.getContent()) + "' == 7",
							(int) (trash.getKey() + trash.getContent()) == 7);
				});

		// get trash based on the key
		Bucket bucket3 = mgr.getTrash(3);

		Assert.assertTrue("fail count check bucket3.trash.length = '" + bucket3.size() + "'" + " != 2 '",
				bucket3.size() == 3);

		bucket3.<Integer, Object> stream().forEach(
				trash -> {
					Assert.assertTrue("fail key check bucket3[" + trash.getKey() + "] '" + trash.getKey() + "' == 3",
							trash.getKey() == 3);
				});

		bucket3 = mgr.getTrash(3, new String[] { "N7" });
		Assert.assertTrue("fail count check bucket3.trash.length = '" + bucket3.size() + "'" + " != 1 '",
				bucket3.size() == 1);

		bucket3.<Integer, Object> stream().forEach(
				trash -> {
					Assert.assertTrue("fail key check bucket3[" + trash.getKey() + "] '" + trash.getKey() + "' == 3",
							trash.getKey() == 3);
				});

		List<Trash<?, Integer>> trash = bucketObject.<Integer> getTrash(Integer.class);

		Assert.assertTrue("bucket handler without generics not called! fail count check bucket3.trash.length = '"
				+ trash.size() + "'" + " != 1 '", trash.size() == 1);

	}
}
