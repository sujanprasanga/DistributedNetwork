package lk.ac.mrt.distributed.messaging.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import lk.ac.mrt.distributed.messaging.LoopBackController;
import lk.ac.mrt.distributed.messaging.MessageSender;
import lk.ac.mrt.distributed.messaging.Node;
import lk.ac.mrt.distributed.messaging.Scheduler;
import lk.ac.mrt.distributed.messaging.SearchResults;
import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public abstract class AbstractPeerController implements Messages {

	protected final ConnectionProperties connectionProperties;
	protected final MessageSender messageSender;
	protected final List<ExecutorService> tasks = new ArrayList<>();
	private Set<Node> knownNodes = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Set<Node> subPeers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	protected final ControllerSwitcher switcher;
	protected final Random random = new Random();
	
	private final Map<String, SearchResults> searchResults = new ConcurrentHashMap<>();
	
	public AbstractPeerController(ConnectionProperties connectionProperties, ControllerSwitcher switcher)
	{
		this.connectionProperties = connectionProperties;
		this.switcher = switcher;
		messageSender = new MessageSender(connectionProperties);
	}
	
	@Override
	public void onRegOk(List<Node> nodes) {
		selectTwoPeers(nodes);
	}
	
	private void selectTwoPeers(List<Node> peers) {
		if(peers.size() > 1)
		{
			while(this.knownNodes.size() < 2)
			{
				int nextInt = random.nextInt(peers.size());
				Node peer = peers.get(nextInt);
				if(!this.knownNodes.contains(peer))
				{
					addKnownNode(peer);
				}
			}
		}
		else if(peers.size() == 1)
		{
			addKnownNode(peers.get(0));
		}
	}
	
	protected Node getOnlinePeerWithMaxResources() {
		return getPeerWithMaxResources(getOnlineNodes(knownNodes));
	}
	
	protected Collection<Node> getOnlineNodes(Collection<Node> nodes){
		List<Node> r = new ArrayList<Node>();
		for(Node n : nodes){
			if(!n.isOffline()){
				r.add(n);
			}
		}
		return r;
	}
	
	protected Node getPeerWithMaxResources(Collection<Node> peers) {
		List<Node> tmp = new ArrayList<>();
		tmp.addAll(peers);
		Collections.sort(tmp, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o2.getResources() - o1.getResources();
			}
		});
		return tmp.isEmpty() ? null : tmp.get(0);
	}
	
	public abstract String getParent();
	public abstract String getRoleName();
	
	public final void stop()
	{
		for(ExecutorService e : tasks)
		{
			e.shutdownNow();
		}
	}
	
	public void start()
	{
		startSuperPeerDiscovery();
		startPeerOnlineCheck();
	}

	private void startSuperPeerDiscovery() {
		ExecutorService e = Scheduler.schedule(new Runnable() {
			
			@Override
			public void run() {
				{
					for(Node n : knownNodes)
					{
						if(shouldSendDSP(n)){
							messageSender.sendSuperPeerDiscoveryRequest(n);
						}
					}
				}
			}

		}, 0, connectionProperties.getSuperPeerDiscoveryInterval());
		tasks.add(e);
	}
	
	private void startPeerOnlineCheck() {
		ExecutorService e = Scheduler.schedule(new Runnable() {
			
			@Override
			public void run() {
				{
					for(Node n : knownNodes)
					{
						if(n.shouldCheckOnline())
						{
							ping(n);
						}
					}
				}
			}
		}, 0, connectionProperties.getPeerRefreshInterval());
		tasks.add(e);
	}
	
	protected abstract boolean shouldSendDSP(Node n);

	private void ping(Node peer) {
		messageSender.ping(peer);
	}

	public void onPing(Node peer) {
		addKnownNode(peer);
		messageSender.pingR(peer);
	}

	@Override
	public void onSuperPeerAnnounce(Node sender, Node superPeer, String messageId) {
		if(!LoopBackController.isLoopingMessage(messageId)){
			for(Node n : knownNodes){
				LoopBackController.addMessageId(messageId);
				messageSender.sendSuperPeerAnnounce(n, superPeer, messageId);
			}
		}
	}
	
	public void copyState(AbstractPeerController source){
		knownNodes.addAll(source.knownNodes);
	}

	public Set<Node> getPeers() {
		return knownNodes;
	}

	public Set<Node> getSuperPeers() {
		Set<Node> s = new HashSet<>();
		for(Node n : knownNodes){
			if(n.isSuperPeer()){
				s.add(n);
			}
		}
		return s;
	}
	
	public AbstractPeerController addKnownNode(Node n) {
		if(Node.self() != n){
			knownNodes.add(n);
		}
		return this;
	}
	
	public Set<Node> getKnownNodes(){
		return knownNodes;
	}

	public Set<Node> getSubPeers() {
		Set<Node> s = new HashSet<>();
		for(Node n : subPeers){
			if(!n.isSuperPeer()){
				s.add(n);
			}
		}
		return s;
	}

	public void addSubPeer(Node subPeer) {
		this.subPeers.add(subPeer);
	}
	
	public void removeSubPeer(Node subPeer) {
		this.subPeers.remove(subPeer);
	}

	public Map<Node, Set<String>> getFileIndex() {
		return Collections.emptyMap();
	}
	
	
	public void search(String query){
		String messageId = LoopBackController.getNewMessageId();
		searchResults.put(messageId, new SearchResults(query, messageId));
		search(query, messageId);
	}
	
	public abstract void search(String query, String messageId);
	

	public void updateResult(String messageId, Node n, Set<String> results) {
		SearchResults r = searchResults.get(messageId);
		r.addSearchResult(n, results);
	}

	public Map<String, SearchResults> getSearchResults() {
		return searchResults;
	}
	

	@Override
	public void onSearchResult(Node sender, Node resultOwner, Set<String> results, String messageId) {
		updateResult(messageId, resultOwner, results);
	}
}
