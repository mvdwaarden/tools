package data;

@FunctionalInterface
public interface Filter<T> {
	boolean include(T obj);
}
