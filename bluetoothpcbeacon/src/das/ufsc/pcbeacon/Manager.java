package das.ufsc.pcbeacon;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Manager 
{
	private CommunicationService communicationService;
	
	public Manager()
	{
		super();
		
		//obtem a interface para o hardware bluetooth do dispositivo
		this.communicationService = new CommunicationService(this);
	}
	
	
	public void start() 
	{
		this.communicationService.start();
	}	

	
	public void handleMessage(int type, String content) 
	{
		switch(type)
		{
		case CommunicationService.MSG_TYPE_MESSAGE_READ:
		{
			readAck(content);

			break;
		}
		case CommunicationService.MSG_TYPE_CONNECTION_ACCEPTED:
		{
			sendTic(content);
			
			break;
		}
		}
	}

	
	private void sendTic(String mac) 
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		showToast("[" + mac + "] in at " + dateFormat.format(new Date()));

		try 
		{
			int secs = new Random().nextInt(5) + 8;
			String msg = "tic:" + secs;
			this.communicationService.sendMessage(mac, msg);
		} 
		catch (IOException e) 
		{
			showToast(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void readAck(String readMessage) 
	{
		if(readMessage.contains("ack"))
		{
			String remoteMac = readMessage.replace(":ack", "");

			communicationService.stopComunicationThread(remoteMac);
		}
	}
	
	
	private void showToast(String string) 
	{
		System.out.println(string);
	}
	
}
