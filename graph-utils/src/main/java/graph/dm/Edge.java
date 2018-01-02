package graph.dm;

/**
 * Purpose: Connects to nodes.
 * 
 * @author mwa17610
 * 
 */
public class Edge<N extends Node> implements NameDescription {
	private N source;
	private N target;
	private String name;
	private String description;
	private double weight;

	public Edge() {
		super();
	}

	public Edge(N source, N target) {
		this.source = source;
		this.target = target;
		this.name = EdgeType.HAS;
	}

	public Edge(N source, N target, String name) {
		this(source, target);
		this.name = name;
	}

	public Edge(N source, N target, String name, String description) {
		this(source, target, name);
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public N getSource() {
		return source;
	}

	public void setSource(N source) {
		this.source = source;
	}

	public N getTarget() {
		return target;
	}

	public void setTarget(N target) {
		this.target = target;
	}
}
