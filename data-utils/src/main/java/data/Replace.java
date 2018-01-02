package data;

public interface Replace<T> {
	String getTarget(T obj);

	String getReplacement(T obj);
	
	boolean isUseToken();
		
}
