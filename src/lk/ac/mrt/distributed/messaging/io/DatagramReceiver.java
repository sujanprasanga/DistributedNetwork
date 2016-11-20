package lk.ac.mrt.distributed.messaging.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import lk.ac.mrt.distributed.messaging.DSException;

public class DatagramReceiver {

	private final ByteBuffer buf;
	private final DatagramChannel channel;
	
	public DatagramReceiver(int port){
		ConnectionProperties conectionProperties = ConnectionProperties.getInstance();
		try {
			this.channel = DatagramChannel.open();
			channel.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			throw new DSException(e);
		}
		buf = ByteBuffer.allocate(conectionProperties.getMessageBufferSize());
	}
	
	public synchronized String receive() throws IOException{
		buf.clear();
		channel.receive(buf);
		return new String(buf.array(), "UTF-8").intern();
	}
	
}
