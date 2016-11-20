package lk.ac.mrt.distributed.messaging.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import lk.ac.mrt.distributed.messaging.Node;

public class PeerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4536403236357753594L;
	private final List<Node> peers = new ArrayList<>();
	int i=0;
	public void setNodes(Set<Node> nodes)
	{
		peers.clear();
		peers.addAll(nodes);
		fireTableDataChanged();
	}
	
	@Override
	public int getRowCount() {
		return peers.size();
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Node node = peers.get(rowIndex);
		switch (columnIndex) {
		case 0: return node.getIp();
		case 1: return node.getPort();
		case 2: return node.getUsername();
		case 3: return node.getResources();
		case 4: return node.isOffline() ? "offline" : "online";
		case 5: return node.getType() == Node.TYPE_PEER ? "peer" : "super peer";
		case 6: return node.getType() == Node.TYPE_SUPER_PEER ? ""+node.getVacantSlots() : "-";
		default: break;
		}
		return "";
	}

}
