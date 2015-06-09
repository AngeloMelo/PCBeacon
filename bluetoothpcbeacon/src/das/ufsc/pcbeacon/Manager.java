package das.ufsc.pcbeacon;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import das.ufsc.pcbeacon.utils.BeaconDefaults;
import das.ufsc.pcbeacon.utils.CallInfo;

public class Manager 
{
	private CommunicationService communicationService;
	private StopInfo currentStopInfo;
	private List<StopInfo> stopInfoList;
	private Map<String, List<CallInfo>> deviceCalls;
	
	public Manager()
	{
		super();
			
		this.stopInfoList = new LinkedList<>();
		//initializes the threads for connections
		this.communicationService = new CommunicationService(this);
		
		//historic initialization, maps a mac address to a list of calls
		this.deviceCalls = new HashMap<String, List<CallInfo>>();
	}
	
	
	public void start() 
	{
		//start the thread for accepting connections
		this.communicationService.start();
	}	


	public void stop() 
	{
		//stop the thread for accepting connections
		this.communicationService.stop();
	}	

	
	public synchronized void handleMessage(int type, String msg) 
	{
		switch(type)
		{
		case CommunicationService.MSG_TYPE_CONNECTION_ACCEPTED:
		{
			sendTic(msg);
			
			break;
		}
		case CommunicationService.MSG_TYPE_MESSAGE_READ:
		{
			if(msg != null && msg.length()>0)
			{
				readAck(msg);
			}

			break;
		}
		}
	}

	
	private void sendTic(String msgRead) 
	{
		String mac = msgRead;
		//addHistoryEntry(mac, BeaconDefaults.OPP_MODE_AUTHENTIC, null, null);

		try 
		{
			int secs = new Random().nextInt(5) + 8;
			
			String msgTic = BeaconDefaults.getTicJson(secs);
			
			this.communicationService.sendMessage(mac, msgTic);
		} 
		catch (IOException e) 
		{
			showToast(e.getMessage());
			e.printStackTrace();
		}
	}



	private synchronized void addHistoryEntry(String mac, int oppMode, Date startDiscoveryTs, Date beaconFoundTs, Date firstConnectionTs, Date lastAcceptedConnectionTs) 
	{
		Date timeStamp = new Date();
		CallInfo hInfo = new CallInfo(timeStamp, oppMode, mac, this.currentStopInfo.getStopName(), this.currentStopInfo.getStopId(), this.currentStopInfo.getStopTs(), startDiscoveryTs, beaconFoundTs, firstConnectionTs, lastAcceptedConnectionTs);

		if(this.deviceCalls.containsKey(mac))
		{
			//get the stored list
			List<CallInfo> privateList = deviceCalls.get(mac);
			privateList.add(hInfo);
		}
		else
		{
			//add a new list
			List<CallInfo> privateList  = new ArrayList<>();
			privateList.add(hInfo);
			
			this.deviceCalls.put(mac, privateList);
		}
		System.out.println("Total current (a/d): " + getTotalOnBoard() + "/" + getTotalDubious());
		//prints the new entry
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		//showToast("[" + mac + "] in at " + dateFormat.format(timeStamp) + " opp mode: " + oppMode);
	}


	/**
	 * receives a message on format {MAC:'mac',OPP_MODE:0, ack:true}
	 * @param msgRead
	 */
	private void readAck(String msgRead) 
	{	
		//System.out.println("onReadAck: [" +msgRead +"]");
		
		try
		{
			JSONObject json = new JSONObject(msgRead);
			if(json.has(BeaconDefaults.ACK_KEY))
			{
				int oppMode = BeaconDefaults.getOppMode(msgRead);
				
				String remoteMac = json.getString(BeaconDefaults.MAC_KEY);

				//get performance infos
				Date startDiscoveryTs = BeaconDefaults.getStartDiscoveryTs(json);
				Date beaconFoundTs = BeaconDefaults.getBeaconFoundTs(json);
				Date firstConnectionTs = BeaconDefaults.getFirstConnectionTs(json);
				Date lastAcceptedConnectionTs = BeaconDefaults.getLastAcceptedConnectionTs(json);

				addHistoryEntry(remoteMac, oppMode, startDiscoveryTs, beaconFoundTs, firstConnectionTs, lastAcceptedConnectionTs);
				
				this.communicationService.stopComunicationThread(remoteMac);
			}		
		}
		catch(Exception e)
		{
			
		}
	}
	
	
	private void showToast(String string) 
	{
		System.out.println(string);
	}


	public synchronized void setCurrentStopInfo(StopInfo stopInfo) 
	{
		this.currentStopInfo = stopInfo;
		this.currentStopInfo.setStopTs(new Date());
		this.stopInfoList.add(this.currentStopInfo);
		
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("On Stop: " + this.currentStopInfo.getStopName() +"(" + this.currentStopInfo.getStopId() + ") at " + this.currentStopInfo.getStopTs());
	}

	
	private int getTotalOnBoard() 
	{
		return getTotalCurrentByMode(BeaconDefaults.OPP_MODE_AUTHENTIC);
	}
	
	
	private int getTotalDubious() 
	{
		return getTotalCurrentByMode(BeaconDefaults.OPP_MODE_DUBIOUS);
	}
	
	
	private synchronized int getTotalCurrentByMode(int mode) 
	{
		int sum = 0;
		for(String mac : this.deviceCalls.keySet())
		{
			List<CallInfo> callsList = deviceCalls.get(mac);
			boolean sameMode = true;
			boolean hasOne = false;
			for(CallInfo callInfo: callsList)
			{
				//select only for this stop
				if(callInfo.getCurrentStopId() == this.currentStopInfo.getStopId())
				{
					hasOne = true;
					//sums only if all calls have the same mode
					if(callInfo.getOppMode() != mode)
					{
						sameMode = false;
						break;
					}					
				}
			}
			if(sameMode && hasOne)
			{
				sum++;
			}
		}
		return sum;
	}
	
	
	public void printTotalBySegmentsReport()
	{
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf("%-7s \t %-30s \t %-8s \t %s \n", "Stop ID", "Stop", "Hour", "Total on Board");
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		for(StopInfo stopInfo : this.stopInfoList)
		{
			int totalOnSegment = getTotalOnSegment(stopInfo.getStopId());
			System.out.printf("%-7s \t %-30s \t %-8s \t %d \n", stopInfo.getStopId(), stopInfo.getStopName(), sdf.format(stopInfo.getStopTs()), totalOnSegment);
		}
	}
	
	
	private int getTotalOnSegment(int stopId) 
	{
		int sum = 0;
		for(String mac : this.deviceCalls.keySet())
		{
			List<CallInfo> callsList = this.deviceCalls.get(mac);
			boolean isAuthentic = true;
			boolean found = false;
			for(CallInfo callInfo: callsList)
			{
				if(callInfo.getCurrentStopId() == stopId)
				{
					found = true;
					if(callInfo.getOppMode() != BeaconDefaults.OPP_MODE_AUTHENTIC)
					{
						isAuthentic = false;
						break;
					}					
				}
			}
			if(found && isAuthentic)
			{
				sum ++;
			}
		}
		return sum;
	}


	public void printODReport()
	{
		//prints the header
		Object []arr = new Object[this.stopInfoList.size()+1];
		String formatter = "%-SIZEs ";
		int maxLength = 0;
		int i = 1;
		arr[0] = "Destinos: ";
		for(StopInfo stopInfo : this.stopInfoList)
		{
			if(stopInfo.getStopName().length() > maxLength)
			{
				maxLength = stopInfo.getStopName().length();	
			}
			arr[i] = stopInfo.getStopName();
			
			i++;
			
			formatter = formatter + "%-SIZEs ";
		}
		
		formatter = formatter.replaceAll("SIZE", maxLength + "");
		
		formatter = formatter + "\n";
		
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf(formatter, arr);
		
		
		
		for(StopInfo stopInfo : this.stopInfoList)
		{
			arr[0] = stopInfo.getStopName();
			int lineIdx = 1;
			
			formatter = "%-" + maxLength + "s ";
			for(StopInfo stopInfoLine : this.stopInfoList)
			{	
				arr[lineIdx] = getTotalTrip(stopInfo, stopInfoLine);
				
				formatter = formatter + "%-" + maxLength + "s ";
				lineIdx++;
			}
			formatter = formatter + "\n";
			System.out.printf(formatter, arr);
		}
	}

	
	private int getTotalTrip(StopInfo stopInfoOrign, StopInfo stopInfoDestination) 
	{
		if(stopInfoOrign.getStopId() >= stopInfoDestination.getStopId()) return 0;
		int sum = 0;
		
		List<String> orignPop = getBoardingOnList(stopInfoOrign);	
		
		
		//checks if aligted on destination
		int lastIdx = this.stopInfoList.indexOf(stopInfoDestination);
		if(lastIdx + 1 == this.stopInfoList.size())
		{
			//final stop
			List<String> destinPop = getMacList(stopInfoDestination.getStopId());	
			for(String mac : orignPop)
			{
				if(destinPop.contains(mac))
				{
					sum++;
				}
			}
		}
		else
		{
			//checks the next of destination
			StopInfo next = this.stopInfoList.get(lastIdx + 1); 
			
			List<String> destinPop = getMacList(stopInfoDestination.getStopId());	
			List<String> nextPop = getMacList(next.getStopId());	
			
			for(String mac : orignPop)
			{
				if(destinPop.contains(mac) && !nextPop.contains(mac))
				{
					sum++;
				}
			}
		}
		StopInfo last = this.stopInfoList.get(lastIdx);
		if(last != null)
		{
			
		}
		
		return sum;
	}


	private List<String> getBoardingOnList(StopInfo stopInfoOrign) 
	{
		List<String> result = new ArrayList<>();
		
		//checks if its the first stop point
		int orignIndex = this.stopInfoList.indexOf(stopInfoOrign);
		if(orignIndex == 0)
		{
			result = getMacList(stopInfoOrign.getStopId());
		}
		else
		{
			//it is a intermediary stop point, checks the predecessor:
			StopInfo predecessor = this.stopInfoList.get(orignIndex - 1);
			List<String> predecessorMacList = getMacList(predecessor.getStopId());
			List<String> orignMacList = getMacList(stopInfoOrign.getStopId());
			for(String mac : this.deviceCalls.keySet())
			{
				if(orignMacList.contains(mac) && !predecessorMacList.contains(mac))
				{
					result.add(mac);
				}
			}
		}

		return result;
	}


	private List<String> getMacList(int stopId) 
	{
		List<String> result = new ArrayList<>();
		
		for(String mac : this.deviceCalls.keySet())
		{
			List<CallInfo> callsList = this.deviceCalls.get(mac);
			//boolean isAuthentic = true;
			boolean found = false;
			for(CallInfo callInfo: callsList)
			{
				if(callInfo.getCurrentStopId() == stopId)
				{
					found = true;
					/*if(callInfo.getOppMode() != BeaconDefaults.OPP_MODE_AUTHENTIC)
					{
						isAuthentic = false;
						break;
					}*/					
				}
			}
			if(found /*&& isAuthentic*/)
			{
				result.add(mac);
			}
		}

		return result;
	}


	public void printPerformanceReport()
	{
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf("%-20s \t %-20s \t %-20s \t %-20s \n", "Discovery AVG Time", "First conn AVG Time", "Connections AVG Time", "Total on Board");
		
		int totalOnBoard = 0;
		long discoveryTimeSum = 0;
		long firstConnTimeSum = 0;
		long connectionAvgSum = 0;
		for(String mac : this.deviceCalls.keySet())
		{
			totalOnBoard++;
			
			List<CallInfo> callInfos = this.deviceCalls.get(mac);
			
			CallInfo firstCall = callInfos.get(0);
			
			long discoveryTime = firstCall.getBeaconFoundTs().getTime() - firstCall.getStartDiscoveryTs().getTime();
			discoveryTimeSum =+ discoveryTime;
			
			long firstCallTime = firstCall.getFirstConnectionTs().getTime() - firstCall.getBeaconFoundTs().getTime();
			firstConnTimeSum =+ firstCallTime;
			
			long connectionsTimeSum = 0;
			int callSum = 0;
			for(CallInfo callInfo : callInfos)
			{
				long connectionsTime = callInfo.getTimeStamp().getTime() - callInfo.getLastAcceptedConnectionTs().getTime();
				connectionsTimeSum =+ connectionsTime;
				callSum++;
			}
			long connectionAvg = connectionsTimeSum / callSum;
			connectionAvgSum =+ connectionAvg;
		}

		long discoveryTimeAvg = discoveryTimeSum/totalOnBoard;
		long firstConnTimeAvg = firstConnTimeSum/totalOnBoard;
		long connectionAvgAvg = connectionAvgSum/totalOnBoard;
		
		System.out.printf("%-20s \t %-20s \t %-20s \t %-20s \n", discoveryTimeAvg, firstConnTimeAvg, connectionAvgAvg, totalOnBoard);
	}

}
