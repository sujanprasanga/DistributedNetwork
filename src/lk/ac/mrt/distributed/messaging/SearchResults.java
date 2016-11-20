package lk.ac.mrt.distributed.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchResults {
	private final String query;
	private final String messageId;
	private final Map<Node, List<String>> results = new HashMap<>();
	
	public SearchResults(String query, String messageId) {
		super();
		this.query = query;
		this.messageId = messageId;
	}

	public String getQuery() {
		return query;
	}

	public String getMessageId() {
		return messageId;
	}
	
	public synchronized void addSearchResult(Node node, Set<String> files){
		List<String> f = results.get(node);
		if(f == null){
			f = new ArrayList<>();
			results.put(node, f);
		}
		f.addAll(files);
	}
	
	public Map<Node, List<String>> getResults(){
		return results;
	}
}
