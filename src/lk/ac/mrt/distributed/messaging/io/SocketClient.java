package lk.ac.mrt.distributed.messaging.io;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lk.ac.mrt.distributed.messaging.DSException;

public class SocketClient
{
	private final ConnectionProperties connectionProperties;
	
	public SocketClient(ConnectionProperties connectionProperties)
	{
		this.connectionProperties = connectionProperties;
	}
	
	public String callBootStrap(String msg)
	{
		InetSocketAddress hostAddress = new InetSocketAddress(connectionProperties.getBSHost(), connectionProperties.getBSPort());
		try
		(
			SocketChannel client = SocketChannel.open(hostAddress);
		)
		{
			System.out.println("sending: " + msg);
			ByteBuffer writeBuffer = ByteBuffer.wrap(msg.getBytes(connectionProperties.getEncoding()));
			client.write(writeBuffer);
			writeBuffer.clear();
			ByteBuffer readBuffer = ByteBuffer.allocate(connectionProperties.getMessageBufferSize());
			client.read(readBuffer);
			String recieved = new String(readBuffer.array(), connectionProperties.getEncoding());
			return recieved;
		}
		catch(Exception e)
		{
			throw new DSException(e);
		}
	}
}
