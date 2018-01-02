package data;

@FunctionalInterface
public interface Test<T> {
	boolean ok(T obj);
}
