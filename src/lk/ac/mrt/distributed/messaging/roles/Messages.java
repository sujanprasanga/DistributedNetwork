package lk.ac.mrt.distributed.messaging.roles;

import java.util.List;
import java.util.Set;

import lk.ac.mrt.distributed.messaging.Node;

public interface Messages {

	void onRegOk(List<Node> node);
	void onDiscoverSuperPeer(Node node);
	void onMySuperPeer(Node sender, Node superPeer);
	void onNoSuperPeer(Node node);
	void onJoinSuperPeer(Node node);
	void onJoinSuperPeerOk(Node node);
	void onPromoteToSuperPeer(Node node);
	void onSuperPeerAnnounce(Node sender, Node superPeer, String messageId);
	void onSuperPeerSync(Node node);
	void onSuperPeerSyncResponse(Node node);
	void onForcePromoteToSuperPeer(Node sender);
	void onTransferPeer(Node sender, Node superPeer);
	void onNotMySuperPeer(Node sender);
	void onFileListSync(Node sender);
	void onFileListResponse(Node sender, Set<String> files);
	void onSearch(Node sender, Node queryOwner, String query, String messageId);
	void onSearchResult(Node sender, Node resultOwner, Set<String> results, String messageId);
}
