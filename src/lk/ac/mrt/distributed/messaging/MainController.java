package lk.ac.mrt.distributed.messaging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.SwingUtilities;

import lk.ac.mrt.distributed.messaging.io.ConnectionProperties;
import lk.ac.mrt.distributed.messaging.io.DatagramReceiver;
import lk.ac.mrt.distributed.messaging.io.SocketClient;
import lk.ac.mrt.distributed.messaging.roles.AbstractPeerController;
import lk.ac.mrt.distributed.messaging.roles.ControllerSwitcher;
import lk.ac.mrt.distributed.messaging.roles.Messages;
import lk.ac.mrt.distributed.messaging.roles.PeerController;
import lk.ac.mrt.distributed.messaging.roles.SuperPeerController;
import lk.ac.mrt.distributed.messaging.ui.EventViewer;

public class MainController implements Messages, ControllerSwitcher {

	private final ConnectionProperties connectionProperties = new ConnectionProperties();
	private final EventViewer view;
	private AbstractPeerController controller;
	private volatile boolean started;
	private final List<ExecutorService> tasks = new ArrayList<>();
	private final MessageGenerator generator = new MessageGenerator();
	private final ResponseMessageDecoder decoder;
	private DatagramReceiver reciver;
	
	public MainController(EventViewer view)
	{
		decoder = new ResponseMessageDecoder(this);
		this.view = view;
		this.view.addWindowListener(new WindowListener() {
			
			@Override public void windowOpened(WindowEvent e){}
			@Override public void windowIconified(WindowEvent e){}
			@Override public void windowDeiconified(WindowEvent e){}
			@Override public void windowDeactivated(WindowEvent e){}
			@Override public void windowClosing(WindowEvent e) {
					stop();
			}
			@Override public void windowClosed(WindowEvent e){}
			@Override public void windowActivated(WindowEvent e){}
		});
		
		Scheduler.schedule(new Runnable() {
			
			@Override
			public void run() {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						
						@Override
						public void run() {
							{
								view.setRole(getRoleName());
								view.setParent(getParent());
								view.setPeers(getPeers());
								view.setSubPeers(getSubPeers());
								view.setSuperPeers(getSuperPeers());
								view.setFileList(getFileIndex());
								view.setSearchResults(getSearchResults());
								view.refresh();
							}
						}

					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 0, 1);
		
		view.addJoinNetworkAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				joinButtonClicked();
			}
		});
		
		view.addSearchButtonAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(controller != null){
					controller.search(view.getSearchText());
				}
			}
		});
	}
	

	private Map<String, SearchResults> getSearchResults() {
		if(controller != null){
			return controller.getSearchResults();
		}
		return Collections.emptyMap();
	}
	
	private void joinButtonClicked() {
		if(started)
		{
			started = false;
			stop();
			view.setJoinButtonText("join");
		}
		else
		{
			start();
			view.setJoinButtonText("leave");
			started = true;
		}
	}

	private void start() {
		setValues();
		bootstrap();
		startMessageReciever();
	}

	private void setValues() {
		try {
			String ip = getLocalHost().getAddress().getHostAddress();
			int port = Integer.parseInt(view.getPort());//9096;
			String un = view.getUserName();//"Sujan";
			int resources = Integer.parseInt(view.getResources());
			Node.setSelf(ip, port, un, resources);
			
		} catch (Exception e) {
			throw new DSException(e);
		}
		
	}
	
	 public static InterfaceAddress getLocalHost() {
	        System.setProperty("java.net.preferIPv4Stack", "true");
	        try {
	            Enumeration list = NetworkInterface.getNetworkInterfaces();
	            while (list.hasMoreElements()) {
	                NetworkInterface iface = (NetworkInterface) list.nextElement();
	                if (iface == null) continue;
	                if (!iface.isLoopback() && iface.isUp()) {
	                    Iterator it = iface.getInterfaceAddresses().iterator();
	                    while (it.hasNext()) {
	                        InterfaceAddress address = (InterfaceAddress) it.next();
	                        if (address == null) continue;
	                        InetAddress broadcast = address.getBroadcast();
	                        if (broadcast != null) {
	                            return address;
	                        }
	                    }
	                }
	            }
	        } catch (SocketException e) {
	            throw new DSException(e);
	        }
	        return null;
	}

	private void bootstrap()
	{
		String connectMessage = generator.register();
		String responseMessage = new SocketClient(connectionProperties).callBootStrap(connectMessage);
		decoder.decode(responseMessage);
	}
	
	private void startMessageReciever() {
		
		ExecutorService e = Scheduler.startThread(new Runnable() {
			@Override
			public synchronized void run() {
				reciver = new DatagramReceiver(Node.self().getPort());
				while(started)
				{
					try {
						decoder.decode(reciver.receive());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		tasks.add(e);
	}
	
	public String getParent() {
		return controller == null ? "" : controller.getParent();
	}

	public String getRoleName() {
		return controller == null ? "" : controller.getRoleName();
	}
	
	private Set<Node> getPeers() {
		return controller == null ? Collections.emptySet() : controller.getPeers();
	}
	

	private Set<Node> getSubPeers() {
		if(controller == null || !(controller instanceof SuperPeerController)){
			return Collections.emptySet();
		}
		return ((SuperPeerController)controller).getSubPeers();
	}
	
	private Set<Node> getSuperPeers() {
		return controller == null ? Collections.emptySet() : controller.getSuperPeers();
	}
	
	public void stop() {
		started = false;
		try	{
			new SocketClient(connectionProperties).callBootStrap(generator.leave());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(controller != null)
		{
			controller.stop();
		}
		stopScheduledTasks();
	}

	private void stopScheduledTasks() {
		for(ExecutorService e : tasks)
		{
			e.shutdownNow();
		}
	}

	@Override
	public void onRegOk(List<Node> nodes) {
		if(this.controller == null)
		{
			controller = new PeerController(connectionProperties, this);
		}
		controller.onRegOk(nodes);
		controller.start();
	}

	@Override
	public void onDiscoverSuperPeer(Node peer) {
		controller.addKnownNode(peer);
		controller.onDiscoverSuperPeer(peer);
	}

	@Override
	public void onMySuperPeer(Node sender, Node superPeer) {
		controller.addKnownNode(sender).addKnownNode(superPeer);
		controller.onMySuperPeer(sender, superPeer);
	}

	@Override
	public void onNoSuperPeer(Node peer) {
		controller.addKnownNode(peer);
		controller.onNoSuperPeer(peer);
	}

	@Override
	public void onJoinSuperPeer(Node peer) {
		controller.addKnownNode(peer);
		controller.onJoinSuperPeer(peer);
	}

	@Override
	public void onJoinSuperPeerOk(Node peer) {
		controller.addKnownNode(peer);
		controller.onJoinSuperPeerOk(peer);
	}

	@Override
	public synchronized void onPromoteToSuperPeer(Node peer) {
			controller.addKnownNode(peer);
			controller.onPromoteToSuperPeer(peer);
	}
	
	@Override
	public void onSuperPeerAnnounce(Node sender, Node superPeer, String messageId) {
		controller.addKnownNode(sender);
		controller.addKnownNode(superPeer);
		controller.onSuperPeerAnnounce(sender, superPeer, messageId);
	}

	public void pingR(Node sender) {
		// nothing to do
	}

	public void ping(Node peer) {
		controller.addKnownNode(peer);
		controller.onPing(peer);
	}

	@Override
	public synchronized void switchController(AbstractPeerController peerController){
		controller.stop();
		peerController.copyState(controller);
		controller = peerController;
		Node.self().setType(Node.TYPE_SUPER_PEER);
		controller.start();
	}

	@Override
	public void onSuperPeerSync(Node node) {
		controller.addKnownNode(node);
		controller.onSuperPeerSync(node);
	}

	@Override
	public void onSuperPeerSyncResponse(Node node) {
		controller.addKnownNode(node);
		controller.onSuperPeerSyncResponse(node);
	}

	@Override
	public void onForcePromoteToSuperPeer(Node sender) {
		controller.addKnownNode(sender);
		controller.onForcePromoteToSuperPeer(sender);
	}

	@Override
	public void onTransferPeer(Node sender, Node superPeer) {
		controller.addKnownNode(sender);
		controller.onTransferPeer(sender, superPeer);
	}

	@Override
	public void onNotMySuperPeer(Node sender) {
		controller.addKnownNode(sender);
		controller.onNotMySuperPeer(sender);
	}

	@Override
	public void onFileListSync(Node sender) {
		controller.addKnownNode(sender);
		controller.onFileListSync(sender);
	}

	@Override
	public void onFileListResponse(Node sender, Set<String> files) {
		controller.addKnownNode(sender);
		controller.onFileListResponse(sender, files);
	}

	private Map<Node, Set<String>> getFileIndex() {
		if(controller == null){
			return Collections.emptyMap();
		}
		return controller.getFileIndex();
	}

	@Override
	public void onSearch(Node sender, Node queryOwner, String query, String messageId) {
		controller.addKnownNode(sender);
		controller.addKnownNode(queryOwner);
		controller.onSearch(sender, queryOwner, query, messageId);
	}

	@Override
	public void onSearchResult(Node sender, Node resultOwner, Set<String> results, String messageId) {
		controller.addKnownNode(sender);
		controller.addKnownNode(resultOwner);
		controller.onSearchResult(sender, resultOwner, results, messageId);
	}
}
