package lk.ac.mrt.distributed.messaging;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LoopBackController {

	private static final Set<String> messageIds = new HashSet<>();
	
	public static synchronized String getNewMessageId(){
		String id = generateId();
		while(messageIds.contains(id)){
			id = generateId();
		}
		addMessageId(id);
		return id;
	}
	
	public static synchronized void addMessageId(String id){
		messageIds.add(id);
	}
	
	public static synchronized boolean isLoopingMessage(String id){
		return messageIds.contains(id);
	}

	private static String generateId() {
		return Node.self().getUsername() + '#' + UUID.randomUUID().toString();
	}
}
