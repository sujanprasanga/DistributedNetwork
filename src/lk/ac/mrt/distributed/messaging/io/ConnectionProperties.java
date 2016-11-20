package lk.ac.mrt.distributed.messaging.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public final class ConnectionProperties {

	private final static Properties properties = new Properties();
	
	public static ConnectionProperties getInstance(){
		try	{
			FileInputStream fis = new FileInputStream("Connection.properties");
			properties.load(fis);
			fis.close();
		}catch(Exception e){}
		return new ConnectionProperties();
	}
	
	public int getMessageBufferSize()
	{
		return 1024;
	}

	public int getBSPort() 
	{
		return Integer.parseInt(properties.getProperty("BSPORT"));
	}

	public String getBSHost()
	{
		return properties.getProperty("BSHOST");
	}

	public String getEncoding() {
		return "UTF-8";
	}

	public int getMaxPeerCount() {
		return 2;
	}

	public int getSuperPeerFindTimeout() {
		return 5;
	}

	public int getPeerRefreshInterval() {
		return 10;
	}

	public int secondsToWaitBeforeRemovingPeer() {
		return 20;
	}

	public int getSuperPeerDiscoveryInterval() {
		return getSuperPeerFindTimeout();
	}

	public int maxHops() {
		return 3;
	}

	public int getSuperPeerDivisionRate() {
		return 5;
	}

	public int getSuperPeerSyncRate() {
		return 10;
	}
	
	
}
