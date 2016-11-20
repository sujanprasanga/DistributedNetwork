package lk.ac.mrt.distributed.messaging;

import java.io.IOException;
import java.util.Set;

import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;
import lk.ac.mrt.distributed.messaging.io.DatagramSender;

public class MessageSender {

	private final ConnectionProperties connectionProperties;
	private final DatagramSender sender;
	private final MessageGenerator generator = new MessageGenerator();
	
	public MessageSender(ConnectionProperties connectionProperties)
	{
		this.connectionProperties = connectionProperties;
		sender = new DatagramSender(connectionProperties);
	}
	
	protected void send(String ip, int port, String message) {
		try {
			sender.send(ip, port, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSuperPeerDiscoveryRequest(Node n) {
		send(n.getIp(), n.getPort(), generator.superPeerDiscovery());
	}

	public void sendMySuperPeer(Node node, Node superPeer) {
		send(node.getIp(), node.getPort(), generator.mySuperPeer(superPeer));
	}

	public void ping(Node node) {
		send(node.getIp(), node.getPort(), generator.ping());
	}

	public void pingR(Node node) {
		send(node.getIp(), node.getPort(), generator.pingR());
	}

	public void sendPromoteToSuperPeerRequest(Node node) {
		send(node.getIp(), node.getPort(), generator.promoteToSuperPeer());
	}

	public void sendForcePromoteToSuperPeerRequest(Node node) {
		send(node.getIp(), node.getPort(), generator.forcePromoteToSuperPeer());
	}

	public void sendJoinSuperPeer(Node node) {
		send(node.getIp(), node.getPort(), generator.joinSuperPeer());
	}
	
	public void sendJoinSuperPeerOk(Node node) {
		send(node.getIp(), node.getPort(), generator.joinSuperPeerOk());
	}

	public void sendSuperPeerSyncRequest(Node node) {
		send(node.getIp(), node.getPort(), generator.superPeerSync());
	}

	public void sendSuperPeerSyncResponse(Node node) {
		send(node.getIp(), node.getPort(), generator.superPeerSyncResponse());
	}

	public void sendTransferPeerRequest(Node node, Node newSuperPeer) {
		send(node.getIp(), node.getPort(), generator.transferPeer(newSuperPeer));
	}

	public void sendNotMySuperPeer(Node node) {
		send(node.getIp(), node.getPort(), generator.notMySuperPeer());
	}

	public void sendFileListSyncRequest(Node node) {
		send(node.getIp(), node.getPort(), generator.fileListSync());
	}

	public void sendFileListSyncResponse(Node node) {
		send(node.getIp(), node.getPort(), generator.fileListSyncResponse());
	}

	public void sendSuperPeerAnnounce(Node node, Node superPeer, String messageId) {
		send(node.getIp(), node.getPort(), generator.superPeerAnnounce(superPeer, messageId));
	}
	
	public void sendSearch(Node node, Node sender, String query, String messageId) {
		send(node.getIp(), node.getPort(), generator.search(sender, query, messageId));
	}

	public void sendSearchResponse(Node queryOwner, String messageId, Node resultOwner, Set<String> results) {
		send(queryOwner.getIp(), queryOwner.getPort(), generator.searchResponse(resultOwner, messageId, results));
	}
}
