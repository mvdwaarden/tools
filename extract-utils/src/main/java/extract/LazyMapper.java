package extract;

public interface LazyMapper<S,T> {
	T map(S element);
}
