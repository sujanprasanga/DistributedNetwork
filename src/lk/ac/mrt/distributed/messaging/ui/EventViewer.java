package lk.ac.mrt.distributed.messaging.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lk.ac.mrt.distributed.messaging.Node;
import lk.ac.mrt.distributed.messaging.SearchResults;
import lk.ac.mrt.distributed.messaging.SharedFiles;

public class EventViewer extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3870943388306584963L;

	private JTextField usernameTexField;
	private JTextField portTextField;
	private JTextField resourceTextField;
	private JButton btnJoinNetwork;
	private JList<String> fileList;
	private final PeerTableModel peerTableModel = new PeerTableModel();
	private final PeerTableModel subPeerTableModel = new PeerTableModel();
	private final PeerTableModel superPeerTableModel = new PeerTableModel();
	private final FileTableModel fileTableModel = new FileTableModel();
	private final SearchResultTableModel searchResultsTableModel = new SearchResultTableModel();
	private JTable peerTable = new JTable(peerTableModel);
	private JTable subPeerTable = new JTable(subPeerTableModel);
	private JTable superPeerTable = new JTable(superPeerTableModel);
	private JTable fileListTable = new JTable(fileTableModel);
	private JTextArea console;
	private JTable searchResults =  new JTable(searchResultsTableModel);

	private JLabel roleLable;
	private JLabel superPeer;
	private JTextField txtSearch;
	private JButton btnSearch;

	private JTabbedPane tab;
	
	public EventViewer() {
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		roleLable = new JLabel("Peer type");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 3;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		getContentPane().add(roleLable, gbc_lblNewLabel);
		
		superPeer = new JLabel("");
		GridBagConstraints gbc_superPeer = new GridBagConstraints();
		gbc_superPeer.gridwidth = 4;
		gbc_superPeer.insets = new Insets(0, 0, 5, 0);
		gbc_superPeer.gridx = 4;
		gbc_superPeer.gridy = 0;
		getContentPane().add(superPeer, gbc_superPeer);
		
		JLabel lblUsernamePort = new JLabel("username");
		GridBagConstraints gbc_lblUsernamePort = new GridBagConstraints();
		gbc_lblUsernamePort.anchor = GridBagConstraints.EAST;
		gbc_lblUsernamePort.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsernamePort.gridx = 0;
		gbc_lblUsernamePort.gridy = 1;
		getContentPane().add(lblUsernamePort, gbc_lblUsernamePort);
		
		usernameTexField = new JTextField();
		GridBagConstraints gbc_un = new GridBagConstraints();
		gbc_un.insets = new Insets(0, 0, 5, 5);
		gbc_un.fill = GridBagConstraints.HORIZONTAL;
		gbc_un.gridx = 1;
		gbc_un.gridy = 1;
		getContentPane().add(usernameTexField, gbc_un);
		usernameTexField.setColumns(10);
		
		JLabel lblPort = new JLabel("port");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.anchor = GridBagConstraints.EAST;
		gbc_lblPort.gridx = 2;
		gbc_lblPort.gridy = 1;
		getContentPane().add(lblPort, gbc_lblPort);
		
		portTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 3;
		gbc_textField_1.gridy = 1;
		getContentPane().add(portTextField, gbc_textField_1);
		portTextField.setColumns(5);
		portTextField.setColumns(5);
		
		JLabel lblResources = new JLabel("resources");
		GridBagConstraints gbc_resLbl = new GridBagConstraints();
		gbc_resLbl.insets = new Insets(0, 0, 5, 5);
		gbc_resLbl.anchor = GridBagConstraints.EAST;
		gbc_resLbl.gridx = 4;
		gbc_resLbl.gridy = 1;
		getContentPane().add(lblResources, gbc_resLbl);
		
		addJoinNWBtn();
		
		JPanel tablePanel = new JPanel();
		tab = new JTabbedPane();
//		tablePanel.add(tab);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 1.0;
		gbc_panel.weightx = 1.0;
		gbc_panel.anchor = GridBagConstraints.WEST;
		gbc_panel.gridwidth = 8;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		getContentPane().add(tab, gbc_panel);
		tablePanel.setLayout(new GridBagLayout());
		if(Beans.isDesignTime())
		{
			fileList = new JList<>();
		}
		else
		{
			fileList = new JList<>(SharedFiles.getInstance().getSharedFilesArray());
		}
		
		console = new JTextArea(10, 50);
		createConsole();
		setUserNameandPort();
		
		
		tab.addTab("message console",addToScrollPane(console));
		tab.addTab("Hosted files",addToScrollPane(fileList));
		tab.addTab("Known nodes",addToScrollPane(peerTable));
		tab.addTab("subpeers",addToScrollPane(subPeerTable));
		tab.addTab("superpeers",addToScrollPane(superPeerTable));
		tab.addTab("files",addToScrollPane(fileListTable));
		tab.addTab("search results",addToScrollPane(searchResults));
		
		txtSearch = new JTextField();
		txtSearch.setText("search");
		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.insets = new Insets(0, 0, 0, 5);
		gbc_txtSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSearch.gridx = 1;
		gbc_txtSearch.gridy = 3;
		getContentPane().add(txtSearch, gbc_txtSearch);
		txtSearch.setColumns(10);
		
		btnSearch = new JButton("search");
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.gridwidth = 2;
		gbc_btnSearch.insets = new Insets(0, 0, 0, 5);
		gbc_btnSearch.gridx = 2;
		gbc_btnSearch.gridy = 3;
		getContentPane().add(btnSearch, gbc_btnSearch);
	}

	private JPanel addToScrollPane(Component c) {
		JPanel consolePanel = new JPanel(new GridBagLayout());
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollBar = new GridBagConstraints();
		gbc_scrollBar.weighty = 1;
		gbc_scrollBar.anchor = GridBagConstraints.WEST;
		gbc_scrollBar.weightx = 1.0;
		gbc_scrollBar.gridwidth = 1;
		gbc_scrollBar.gridheight = 1;
		gbc_scrollBar.gridx = 0;
		gbc_scrollBar.gridy = 0;
		gbc_scrollBar.fill = GridBagConstraints.BOTH;
		consolePanel.add(scrollPane, gbc_scrollBar);
		JPanel p = new JPanel(new GridBagLayout());
		p.add(c, gbc_scrollBar);
		scrollPane.setViewportView(p);
		return consolePanel;
	}

	private void addJoinNWBtn() {
		
		resourceTextField = new JTextField();
		GridBagConstraints gbc_rtextField_1 = new GridBagConstraints();
		gbc_rtextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_rtextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtextField_1.gridx = 6;
		gbc_rtextField_1.gridy = 1;
		getContentPane().add(resourceTextField, gbc_rtextField_1);
		btnJoinNetwork = new JButton("join");
		GridBagConstraints gbc_btnJoinNetwork = new GridBagConstraints();
		gbc_btnJoinNetwork.insets = new Insets(0, 0, 5, 0);
		gbc_btnJoinNetwork.gridx = 7;
		gbc_btnJoinNetwork.gridy = 1;
		getContentPane().add(btnJoinNetwork, gbc_btnJoinNetwork);
	}

	public void addJoinNetworkAction(ActionListener l)
	{
		btnJoinNetwork.addActionListener(l);
	}
	
	private void createConsole() {
		
		final PrintStream out = System.out;
		OutputStream consoleOut = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				out.write(b);
				console.append(String.valueOf((char)b));
			}
		};
		System.setOut(new PrintStream(consoleOut, true));
		final PrintStream err = System.err;
		OutputStream errconsoleOut = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				err.write(b);
				console.append(String.valueOf((char)b));
			}
		};
		System.setErr(new PrintStream(errconsoleOut, true));
	}
	
	private void setUserNameandPort() {
		Random rn = new Random();
		usernameTexField.setText("user" + rn.nextInt(99));
		portTextField.setText(rn.nextInt(2000)+10000+"");
		resourceTextField.setText(rn.nextInt(10000)+10000+"");
	}

	public void setParent(String parent) {
		superPeer.setText(parent);
	}

	public void setRole(String roleName) {
		roleLable.setText(roleName);
	}

	public void setJoinButtonText(String string) {
		btnJoinNetwork.setText(string);
	}

	public String getPort() {
		return portTextField.getText();
	}

	public String getUserName() {
		return usernameTexField.getText();
	}

	public String getResources() {
		return resourceTextField.getText();
	}
	
	public void setPeers(Set<Node> peers) {
		peerTableModel.setNodes(peers);
	}
	
	public void setSubPeers(Set<Node> peers) {
		subPeerTableModel.setNodes(peers);
	}
	
	public void setSuperPeers(Set<Node> peers) {
		superPeerTableModel.setNodes(peers);
	}
	
	public void setFileList(Map<Node, Set<String>> fileIndex) {
		fileTableModel.setData(fileIndex);
	}

	public void addSearchButtonAction(ActionListener actionListener) {
		btnSearch.addActionListener(actionListener);
	}

	public String getSearchText() {
		return txtSearch.getText();
	}
	
	public void setSearchResults(Map<String, SearchResults> results) {
		searchResultsTableModel.setData(results.values());
	}
	
	public void refresh(){
		tab.revalidate();
		tab.repaint();
	}
}
