package das.ufsc.pcbeacon;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.json.JSONObject;

import das.ufsc.pcbeacon.utils.HistoricInfo;

public class Manager 
{
	public static final String MAC_KEY = "MAC";
	public static final String OPP_MODE_KEY = "OPP_MODE";
	public static final int OPP_MODE_AUTHENTIC = 0;
	public static final int OPP_MODE_DUBIOUS = 1;
	
	private CommunicationService communicationService;
	private Map<String, List<HistoricInfo>> historic;
	
	public Manager()
	{
		super();
		
		//initializes the historic
		this.historic = new HashMap<String, List<HistoricInfo>>();
		
		//initializes the threads for connections
		this.communicationService = new CommunicationService(this);
	}
	
	
	public void start() 
	{
		//start the thread for accepting connections
		this.communicationService.start();
	}	

	
	public void handleMessage(int type, String msg) 
	{
		switch(type)
		{
		case CommunicationService.MSG_TYPE_MESSAGE_READ:
		{
			readAck(msg);

			break;
		}
		case CommunicationService.MSG_TYPE_CONNECTION_ACCEPTED:
		{
			sendTic(msg);
			
			break;
		}
		}
	}

	
	private void sendTic(String msgRead) 
	{
		int oppMode = getOppMode(msgRead);
		String mac = getMac(msgRead);
		addHistoryEntry(mac, oppMode);	

		try 
		{
			int secs = new Random().nextInt(5) + 8;
			String msg = "tic:" + secs;
			this.communicationService.sendMessage(msgRead, msg);
		} 
		catch (IOException e) 
		{
			showToast(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param msgRead message on format {MAC:'mac',OPP_MODE:'oppMode'}
	 * @return
	 */
	private String getMac(String msgRead) 
	{
		JSONObject json = new JSONObject(msgRead);
		String mac = json.getString(MAC_KEY);
		
		return mac;
	}


	/**
	 * 
	 * @param msgRead message on format {MAC:'mac',OPP_MODE:'oppMode'}
	 * @return
	 */
	private int getOppMode(String msgRead)
	{
		//the most of cases the operation mode is authentic
		int oppMode = OPP_MODE_AUTHENTIC;
		
		JSONObject json = new JSONObject(msgRead);
		if(json.has(OPP_MODE_KEY))
		{
			oppMode = json.getInt(OPP_MODE_KEY);
		}
		
		return oppMode;
	}


	private void addHistoryEntry(String mac, int oppMode) 
	{
		Date timeStamp = new Date();
		HistoricInfo hInfo = new HistoricInfo(timeStamp, oppMode, mac);

		if(this.historic.containsKey(mac))
		{
			//get the stored list
			List<HistoricInfo> privateList = this.historic.get(mac);
			privateList.add(hInfo);
		}
		else
		{
			//add a new list
			List<HistoricInfo> privateList  = new ArrayList<>();
			privateList.add(hInfo);
			
			this.historic.put(mac, privateList);
		}

		//prints the new entry
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		showToast("[" + mac + "] in at " + dateFormat.format(timeStamp) + " opp mode: " + oppMode);
	}


	//TODO
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
