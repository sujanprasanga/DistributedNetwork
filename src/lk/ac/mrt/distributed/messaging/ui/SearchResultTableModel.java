package lk.ac.mrt.distributed.messaging.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import lk.ac.mrt.distributed.messaging.Node;
import lk.ac.mrt.distributed.messaging.SearchResults;

public class SearchResultTableModel extends AbstractTableModel {

	private final Set<SearchResults> results = new HashSet<>();
	
	private final List<String> query = new ArrayList<>();
	private final List<String> fileOwner = new ArrayList<>();
	private final List<String> files = new ArrayList<>();
	
	public void setData(Collection<SearchResults> collection){
		query.clear();
		fileOwner.clear();
		files.clear();
		for(SearchResults s : collection){
			for(Entry<Node, List<String>> e : s.getResults().entrySet()){
				for(String file : e.getValue()){
					query.add(s.getQuery());
					fileOwner.add(e.getKey().toString());
					files.add(file);
				}
			}
		}
	}
	
	@Override
	public int getRowCount() {
		return query.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: return query.get(rowIndex);
		case 1: return fileOwner.get(rowIndex);
		case 2: return files.get(rowIndex);
		default: break;
		}
		return "";
	}

}
