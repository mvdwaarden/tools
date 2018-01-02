package nl.ordina.tools.ruleparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import data.Filter;

/**
 * Convenience class for iterating through a ANTLR parse tree
 * 
 * @author mwa17610
 * @see RuleContextQuery
 */
public class RuleContextQueryList {
	private List<RuleContextQuery> queries;

	public RuleContextQueryList() {

	}

	public RuleContextQueryList(List<RuleContextQuery> queries) {
		this.queries = queries;
	}

	public List<RuleContextQuery> getQueries() {
		if (null == queries)
			queries = new ArrayList<>();

		return queries;
	}

	public boolean exists(Filter<RuleContextQuery> filter) {
		return !findFirst(filter).isEmpty();
	}

	public RuleContextQuery findFirst(Filter<RuleContextQuery> filter) {
		RuleContextQuery result = null;

		if (null != queries) {
			Optional<RuleContextQuery> optResult = getQueries().stream().filter(ctx -> filter.include(ctx)).findFirst();

			if (optResult.isPresent())
				result = optResult.get();
		}
		if (null == result)
			result = new RuleContextQuery(null);

		return result;
	}

	public RuleContextQueryList filter(Filter<RuleContextQuery> filter) {
		RuleContextQueryList result;

		if (null != queries)
			result = new RuleContextQueryList(getQueries().stream().filter(ctx -> filter.include(ctx))
					.collect(Collectors.toList()));
		else
			result = new RuleContextQueryList();

		return result;
	}

	public boolean isEmpty() {
		return null == queries || queries.isEmpty();
	}

	public void forEach(Consumer<RuleContextQuery> consumer) {
		if (null != queries)
			getQueries().stream().forEach(consumer);
	}

	public int size() {
		return (null != queries) ? queries.size() : 0;
	}
	
	public RuleContextQuery get(int idx){
		return (null == queries) ? new RuleContextQuery(null) : queries.get(idx);
	}
}
