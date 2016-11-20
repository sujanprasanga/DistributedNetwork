package lk.ac.mrt.distributed.messaging.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import lk.ac.mrt.distributed.messaging.Node;

public class FileTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7117049764371837877L;
	
	private final List<String> nodes = new ArrayList<>();
	private final List<String> fileList = new ArrayList<>();
	
	
	public void setData(Map<Node, Set<String>> fileIndex){
		nodes.clear();
		fileList.clear();
		for(Entry<Node, Set<String>> e : fileIndex.entrySet()){
			Node n = e.getKey();
			for(String s: e.getValue()){
				nodes.add(n.toString());
				fileList.add(s);
			}
		}
	}
	
	@Override
	public int getRowCount() {
		return nodes.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0 : return nodes.get(rowIndex);
		case 1 : return fileList.get(rowIndex);
		}
		return null;
	}

}
