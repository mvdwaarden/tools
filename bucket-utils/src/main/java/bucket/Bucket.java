package bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import data.LogUtil;
import object.ObjectUtil;

/**
 * <pre>
 * Purpose: Bucket concept. 
 * - A bucket accepts 'trash'. Each bucket has handler. Each handler is checked before adding 
 *   trash. If one of the handlers accepts the trash, the trash is dropped into the bucket. 
 *   All handlers are notified when trash is dropped.
 * - A bucket may be disposed of. All handlers are notified when
 *   trash is disposed.
 * What can it be used for?
 * - Creating groups for specific data: The producer can just add trash
 *    via the bucketManager without being concerned about the bucket configuration. The 
 *    actual bucket configuration is typically not determined by the producer. This way 
 *    the production and consumption of data is separated. A bucket would typically 
 *    correspond with a group.
 * - Adding data that pertains to a certain object at different point in times (and code). When 
 * 	 appropriate all the data can be retrieved based upon the objects key. A bucket would
 *   typically correspond with a data type.
 * </pre>
 * 
 * @author mwa17610
 * 
 */
public class Bucket {
	private String name;
	private BucketConfig config;
	private List<BucketHandler<?, ?>> handlers = new ArrayList<>();
	private List<Trash<?, ?>> contents = new ArrayList<>();

	public Bucket(String name) {
		this.name = name;
	}

	/**
	 * Get the bucket name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the bucket name
	 * 
	 * @param name
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the bucket configuration
	 * 
	 * @return
	 */
	public BucketConfig getConfig() {
		return config;
	}

	public void setConfig(BucketConfig config) {
		this.config = config;
	}

	/**
	 * Register a bucket handler
	 * 
	 * @param handler
	 */
	public void registerHandler(BucketHandler<?, ?> handler) {
		if (!handlers.contains(handler))
			handlers.add(handler);
	}

	/**
	 * Unregister a bucket handler
	 * 
	 * @param handler
	 */
	public void unregisterHandler(BucketHandler<?, ?> handler) {
		handlers.remove(handler);
	}

	/**
	 * Drop trash in the bucket
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void drop(Trash<?, ?> trash) {
		boolean add = false;

		if (handlers.isEmpty()) {
			add = true;
		} else {
			Optional<BucketHandler<?, ?>> optHandler = handlers.stream().filter(h -> {
				boolean result = false;
				try {
					result = h.accepts(this, (Trash) trash);
				} catch (ClassCastException e) {
					LogUtil.getInstance().ignore("ignored drop cast exception", e);
				}
				return result;
			}).findFirst();
			add = optHandler.isPresent();
		}
		if (add) {
			this.contents.add(trash);
			// notify all handlers that the item was dropped
			for (BucketHandler<?, ?> drophandler : handlers) {
				drophandler.dropped(this, (Trash) trash);
			}
		} else {
			add = false;
		}
	}

	/**
	 * dispose the specific trash
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void dispose(Trash<?, ?> trash) {
		if (this.contents.contains(trash)) {
			this.contents.remove(trash);
			for (BucketHandler<?, ?> handler : handlers) {
				handler.dispose(this, (Trash) trash);
			}
		}
	}

	/**
	 * dispose the bucket contents
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void dispose() {
		for (BucketHandler<?, ?> handler : handlers) {
			for (Trash<?, ?> trash : this.contents)
				handler.dispose(this, (Trash) trash);
		}
		while (!this.contents.isEmpty())
			this.contents.remove(0);
	}

	/**
	 * Get all the trash
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public <K, C> Stream<Trash<K, C>> stream() {
		Stream<Trash<K, C>> result = (Stream<Trash<K, C>>) this.contents.stream().map(o -> (Trash<K, C>) o);

		return result;
	}

	/**
	 * Get the trash identified by the key. The trash is added to the 'input
	 * bucket'.
	 * 
	 * @param key
	 * @param bucket
	 */
	public <K> void getTrash(K key, Bucket bucket) {
		this.contents.stream().forEach(t -> {
			if (t.getKey().equals(key))
				bucket.drop(t);
		});
	}

	/**
	 * Get the trash of a specific class
	 * 
	 * @param clsTrash
	 * @return
	 */
	public <C> List<Trash<?, C>> getTrash(Class<?> clsTrash) {
		return getTrash(clsTrash, (String) null);
	}

	/**
	 * Get the trash of a specific class and type
	 * 
	 * @param clsTrash
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <C> List<Trash<?, C>> getTrash(Class<?> clsTrash, String type) {
		List<Trash<?, C>> result = this.contents.stream()
				.filter(t -> null != contents
						&& ObjectUtil.getInstance().isA(t.getContent(), new Class[] { clsTrash }, true)
						&& (null == type || t.getType().equals(type)))
				.map(t -> (Trash<?, C>) t).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get the trash content list of a specific class and type
	 * 
	 * @param clsTrash
	 * @return
	 */
	public <C> List<C> getTrashContent(Class<?> clsTrash) {
		return getTrashContent(clsTrash, null);
	}

	/**
	 * Get the trash content list of a specific class and type
	 * 
	 * @param clsTrash
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public <C> List<C> getTrashContent(Class<?> clsTrash, String type) {
		List<C> result = this.contents.stream()
				.filter(t -> null != contents
						&& ObjectUtil.getInstance().isA(t.getContent(), new Class[] { clsTrash }, true)
						&& (null == type || t.getType().equals(type)))
				.map(t -> (C) t.getContent()).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get the trash content list of a specific class and type
	 * 
	 * @param clsTrash
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <C> List<C> getTrashContent(String type) {
		List<C> result = this.contents.stream().filter(t -> null == type || t.getType().equals(type))
				.map(t -> (C) t.getContent()).collect(Collectors.toList());

		return result;
	}

	/**
	 * Checks if a bucket is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return contents.isEmpty();
	}

	/**
	 * Returns the size of a bucket
	 * 
	 * @return
	 */
	public int size() {
		return this.contents.size();
	}
}
