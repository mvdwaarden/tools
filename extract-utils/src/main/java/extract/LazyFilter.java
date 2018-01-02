package extract;

public interface LazyFilter<E> {
	boolean filter(E element);
}
