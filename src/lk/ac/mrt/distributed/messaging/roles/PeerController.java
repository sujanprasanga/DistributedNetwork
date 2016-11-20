package lk.ac.mrt.distributed.messaging.roles;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import lk.ac.mrt.distributed.messaging.Node;
import lk.ac.mrt.distributed.messaging.Scheduler;
import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public class PeerController extends AbstractPeerController {

	private Node superPeer;
	
	public PeerController(ConnectionProperties connectionProperties, ControllerSwitcher switcher) {
		super(connectionProperties, switcher);
	}

	@Override
	public void onDiscoverSuperPeer(Node node) {
		if(superPeer != null && !superPeer.isOffline()){
			messageSender.sendMySuperPeer(node, superPeer);
		}
	}
	
	public void start()
	{
		super.start();
		scheduleForceSuperPeerSelection();
	}

	private void scheduleForceSuperPeerSelection() {
			ExecutorService e = Scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				{
					if(superPeer == null && noOnlineSuperPeers()){
						Node p = getOnlinePeerWithMaxResources();
						if(p== null) return;
						if(p.getResources() > Node.self().getResources())
						{
							messageSender.sendPromoteToSuperPeerRequest(p);
						}
					}
					if(superPeer != null && superPeer.isOffline()){
						superPeer = null;
					}
				}
			}
		}, connectionProperties.getSuperPeerFindTimeout(),
		   connectionProperties.getSuperPeerDiscoveryInterval());
		tasks.add(e);
	}

	private boolean noOnlineSuperPeers() {
		for(Node n : getSuperPeers()){
			if(!n.isOffline()){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onMySuperPeer(Node sender, Node superPeer) {
		addKnownNode(superPeer);
		if(this.superPeer == null){
			messageSender.sendJoinSuperPeer(superPeer);
		}
	}

	@Override
	public void onNoSuperPeer(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onJoinSuperPeer(Node node) {
		if(superPeer != null && !superPeer.isOffline()){
			messageSender.sendMySuperPeer(node, superPeer);
		}
	}

	@Override
	public synchronized void onJoinSuperPeerOk(Node node) {
		if(superPeer == null || superPeer.isOffline()){
			superPeer = node;
		}
	}

	@Override
	public synchronized void onPromoteToSuperPeer(Node node) {
		if(superPeer == null || superPeer.isOffline()){
			promoteToSuperPeer(node);
		} else {
			messageSender.sendMySuperPeer(node, superPeer);
		}
	}
	
	@Override
	public synchronized void onForcePromoteToSuperPeer(Node node) {
			promoteToSuperPeer(node);
	}

	private void promoteToSuperPeer(Node node) {
		SuperPeerController controller = new SuperPeerController(connectionProperties, switcher);
		controller.addSubPeer(node);
		switcher.switchController(controller);
	}

	@Override
	public String getParent() {
		return superPeer == null ? "" : superPeer.toString();
	}

	@Override
	public String getRoleName() {
		return "peer";
	}

	@Override
	protected boolean shouldSendDSP(Node n) {
		return superPeer == null || superPeer.isOffline();
	}

	@Override
	public void onSuperPeerSync(Node node) {
	}

	@Override
	public void onSuperPeerSyncResponse(Node node) {
	}

	@Override
	public void onTransferPeer(Node sender, Node superPeer) {
		if(!sender.equals(this.superPeer)){
			messageSender.sendNotMySuperPeer(sender);
		} else {
			this.superPeer = superPeer;
			messageSender.sendJoinSuperPeer(superPeer);
		}
	}

	@Override
	public void onNotMySuperPeer(Node sender) {
	}

	@Override
	public void onFileListSync(Node sender) {
		if(!sender.equals(this.superPeer)){
			messageSender.sendNotMySuperPeer(sender);
		} else {
			messageSender.sendFileListSyncResponse(superPeer);
		}
	}

	@Override
	public void onFileListResponse(Node sender, Set<String> files) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void search(String query, String messageId) {
		messageSender.sendSearch(superPeer, Node.self(), query, messageId);
	}

	@Override
	public void onSearch(Node sender, Node queryOwner, String query, String messageId) {
		// TODO Auto-generated method stub
		
	}
}
