package lk.ac.mrt.distributed.messaging;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public class ResponseMessageDecoder {

	private final MainController controller;
	
	public ResponseMessageDecoder(MainController controller){
		this.controller = controller;
	}
	
	public void decode(String response) {
		int length = Integer.parseInt(response.substring(0, 4));
		String data = response.substring(5, length);
		System.out.println("recieved: " + response.substring(0, length));
		int commandEndIndex = data.indexOf(MessageConstants.MESSAGE_ELEMENT_SEPARATER);
		String command = data.substring(0, commandEndIndex);
		String[] parameters = data.substring(commandEndIndex + 1).split(MessageConstants.MESSAGE_ELEMENT_SEPARATER);
		decode(command, parameters);
	}

	private void decode(String command, String[] parameters) {
		
		if(MessageConstants.REGOK.equals(command))
		{
			regOk(parameters);
			return;
		}
		
		Node sender = getSender(parameters);
		sender.online();
		if(MessageConstants.DISCOVER_SUPER_PEER.equals(command))
		{
			discoverSuperPeer(sender);
		}
		else if(MessageConstants.JOIN_SUPER_PEER.equals(command))
		{
			joinSuperPeer(sender);
		}
		else if(MessageConstants.JOIN_SUPER_PEER_OK.equals(command))
		{
			joinSuperPeerOk(sender);
		}
		else if(MessageConstants.MY_SUPER_PEER.equals(command))
		{
			mySuperPeer(sender, parameters);
		}
		else if(MessageConstants.PROMOTE_TO_SUPER_PEER.equals(command))
		{
			controller.onPromoteToSuperPeer(sender);
		}
		else if(MessageConstants.FORCE_PROMOTE_TO_SUPER_PEER.equals(command))
		{
			controller.onForcePromoteToSuperPeer(sender);
		}
		else if(MessageConstants.SUPER_PEER_ANNOUNCE.equals(command))
		{
			superPeerAnnounce(sender, parameters);
		}
		else if(MessageConstants.PING.equals(command))
		{
			controller.ping(sender);
		}
		else if(MessageConstants.JOIN_SUPER_PEER.equals(command))
		{
			controller.pingR(sender);
		}
		else if(MessageConstants.SUPER_PEER_SYNC.equals(command))
		{
			superPeerSync(sender, parameters);
		}
		else if(MessageConstants.SUPER_PEER_SYNC_RESPONSE.equals(command))
		{
			superPeerSyncResponse(sender, parameters);
		}
		else if(MessageConstants.TRANSFER_PEER.equals(command))
		{
			transferPeer(sender, parameters);
		}
		else if(MessageConstants.NOT_MY_SUPER_PEER.equals(command))
		{
			notMySuperPeer(sender);
		}
		else if(MessageConstants.FILE_LIST_SYNC.equals(command))
		{
			controller.onFileListSync(sender);
		}
		else if(MessageConstants.FILE_LIST_SYNC_RESPONSE.equals(command))
		{
			onFileList(sender, parameters);
		}
		else if(MessageConstants.SEARCH.equals(command))
		{
			onSearch(sender, parameters);
		}
		else if(MessageConstants.SEARCH_RESPONSE.equals(command))
		{
			onSearchResult(sender, parameters);
		}
		
	}


	private void onSearchResult(Node sender, String[] parameters) {
		Set<String> results = new HashSet<>();
		for(int i = 11; i< parameters.length; i++){
			results.add(base64Decode(parameters[i]));
		}
		controller.onSearchResult(sender, getNode(parameters, 6), results, parameters[5]);
	}

	private void onSearch(Node sender, String[] parameters) {
		controller.onSearch(sender, getNode(parameters, 6), base64Decode(parameters[11]), parameters[5]);
	}

	private void superPeerAnnounce(Node sender, String[] parameters) {
		controller.onSuperPeerAnnounce(sender, getNode(parameters, 5), parameters[10]);
	}

	private void onFileList(Node sender, String[] parameters) {
		Set<String> files = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		for (int i = 5; i < parameters.length; i++) {
			String file = base64Decode(parameters[i]);
			files.add(file);
		}
		controller.onFileListResponse(sender, files);
	}

	private String base64Decode(String str) {
		try {
			byte[] bytes = str.getBytes(ConnectionProperties.getInstance().getEncoding());
			byte[] decoded = Base64.getDecoder().decode(bytes);
			String file = new String(decoded).intern();
			return file;
		} catch (UnsupportedEncodingException e) {
			throw new DSException(e);
		}
	}
	
	private void transferPeer(Node sender, String[] parameters) {
		controller.onTransferPeer(sender, getNode(parameters, 5));
	}

	private void notMySuperPeer(Node sender) {
		controller.onNotMySuperPeer(sender);
	}

	private void superPeerSync(Node sender, String[] parameters) {
		sender.setVacantSlots(Integer.parseInt(parameters[5]));
		controller.onSuperPeerSync(sender);
	}
	
	private void superPeerSyncResponse(Node sender, String[] parameters) {
		sender.setVacantSlots(Integer.parseInt(parameters[5]));
		controller.onSuperPeerSyncResponse(sender);
	}

	private void joinSuperPeer(Node sender) {
		controller.onJoinSuperPeer(sender);
	}

	private void joinSuperPeerOk(Node sender) {
		controller.onJoinSuperPeerOk(sender);
	}

	private void mySuperPeer(Node sender, String[] parameters) {
		controller.onMySuperPeer(sender, getNode(parameters, 5));
	}

	private void discoverSuperPeer(Node sender) {
		controller.onDiscoverSuperPeer(sender);
	}

	private void regOk(String[] parameters) {
		List<Node> nodes = Node.exatractNodes(parameters);
		controller.onRegOk(nodes);
	}

	private Node getSender(String[] parameters) {
		return getNode(parameters, 0);
	}
	
	private Node getNode(String[] parameters, int arrayOffset) {
		String ip = parameters[arrayOffset].trim();
		int port = Integer.parseInt(parameters[arrayOffset+1]);
		String un = parameters[arrayOffset+2];
		int resources = Integer.parseInt(parameters[arrayOffset+4]);
		Node n = Node.getNode(ip, port, un, resources);
		n.setType(Integer.parseInt(parameters[arrayOffset+3]));
		return n;
	}
}
