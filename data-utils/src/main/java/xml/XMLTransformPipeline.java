package xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline definitie voor XSLT translaties.
 */
public class XMLTransformPipeline {
	List<PipelineItem> items = new ArrayList<>();

	public XMLTransformPipeline(PipelineItem[] items) {
		for (PipelineItem item : items)
			this.items.add(item);
	}

	public XMLTransformPipeline(List<PipelineItem> items) {
		for (PipelineItem item : items)
			this.items.add(item);
	}

	public List<PipelineItem> getItems() {
		return items;

	}
}
