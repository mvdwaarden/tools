package extract;

public interface LineSource extends  Source<String> {
	@Override
	String nextElement();	
}
