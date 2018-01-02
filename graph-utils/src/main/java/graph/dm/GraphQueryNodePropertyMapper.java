package graph.dm;

public interface GraphQueryNodePropertyMapper<N extends Node> {
	String map(N node, String property);
}
