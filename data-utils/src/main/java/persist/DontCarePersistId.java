package persist;

public class DontCarePersistId implements PersistId<Integer> {
	@Override
	public Integer getId() {
		return 0;
	}
}
