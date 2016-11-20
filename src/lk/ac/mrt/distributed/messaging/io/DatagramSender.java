package lk.ac.mrt.distributed.messaging.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramSender {

	private final ConnectionProperties conectionProperties;
	
	public DatagramSender(ConnectionProperties conectionProperties)
	{
		this.conectionProperties = conectionProperties;
	}
	
	public synchronized void send(String host, int port, String message) throws IOException
	{
		InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
		DatagramChannel channel = DatagramChannel.open();
		final ByteBuffer buf = ByteBuffer.allocate(conectionProperties.getMessageBufferSize());
		buf.clear();
		buf.put(message.getBytes("UTF-8"));
		buf.flip();
		channel.send(buf, inetSocketAddress);
		System.out.println("Sent : " + message + " to " + host + ":" + port);
	}
}
