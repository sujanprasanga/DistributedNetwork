package lk.ac.mrt.distributed.messaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;

public class Node {
	
	public static final int TYPE_PEER = 1;
	public static final int TYPE_SUPER_PEER = 2;
	
	private static final ConnectionProperties connectionProperties = ConnectionProperties.getInstance();
	private final static Set<Node> immutableNodes = new HashSet<>();
	private static Node self;
	
	private final String ip;
	private final int port;
	private int resources = -1;
	private final String username;
	private long lastOnlineTimestamp = System.currentTimeMillis();
	private int vacantSlots;
	private int type = TYPE_PEER;
	
	public synchronized static void setSelf(String ip, int port, String username, int resources)
	{
		if(self != null)
		{
			immutableNodes.remove(self);
		}
		self = getNode(ip, port, username);
		self.setResources(resources);
	}
	
	public static List<Node> getNode(String[] arg)
	{
		List<Node> p = new ArrayList<Node>();
		for(int i=0; i<arg.length - 1; i+=3)
		{
			p.add(getNode(arg[i+1], Integer.parseInt(arg[i+2]), arg[i+3]));
		}
		return p;
	}
	
	public static Node self()
	{
		return self;
	}
	
	public static Node getNode(String ip, int port, String username, int resources) {
		Node p = getNode(ip, port, username);
		p.setResources(resources);
		return p;
	}
	
	public synchronized static Node getNode(String ip, int port, String username)
	{
		Node tmp = new Node(ip, port, username);
		if(immutableNodes.contains(tmp))
		{
			for(Node p : immutableNodes)
			{
				if(tmp.equals(p))
				{
					return p;
				}
			}
		}
		else
		{
			immutableNodes.add(tmp);
		}
		return tmp;
	}
	

	public static List<Node> exatractNodes(String[] parameters) {
		List<Node> nodes = new ArrayList<>();
		for(int i=1; i<parameters.length; i+=3)
		{
			String ip = parameters[i];
			int port = Integer.parseInt(parameters[i+1]);
			String username = parameters[i+2];
			nodes.add(getNode(ip, port, username));
		}
		return nodes;
	}
	
	private Node(String ip, int port, String username) {
		this.ip = ip;
		this.port = port;
		this.username = username;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String toString()
	{
		return getUsername() + "@" + getIp() + ":" + getPort();
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof Node))
		{
			return false;
		}
		Node node = (Node)o;
		return ip.equals(node.ip) && port == node.port;
	}

	public int hashCode()
	{
		return ip.hashCode() + port;
	}
	
	public int getResources() {
		return resources;
	}

	public void setResources(int resources) {
		this.resources = resources;
	}

	public boolean shouldCheckOnline()
	{
		return  resources == -1 ||
				((System.currentTimeMillis() - lastOnlineTimestamp)/1000 > 
		        connectionProperties.secondsToWaitBeforeRemovingPeer()/3);
	}
	
	public boolean isOffline()
	{
		return (System.currentTimeMillis() - lastOnlineTimestamp)/1000 > connectionProperties.secondsToWaitBeforeRemovingPeer();
	}
	
	public void online() {
		lastOnlineTimestamp = System.currentTimeMillis();
	}

	public int getVacantSlots() {
		return vacantSlots;
	}
	
	public void setVacantSlots(int vacantSlots) {
		this.vacantSlots = vacantSlots;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isSuperPeer() {
		return type == TYPE_SUPER_PEER;
	}

}
