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

public class Manager 
{
	private CommunicationService communicationService;
	private TripSegmentInfo currentTripSegmentInfo;
	private List<TripSegmentInfo> tripSegmentList;
	private Map<String, CallHistoric> deviceCalls;
	private LineInfo lineInfo;
	
	public Manager()
	{
		super();
			
		this.tripSegmentList = new LinkedList<>();
		//initializes the threads for connections
		this.communicationService = new CommunicationService(this);
		
		//historic initialization, maps a mac address to a list of calls
		this.deviceCalls = new HashMap<String, CallHistoric>();
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
	
	
	public synchronized boolean isBluetoothReady()
	{
		return this.communicationService.isBluetoothReady();
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
			int secs = new Random().nextInt(5) + 5;
			
			int lineId = this.lineInfo.getLineId();
			String lineName = this.lineInfo.getLineNm();
			String lastStop = "Next stop: " + this.currentTripSegmentInfo.getTripSegmentDestination();
			if(this.currentTripSegmentInfo.isStop())
			{
				lastStop = this.currentTripSegmentInfo.getTripSegmentOrign();
			}
			String msgTic = BeaconDefaults.getTicJson(secs, lineId, lineName, lastStop);
			
			this.communicationService.sendMessage(mac, msgTic);
		} 
		catch (IOException e) 
		{
			showToast(e.getMessage());
			e.printStackTrace();
		}
	}


	private void addHistoryEntry(String mac, int oppMode,
			Date startDiscoveryTs, Date beaconFoundTs,
			Date firstConnectionAcceptanceTs, Date lastConnectionRequestTs,
			Date lastConnectionAcceptanceTs, Date lastTicTs, Date lastAckSentTs)
	{
		Date timeStamp = new Date();
		
		CallInfo callInfo = new CallInfo();
		callInfo.setCallTimeStamp(timeStamp);
		callInfo.setCurrentTripSegmentId(this.currentTripSegmentInfo.getTripSegmentId());
		callInfo.setCurrentTripSegmentOrign(this.currentTripSegmentInfo.getTripSegmentOrign());
		callInfo.setCurrentTripSegmentDestination(this.currentTripSegmentInfo.getTripSegmentDestination());
		callInfo.setCurrentTripSegmentTs(this.currentTripSegmentInfo.getTripSegmentStartTs());
		callInfo.setOppMode(oppMode);
		callInfo.setLastConnectionRequestTs(lastConnectionRequestTs);
		callInfo.setLastConnectionAcceptanceTs(lastConnectionAcceptanceTs);
		callInfo.setLastTicTs(lastTicTs);
		callInfo.setLastAckSentTs(lastAckSentTs);
		
		if(this.deviceCalls.containsKey(mac))
		{
			//get the stored list
			CallHistoric callHistoric = deviceCalls.get(mac);
			callHistoric.getCalls().add(callInfo);
		}
		else
		{
			//add a new 
			CallHistoric callHistoric = new CallHistoric();
			callHistoric.setMac(mac);
			callHistoric.setStartDiscoveryTs(startDiscoveryTs);
			callHistoric.setBeaconFoundTs(beaconFoundTs);
			callHistoric.setFirstConnectionAcceptanceTs(firstConnectionAcceptanceTs);
			
			callHistoric.getCalls().add(callInfo);
			
			this.deviceCalls.put(mac, callHistoric);
		}
		System.out.println("Total current (a/d): " + getTotalOnBoard() + "/" + getTotalDubious());
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
				String remoteMac = json.getString(BeaconDefaults.MAC_KEY);
				
				//register only if not on a stop
				if(!this.currentTripSegmentInfo.isStop())
				{
					//get performance infos
					Date startDiscoveryTs = BeaconDefaults.getStartDiscoveryTs(json);
					Date beaconFoundTs = BeaconDefaults.getBeaconFoundTs(json);
					Date firstConnectionAcceptanceTs = BeaconDefaults.getFirstConnectionAcceptanceTs(json);
					Date lastConnectionRequestTs = BeaconDefaults.getLastConnectionRequestTs(json);
					Date lastConnectionAcceptanceTs = BeaconDefaults.getLastConnectionAcceptanceTs(json);
					Date lastTicTs = BeaconDefaults.getLastTicReceivedTs(json);
					Date lastAckSentTs = BeaconDefaults.getLastAckSentTs(json);
					
					int oppMode = BeaconDefaults.getOppMode(msgRead);
					addHistoryEntry(remoteMac, oppMode, startDiscoveryTs, beaconFoundTs, firstConnectionAcceptanceTs, lastConnectionRequestTs, lastConnectionAcceptanceTs, lastTicTs, lastAckSentTs);
					validateHistoric(remoteMac, oppMode);
				}
				
				this.communicationService.stopComunicationThread(remoteMac);
			}		
		}
		catch(Exception e)
		{
			
		}
	}


	private void validateHistoric(String remoteMac, int oppMode) 
	{
		if(oppMode == BeaconDefaults.OPP_MODE_AUTHENTIC)
		{
			//valid the registers marked as dubious
			List<CallInfo> calls = deviceCalls.get(remoteMac).getCalls();
			
			for(CallInfo callInfo : calls)
			{
				if(callInfo.getOppMode() == BeaconDefaults.OPP_MODE_DUBIOUS)
				{
					callInfo.setOppMode(BeaconDefaults.OPP_MODE_AUTHENTIC);
				}
			}
		}
	}
	
	
	public void removeDubious() 
	{
		List<String> remove = new ArrayList<>();
		for(String remoteMac: this.deviceCalls.keySet())
		{	
			CallHistoric callHistoric = deviceCalls.get(remoteMac);
			
			LinkedList<CallInfo> calls = (LinkedList<CallInfo>) callHistoric.getCalls();
			if(calls.getLast().getOppMode() == BeaconDefaults.OPP_MODE_DUBIOUS)
			{
				//last call was as dubious, remove register
				remove.add(remoteMac);
			}
		}
		//remove dubious
		for(String mac : remove)
		{
			this.deviceCalls.remove(mac);
		}
	}


	private void showToast(String string) 
	{
		System.out.println(string);
	}


	public synchronized void setCurrentTripSegmentInfo(TripSegmentInfo tripSegmentInfo) 
	{
		this.currentTripSegmentInfo = tripSegmentInfo;
		this.currentTripSegmentInfo.setTripSegmentStartTs(new Date());
		String stopTs = new SimpleDateFormat("HH:mm:ss").format(this.currentTripSegmentInfo.getTripSegmentStartTs());
		
		System.out.println("-------------------------------------------------------------------------------------");
		if(this.currentTripSegmentInfo.isStop())
		{
			System.out.print("\t");
			System.out.println(this.currentTripSegmentInfo.getTripSegmentOrign() +"(" + this.currentTripSegmentInfo.getTripSegmentId() + ") at " + stopTs);
		}
		else
		{
			this.tripSegmentList.add(this.currentTripSegmentInfo);
			System.out.println("Next stop: " + this.currentTripSegmentInfo.getTripSegmentDestination() +"(" + this.currentTripSegmentInfo.getTripSegmentId() + ") at " + stopTs);
		}
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
		if(this.currentTripSegmentInfo.isStop()) return sum;
		
		for(String mac : this.deviceCalls.keySet())
		{
			List<CallInfo> callsList = deviceCalls.get(mac).getCalls();
			boolean sameMode = true;
			boolean hasOne = false;
			for(CallInfo callInfo: callsList)
			{
				//select only for this stop
				if(callInfo.getCurrentTripSegmentId() == this.currentTripSegmentInfo.getTripSegmentId())
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
		System.out.printf("%-5s \t %-30s \t %-8s \t %s \n", "Seg ID", "Segment", "Hour", "Total on Board");
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		for(TripSegmentInfo tripSegmentInfo : this.tripSegmentList)
		{
			int totalOnSegment = getTotalOnSegment(tripSegmentInfo.getTripSegmentId());
			String segmentDescription = tripSegmentInfo.getTripSegmentOrign() + " to " + tripSegmentInfo.getTripSegmentDestination();
			System.out.printf("%-5s \t %-30s \t %-8s \t %d \n", tripSegmentInfo.getTripSegmentId(), segmentDescription, sdf.format(tripSegmentInfo.getTripSegmentStartTs()), totalOnSegment);
		}
	}
	
	
	private int getTotalOnSegment(int tripSegmentId) 
	{
		int sum = 0;
		for(String mac : this.deviceCalls.keySet())
		{
			int originId = getOriginSegmentId(mac);
			int destinId = getDestinSegmentId(mac);
			
			if(tripSegmentId >= originId && tripSegmentId <= destinId)
			{
				sum ++;
			}
		}
		return sum;
	}


	private int getOriginSegmentId(String mac) 
	{
		int result = 1000000;
		
		CallHistoric callHistoric = this.deviceCalls.get(mac);
		for(CallInfo callInfo : callHistoric.getCalls())
		{
			if(callInfo.getCurrentTripSegmentId() < result)
			{
				result = callInfo.getCurrentTripSegmentId();
			}
		}
		return result;
	}
	
	
	private int getDestinSegmentId(String mac) 
	{
		int result = -1;
		
		CallHistoric callHistoric = this.deviceCalls.get(mac);
		for(CallInfo callInfo : callHistoric.getCalls())
		{
			if(callInfo.getCurrentTripSegmentId() > result)
			{
				result = callInfo.getCurrentTripSegmentId();
			}
		}
		return result;
	}


	public void printODReport()
	{
		//prints the header
		Object []arr = new Object[this.tripSegmentList.size()+1];
		String formatter = "%-SIZEs ";
		int maxLength = 0;
		int i = 1;
		arr[0] = "Destinos: ";
		for(TripSegmentInfo tripSegmentInfo : this.tripSegmentList)
		{
			if(tripSegmentInfo.getTripSegmentDestination().length() > maxLength)
			{
				maxLength = tripSegmentInfo.getTripSegmentDestination().length();	
			}
			arr[i] = tripSegmentInfo.getTripSegmentDestination();
			
			i++;
			
			formatter = formatter + "%-SIZEs ";
		}
		
		formatter = formatter.replaceAll("SIZE", maxLength + "");
		
		formatter = formatter + "\n";
		
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf(formatter, arr);
		
		//print the body		
		for(TripSegmentInfo tripSegmentOrign : this.tripSegmentList)
		{
			arr[0] = tripSegmentOrign.getTripSegmentOrign();
			int lineIdx = 1;
			
			formatter = "%-" + maxLength + "s ";
			for(TripSegmentInfo tripSegmentDestin : this.tripSegmentList)
			{	
				arr[lineIdx] = getTotalTrip(tripSegmentOrign, tripSegmentDestin);
				
				formatter = formatter + "%-" + maxLength + "s ";
				lineIdx++;
			}
			formatter = formatter + "\n";
			System.out.printf(formatter, arr);
		}
	}

	
	private int getTotalTrip(TripSegmentInfo origin, TripSegmentInfo destination) 
	{
		//descarta trechos percorridos antes do destino
		if(origin.getTripSegmentId() >= destination.getTripSegmentId()) return 0;
		
		int totalTrip = 0;
		
		//lista de passageiros que embarcaram em origin
		List<String> originPassengers = getBoardingOnList(origin);
		
		//lista de passageiros que foram ateh destination
		List<String> destinPassengers = getAlightedOnList(destination);
		
		for(String mac : originPassengers)
		{
			if(destinPassengers.contains(mac))
			{
				totalTrip++;
			}
		}
		
		return totalTrip;
	}


	/**
	 * Returns a list of every passenger embarked on origin
	 * @param orign
	 * @return
	 */
	private List<String> getBoardingOnList(TripSegmentInfo origin) 
	{
		List<String> result = new ArrayList<>();
		
		for(String mac : this.deviceCalls.keySet())
		{
			if(getOriginSegmentId(mac) == origin.getTripSegmentId())
			{
				result.add(mac);
			}
		}

		return result;
	}
	
	


	/**
	 * Returns a list of every passenger on board til this point
	 * @param orign
	 * @return
	 */
	private List<String> getAlightedOnList(TripSegmentInfo destin) 
	{
		List<String> result = new ArrayList<>();
		
		for(String mac : this.deviceCalls.keySet())
		{
			if(getDestinSegmentId(mac) == destin.getTripSegmentId())
			{
				result.add(mac);
			}
		}

		return result;
	}



	public void printPerformanceReport()
	{
		System.out.println("-------------------------------------------------------------------------------------");
		
		int totalOnBoard = 0;
		long discoveryTimeSum = 0;
		long firstConnTimeSum = 0;
		long connectionAcceptanceTimeSum = 0;
		long connectionAcceptanceTimeMin = 1000000000;
		long connectionAcceptanceTimeMax = 0;
		long protocolTimeSum = 0;
		long protocolTimeMin = 1000000000;
		long protocolTimeMax = 0;
		int totalCall = 0;
		
		for(String mac : this.deviceCalls.keySet())
		{
			totalOnBoard++;
			
			CallHistoric callHistoric = this.deviceCalls.get(mac);
			
			long discoveryTime = callHistoric.getBeaconFoundTs().getTime() - callHistoric.getStartDiscoveryTs().getTime();
			discoveryTimeSum =+ discoveryTime;
			
			long firstCallTime = callHistoric.getFirstConnectionAcceptanceTs().getTime() - callHistoric.getBeaconFoundTs().getTime();
			
			firstConnTimeSum =+ firstCallTime;
			
			for(CallInfo callInfo : callHistoric.getCalls())
			{
				totalCall++;
				///verificar null em callInfo.getLastConnectionRequestTs()
				long connAcceptanceTime = callInfo.getLastConnectionAcceptanceTs().getTime() - callInfo.getLastConnectionRequestTs().getTime();
				connectionAcceptanceTimeSum =+ connAcceptanceTime;
				
				if(connAcceptanceTime < connectionAcceptanceTimeMin)
				{
					connectionAcceptanceTimeMin = connAcceptanceTime;
				}
				
				if(connAcceptanceTime > connectionAcceptanceTimeMax)
				{
					connectionAcceptanceTimeMax = connAcceptanceTime;
				}
				
				long protocolTime = callInfo.getLastAckSentTs().getTime() - callInfo.getLastConnectionAcceptanceTs().getTime();
				protocolTimeSum += protocolTime;
				
				if(protocolTime < protocolTimeMin)
				{
					protocolTimeMin = protocolTime;
				}
				
				if(protocolTime > protocolTimeMax)
				{
					protocolTimeMax = protocolTime;
				}

			}
		}

		long discoveryTimeAvg = 0;
		long firstConnTimeAvg = 0;
		long connectionAcceptanceTimeAvg = 0;
		long protocolTimeAvg = 0;
		
		if(totalOnBoard > 0)
		{
			discoveryTimeAvg = discoveryTimeSum/totalOnBoard;
			firstConnTimeAvg = firstConnTimeSum/totalOnBoard;
		}
		
		if(totalCall > 0)
		{
			connectionAcceptanceTimeAvg = connectionAcceptanceTimeSum/totalCall;
			protocolTimeAvg  = protocolTimeSum/totalCall;
		}
		
		System.out.println("Discovery avg time: " + discoveryTimeAvg + " ms");
		System.out.println("First connection acceptance avg time: " + firstConnTimeAvg + " ms");
		
		System.out.println("Connection acceptance time: " + connectionAcceptanceTimeAvg + " (avg) " + connectionAcceptanceTimeMin + " (min) " + connectionAcceptanceTimeMax + "(max)");
		System.out.println("Protocol execution time:" +  protocolTimeAvg + " (avg) " + protocolTimeMin + " (min) " + protocolTimeMax + " (max)");
		System.out.println("Total of passengers:" +  totalOnBoard);
		System.out.println("Total of calls:" +  totalCall);
	}


	public void setLineInfo(LineInfo lineInfo) 
	{
		this.lineInfo = lineInfo;
	}
}
