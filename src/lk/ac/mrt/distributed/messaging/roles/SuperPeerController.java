package lk.ac.mrt.distributed.messaging.roles;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import lk.ac.mrt.distributed.messaging.LoopBackController;
import lk.ac.mrt.distributed.messaging.Node;
import lk.ac.mrt.distributed.messaging.Scheduler;
import lk.ac.mrt.distributed.messaging.SharedFiles;
import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public class SuperPeerController extends AbstractPeerController {
	
	private final Map<Node, Set<String>> fileIndex = new ConcurrentHashMap<>();
	private String superPeerAnnounceMessageId;

	public SuperPeerController(ConnectionProperties connectionProperties, ControllerSwitcher switcher) {
		super(connectionProperties, switcher);
		addMyFileList();
		startFileListSyncTask();
		startSuperPeerSyncTask();
		startSuperPeerDivisionTask();
	}

	public void start()
	{
		super.start();
		announceSuperPeer();
	}
	
	private void addMyFileList() {
		fileIndex.put(Node.self(), SharedFiles.getInstance().getSharedFiles());
	}

	private void startFileListSyncTask() {
		ExecutorService e = Scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				for(Node n : getSubPeers()){
						sendFileListSyncRequest(n);
				}
			}
		},0, connectionProperties.getSuperPeerSyncRate());
		tasks.add(e);
	}
	
	private void sendFileListSyncRequest(Node node) {
		messageSender.sendFileListSyncRequest(node);
	}

	private void startSuperPeerSyncTask() {
		ExecutorService e = Scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				setVacantSlotCount();
				for(Node n : getSuperPeers()){
						sendSuperPeerSyncRequest(n);
				}
			}
		},0, connectionProperties.getSuperPeerSyncRate());
		tasks.add(e);
	}
	
	private void sendSuperPeerSyncRequest(Node n) {
		messageSender.sendSuperPeerSyncRequest(n);
	}

	private void startSuperPeerDivisionTask() {
		ExecutorService e = Scheduler.schedule(new Runnable() {
			
			@Override
			public void run() {
				if(getSubPeers().size() > connectionProperties.getMaxPeerCount()){
					if(noKnownSuperPeersAreVacant()){
						createNewSuperPeer();
					}else{
						transferPeers();
					}
				}
			}
		}, connectionProperties.getSuperPeerDivisionRate());
		tasks.add(e);
	}
	
	private boolean noKnownSuperPeersAreVacant() {
		return getVacantSuperPeer() == null;
	}

	private void createNewSuperPeer() {
		Node newSuperPeer = getPeerWithMaxResources(getOnlineNodes(getSubPeers()));
		messageSender.sendForcePromoteToSuperPeerRequest(newSuperPeer);
	}


	private void transferPeers() {
		Set<Node> transferringNodes = new HashSet<>();
		while(transferringNodes.size() < getSubPeers().size() - connectionProperties.getMaxPeerCount()){
			transferringNodes.add((Node)getSubPeers().toArray()[random.nextInt(getSubPeers().size())]);
		}
		transferPeers(transferringNodes);
	}
	
	private void transferPeers(Set<Node> transferringNodes) {
		for(Node n : transferringNodes){
			transeferPeer(n);
		}
	}

	private void transeferPeer(Node n) {
		messageSender.sendTransferPeerRequest(n, getVacantSuperPeer());
	}

	private Node getVacantSuperPeer() {
		for(Node n : getSuperPeers()){
			if(!n.isOffline() && n.getVacantSlots() > 0){
				n.setVacantSlots(n.getVacantSlots() - 1);
				return n;
			}
		}
		return null;
	}

	public void announceSuperPeer() {
		for(Node n : getKnownNodes()){
		  messageSender.sendSuperPeerAnnounce(n, Node.self(), getSuperPeerAnnounceMessageId());
		}
	}

	private String getSuperPeerAnnounceMessageId() {
		if(superPeerAnnounceMessageId == null){
			superPeerAnnounceMessageId = LoopBackController.getNewMessageId();
		}
		return superPeerAnnounceMessageId;
	}
	
	@Override
	public void onDiscoverSuperPeer(Node node) {
		messageSender.sendMySuperPeer(node, Node.self());
	}

	@Override
	public void onMySuperPeer(Node sender, Node superPeer) {
		addKnownNode(superPeer);
	}

	@Override
	public void onNoSuperPeer(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onJoinSuperPeer(Node node) {
		acceptSubPeer(node);
	}

	@Override
	public void onJoinSuperPeerOk(Node node) {
		getSuperPeers().add(node);
	}

	@Override
	public void onPromoteToSuperPeer(Node node) {
		acceptSubPeer(node);
	}

	private void acceptSubPeer(Node node) {
		addSubPeer(node);
		messageSender.sendJoinSuperPeerOk(node);
		Scheduler.executeIn(3, new Runnable() {
			@Override
			public void run() {
				messageSender.sendFileListSyncRequest(node);
			}
		});
	}

	@Override
	public String getParent() {
		return "";
	}

	@Override
	public String getRoleName() {
		return "superPeer";
	}

	@Override
	protected boolean shouldSendDSP(Node n) {
		return !getSubPeers().contains(n);
	}

	@Override
	public void onSuperPeerSync(Node node) {
		setVacantSlotCount();
		messageSender.sendSuperPeerSyncResponse(node);
	}

	private void setVacantSlotCount() {
		Node.self().setVacantSlots(connectionProperties.getMaxPeerCount() - getSubPeers().size());
	}

	@Override
	public void onSuperPeerSyncResponse(Node node) {
	}

	@Override
	public void onForcePromoteToSuperPeer(Node sender) {
		sendSuperPeerSyncRequest(sender);
	}

	@Override
	public void onTransferPeer(Node sender, Node superPeer) {
		messageSender.sendNotMySuperPeer(sender);
	}

	@Override
	public void onNotMySuperPeer(Node sender) {
		removeSubPeer(sender);
		fileIndex.remove(sender);
	}

	@Override
	public void onFileListSync(Node sender) {
		messageSender.sendNotMySuperPeer(sender);
	}

	@Override
	public void onFileListResponse(Node sender, Set<String> files) {
		fileIndex.put(sender, files);
	}
	
	@Override
	public Map<Node, Set<String>> getFileIndex() {
		return fileIndex;
	}
	
	@Override
	public AbstractPeerController addKnownNode(Node n) {
		if(Node.self() != n && !getKnownNodes().contains(n)){
			super.addKnownNode(n);
			messageSender.sendSuperPeerAnnounce(n, Node.self(), getSuperPeerAnnounceMessageId());
		}
		return this;
	}
	
	@Override
	public void search(String query, String messageId) {
		findInSubPeers(query, Node.self(), messageId);
		for(Node s : getSuperPeers()){
			messageSender.sendSearch(s, Node.self(), query, messageId);
		}
	}

	private void findInSubPeers(String query, Node queryOwner, String messageId) {
		for(Entry<Node,Set<String>> e : fileIndex.entrySet()){
			Node n = e.getKey();
			Set<String> results = new HashSet<>();
			for(String s : e.getValue()){
				if(s.toLowerCase().contains(query.toLowerCase())){
					results.add(s);
				}
			}
			if(!results.isEmpty()){
				if(Node.self().equals(queryOwner)){
					updateResult(messageId, n, results);
				}else{
					sendResult(queryOwner, messageId, n, results);
				}
			}
		}
	}

	private void sendResult(Node finder, String messageId, Node resultOwner, Set<String> results) {
		messageSender.sendSearchResponse(finder, messageId, resultOwner, results);
	}

	@Override
	public void onSearch(Node sender, Node queryOwner, String query, String messageId) {
		if(!LoopBackController.isLoopingMessage(messageId)){
			findInSubPeers(query, queryOwner, messageId);
			for(Node s : getSuperPeers()){
				LoopBackController.addMessageId(messageId);
				messageSender.sendSearch(s, queryOwner, query, messageId);
			}
		}
	}
}
