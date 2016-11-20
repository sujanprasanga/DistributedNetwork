package lk.ac.mrt.distributed.messaging;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Set;

import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public class MessageGenerator {

	
	public String register() {
		return bsMessage(MessageConstants.REG);
	}

	public String leave() {
		return bsMessage(MessageConstants.LEAVE);
	}

	private String bsMessage(String command) {
		return attachLength(appendBasicInfo(command, Node.self())).toString();
	}
	
	public String superPeerDiscovery() {
		return peerMessage(MessageConstants.DISCOVER_SUPER_PEER);
	}
	
	public String transferPeer(Node newSuperPeer) {
		StringBuilder sb = appendAllInfo(MessageConstants.TRANSFER_PEER, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(newSuperPeer.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(newSuperPeer.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(newSuperPeer.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(newSuperPeer.getType());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(newSuperPeer.getResources());
		return attachLength(sb).toString();
	}
	

	public String mySuperPeer(Node superPeer) {
		StringBuilder sb = appendAllInfo(MessageConstants.MY_SUPER_PEER, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getType());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getResources());
		return attachLength(sb).toString();
	}
	

	public String superPeerAnnounce(Node superPeer, String messageId) {
		StringBuilder sb = appendAllInfo(MessageConstants.SUPER_PEER_ANNOUNCE, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getType());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(superPeer.getResources());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(messageId);
		return attachLength(sb).toString();
	}
	
	public String search(Node queryOwner, String query, String messageId) {
		StringBuilder sb = appendAllInfo(MessageConstants.SEARCH, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(messageId);
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(queryOwner.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(queryOwner.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(queryOwner.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(queryOwner.getType());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(queryOwner.getResources());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(base64Encode(query));
		return attachLength(sb).toString();
	}
	

	public String searchResponse(Node resultOwner, String messageId, Set<String> results) {
		StringBuilder sb = appendAllInfo(MessageConstants.SEARCH_RESPONSE, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(messageId);
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(resultOwner.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(resultOwner.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(resultOwner.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(resultOwner.getType());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(resultOwner.getResources());
		for(String s : results){
			sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(base64Encode(s));
		}
		return attachLength(sb).toString();
	}

	public String ping() {
		return peerMessage(MessageConstants.PING);
	}
	
	public String pingR() {
		return peerMessage(MessageConstants.PING_RESPONSE);
	}
	

	public String promoteToSuperPeer() {
		return peerMessage(MessageConstants.PROMOTE_TO_SUPER_PEER);
	}
	
	public String forcePromoteToSuperPeer() {
		return peerMessage(MessageConstants.FORCE_PROMOTE_TO_SUPER_PEER);
	}
	
	public String joinSuperPeer() {
		return peerMessage(MessageConstants.JOIN_SUPER_PEER);
	}
	
	public String joinSuperPeerOk() {
		return peerMessage(MessageConstants.JOIN_SUPER_PEER_OK);
	}
	
	public String superPeerSync() {
		StringBuilder sb = appendAllInfo(MessageConstants.SUPER_PEER_SYNC, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(Node.self().getVacantSlots());
		return attachLength(sb).toString();
	}
	
	public String superPeerSyncResponse() {
		StringBuilder sb = appendAllInfo(MessageConstants.SUPER_PEER_SYNC_RESPONSE, Node.self());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(Node.self().getVacantSlots());
		return attachLength(sb).toString();
	}

	public String fileListSync() {
		return peerMessage(MessageConstants.FILE_LIST_SYNC);
	}

	public String notMySuperPeer() {
		return peerMessage(MessageConstants.NOT_MY_SUPER_PEER);
	}
	

	public String fileListSyncResponse() {
		StringBuilder sb = appendAllInfo(MessageConstants.FILE_LIST_SYNC_RESPONSE, Node.self());
		for(String f : SharedFiles.getInstance().getSharedFiles()){
				sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(base64Encode(f));
		}
		return attachLength(sb).toString();
	}

	private String base64Encode(String str) {
		try {
			return Base64.getEncoder().encodeToString(str.getBytes(ConnectionProperties.getInstance().getEncoding()));
		} catch (UnsupportedEncodingException e) {
			throw new DSException(e);
		}
	}
	
	private String peerMessage(String command) {
		return attachLength(appendAllInfo(command, Node.self())).toString();
	}
	
	private StringBuilder appendAllInfo(String command, Node node)
	{
		StringBuilder sb = appendBasicInfo(command, node);
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(node.getResources());
		return sb;
	}
	
	private StringBuilder appendBasicInfo(String command, Node node)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(command);
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(node.getIp());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(node.getPort());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(node.getUsername());
		sb.append(MessageConstants.MESSAGE_ELEMENT_SEPARATER).append(node.getType());
		return sb;
	}


	private StringBuilder attachLength(StringBuilder sb)
	{
		int length = sb.length() + 5;
		String lengthString = padZeroes(length);
		sb.insert(0, lengthString);
		return sb;
	}

	private String padZeroes(int length)
	{
		if(length < 10)
		{
			return "000" + length + MessageConstants.MESSAGE_ELEMENT_SEPARATER;
		}
		if(length < 100)
		{
			return "00" + length + MessageConstants.MESSAGE_ELEMENT_SEPARATER;
		}
		if(length < 1000)
		{
			return "0" + length + MessageConstants.MESSAGE_ELEMENT_SEPARATER;
		}
		return Integer.toString(length) + MessageConstants.MESSAGE_ELEMENT_SEPARATER;
	}
}
