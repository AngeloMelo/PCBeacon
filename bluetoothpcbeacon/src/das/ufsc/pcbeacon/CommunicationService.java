package das.ufsc.pcbeacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;



public class CommunicationService
{
	public static final int MSG_TYPE_MESSAGE_READ = 0;
	public static final int MSG_TYPE_REFRESH_SLAVELIST = 1;
	public static final int MSG_TYPE_STOP_DISCOVERY = 2;
	public static final int MSG_TYPE_CONNECTION_ACCEPTED = 3;
	
	public static final String NAME = "beacon";
	public static final UUID uuid = new UUID("061d375178df472c895707df79497e71", false);
	
	private WaitThread mServerThread;
	private final Manager mHandler;
	private Map<String, ReadWriteThread> pool;
		
	public CommunicationService(Manager handler) 
	{
        mHandler = handler;
        this.pool = new HashMap<>();
    }

	
	public synchronized void start() 
	{
		if(!LocalDevice.isPowerOn())
		{
			System.out.println("Bluetooth is not on");
			return;
		}
		
        // Start the thread to listen on a BluetoothServerSocket
        if (mServerThread == null) 
        {
            mServerThread = new WaitThread();
            mServerThread.start();
        }
    }
	
	
	public synchronized void restartAcceptThread()
	{
		// Start the thread to listen on a BluetoothServerSocket
		if (mServerThread != null) { mServerThread = null;}
		
        mServerThread = new WaitThread();
        mServerThread.start();
	}
	
	
	public synchronized void stop()
	{
		if (mServerThread != null) 
		{ 
			mServerThread.terminate();
			mServerThread = null;
		}
		
		for(String mac : this.pool.keySet())
		{
			this.pool.get(mac).cancel();
		}
	}
	
	
	
	public synchronized void startTransmission(StreamConnection connection) 
    {        
		try 
		{
			ReadWriteThread mReadWriteThread = new ReadWriteThread(connection);
			mReadWriteThread.start();
        
			RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
			String remoteAddress = dev.getBluetoothAddress();
			
			this.pool.put(remoteAddress, mReadWriteThread);
			mHandler.handleMessage(MSG_TYPE_CONNECTION_ACCEPTED, remoteAddress);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
    }    

	
	public void stopComunicationThread(String mac) 
	{
		mac = mac.replace(":", "");
		ReadWriteThread mReadWriteThread = this.pool.get(mac);
		if(mReadWriteThread != null) mReadWriteThread.cancel(); 
		mReadWriteThread = null;

		this.pool.remove(mac);
	}
    

	public synchronized void sendMessage(String targetMac, String msg) throws IOException
    {
		ReadWriteThread mReadWriteThread = this.pool.get(targetMac);
    	mReadWriteThread.write(msg.getBytes());
    }
	
	/**
	 * 
	 * @author angelo
	 *
	 */
	private class WaitThread extends Thread
	{
		private volatile boolean running = true;

		public void terminate() 
		{
			this.running = false;
		}
		
		@Override
		public void run() 
		{
			// retrieve the local Bluetooth device object
			LocalDevice local = null;

			StreamConnectionNotifier notifier;
			StreamConnection connection = null;

			// setup the server to listen for connection
			try 
			{
				local = LocalDevice.getLocalDevice();
				
				// setup the device to be discoverable by General/Unlimited Inquiry Access Code. 
				//All remote devices will be able to find the beacon
				local.setDiscoverable(DiscoveryAgent.GIAC);

				//setup the url of beacon service
				String url = "btspp://localhost:" + uuid.toString() + ";name=" + NAME;
				notifier = (StreamConnectionNotifier)Connector.open(url);
			} 
			catch (BluetoothStateException e) 
			{
				System.out.println("Bluetooth is not turned on.");
				e.printStackTrace();
				return;
			} 
			catch (IOException e) 
			{
				System.out.println("I/O exception has occurred");
				e.printStackTrace();
				return;
			}

			// waiting for connection
			while(this.running)
			{
				try
				{
					//waits for incoming connections 
					//acceptAndOpen will block the server until a connection request is received
					connection = notifier.acceptAndOpen();

					//monitor connected, start transmission thread
					startTransmission(connection);

				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					return;
				}
			}
		}
	}

	
	private class ReadWriteThread extends Thread 
	{
		private final StreamConnection streamConnection;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    private volatile boolean running = true;
	    private String remoteAddress;
	 
	    public ReadWriteThread(StreamConnection st) 
	    {
	        streamConnection = st;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because member streams are final
	        try 
	        {
	        	RemoteDevice dev = RemoteDevice.getRemoteDevice(st);
	        	this.remoteAddress = dev.getBluetoothAddress();
	            tmpIn = streamConnection.openInputStream();
	            tmpOut = streamConnection.openOutputStream();
	        } 
	        catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() 
	    {
	    	// buffer store for the stream
	        byte[] buffer;  

	        // Keep listening to the InputStream until canceled
	        while (this.running) 
	        {
	            try 
	            {
	            	// count the available bytes form the input stream
	            	int count = mmInStream.available();
	            	
	            	if(count > 0)
	            	{	            		
	            		buffer = new byte[count];
	            		
	            		// Read from the InputStream
	            		mmInStream.read(buffer);
	            		
	            		String cmd = new String(buffer, "UTF-8");
	            		
	            		mHandler.handleMessage(MSG_TYPE_MESSAGE_READ, cmd);
	            	}
	            } 
	            catch (IOException e) 
	            {
	            	//stop connection
	            	stopComunicationThread(remoteAddress);
	                break;
	            }
	        }
	    }
	 
	    public void write(byte[] bytes) throws IOException 
	    {
            mmOutStream.write(bytes);
	    }
	 
	    public void cancel() 
	    {
	        try 
	        { 
	        	mmInStream.close(); 
	        } 
	        catch (IOException e) 
	        { 
	        	e.printStackTrace();
	        }
	        
	        try 
	        { 
	        	mmOutStream.close(); 
	        } 
	        catch (IOException e) 
	        { 
	        	e.printStackTrace();
	        }
	        
	        try 
	        {
	            streamConnection.close();
	        } 
	        catch (IOException e) 
	        { 
	        	e.printStackTrace();
	        }
	        finally
	        {
	        	this.running = false;
	        }
	    }
	}
}