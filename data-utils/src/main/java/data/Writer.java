package data;

@FunctionalInterface
public interface Writer<T> {
	StringBuilder write(T obj);
}
