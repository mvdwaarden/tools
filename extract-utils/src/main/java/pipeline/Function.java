package pipeline;

public abstract class Function {
	private Function chain;

	protected Function wrap(Function function) {
		Function result = new Function() {
			@Override
			protected void execute() {
				Function.this.execute();
			}
		};
		return result;
	}

	protected abstract void execute();

	protected void doExecute() {
		chain = wrap(this);
		if (null != chain)
			chain.execute();

		execute();
	}
}
