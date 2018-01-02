package extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazySource<E> implements Source<E> {
	private String name;
	private Source<?> source;

	public LazySource(String name, Source<?> source) {
		this.name = name;
		this.source = source;
	}

	public LazySource(Source<?> source) {
		this.source = source;
	}

	protected void setSource(Source<?> source) {
		this.source = source;
	}

	public LazySource<E> filter(String name, LazyFilter<E> filter) {
		LazySource<E> result = new LazyFilterSource<E>(name, this, filter);
		return result;
	}

	public <T> LazySource<T> map(String name, LazyMapper<E, T> mapper) {
		LazySource<T> result = new LazyMapSource<E, T>(name, this, mapper);

		return result;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public E nextElement() {
		return (E) source.<E> nextElement();
	}

	public List<E> toList() {
		List<E> result = new ArrayList<>();
		E element;
		while (null != (element = nextElement())) {
			result.add(element);
		}
		return result;
	}

	public <T> Map<E, T> toMap(LazyMapper<E, T> mapper) {
		Map<E, T> result = new HashMap<>();
		E element;
		while (null != (element = nextElement())) {
			result.put(element, mapper.map(element));
		}
		return result;
	}

	public E findFirst() {
		E result = nextElement();

		return result;
	}

	public void go() {

		while (null != nextElement())
			;

	}

	public void printLazyChain() {
		Source<?> lazy = this;

		while (lazy instanceof LazySource) {
			System.out.print(((null != ((LazySource<?>) lazy).name) ? ((LazySource<?>) lazy).name : "?") + "->");
			lazy = ((LazySource<?>) lazy).source;
		}
		System.out.println(" = the source");
	}

	/**
	 * Chain wrapper for LazyFilter
	 */
	private class LazyFilterSource<F> extends LazySource<F> {
		private LazyFilter<F> filter;

		public LazyFilterSource(String name, LazySource<F> source, LazyFilter<F> filter) {
			super(name, source);
			this.filter = filter;
		}

		@Override
		public F nextElement() {
			F element;
			F result = null;
			while (null != (element = super.nextElement())) {
				if (filter.filter(element)) {
					result = element;
					break;
				}
			}
			return result;
		}
	}
	/**
	 * Chain wrapper for LazyFilter
	 */
	private class LazyMapSource<F, T> extends LazySource<T> {
		private LazyMapper<F, T> mapper;

		public LazyMapSource(String name, LazySource<F> source, LazyMapper<F, T> mapper) {
			super(name, source);
			this.mapper = mapper;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T nextElement() {
			F element;
			T result = null;
			while (null != (element = (F) super.nextElement())) {
				if (null != element) {
					result = mapper.map(element);
					break;
				}
			}
			return result;
		}
	};
}
