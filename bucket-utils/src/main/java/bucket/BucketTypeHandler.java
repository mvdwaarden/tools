package bucket;

import object.ObjectUtil;

public class BucketTypeHandler<K,C> implements BucketHandler<K,C> {
	private Class<?> clsK;
	private Class<?> clsC;

	public BucketTypeHandler(Class<?> clsK, Class<?> clsC) {
		this.clsK = clsK;
		this.clsC = clsC;
	}

	@Override
	public void dropped(Bucket bucket, Trash<K, C> trash) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean accepts(Bucket bucket, bucket.Trash<K, C> trash) {
		if (null != trash && null != trash.getKey()
				&& ObjectUtil.getInstance().isA(trash.getKey(), new Class[] { clsK }, true)
				&& null != trash.getContent()
				&& ObjectUtil.getInstance().isA(trash.getContent(), new Class[] { clsC }, true)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void dispose(Bucket bucket, Trash<K, C> trash) {
		// TODO Auto-generated method stub

	}

}
