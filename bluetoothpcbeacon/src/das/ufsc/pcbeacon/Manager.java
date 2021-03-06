package das.ufsc.pcbeacon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import das.ufsc.pcbeacon.utils.BeaconDefaults;

public class Manager 
{
	private CommunicationService communicationService;
	private TripSegmentInfo currentTripSegmentInfo;
	private List<TripSegmentInfo> tripSegmentList;
	private Map<String, CallHistoric> deviceCalls;
	private LineInfo lineInfo;
	private int ticTimeMin;
	private int ticTimeOffset;
	private int threadDelay;
	private Main gui;
	
	public Manager(Main gui)
	{
		super();
		
		this.gui = gui;
		this.tripSegmentList = new LinkedList<>();
		
		//initializes the threads for connections
		this.communicationService = new CommunicationService(this);
		
		//historic initialization, maps a mac address to a list of calls
		this.deviceCalls = new HashMap<String, CallHistoric>();
	}

	
	public void setTicTimeMin(int value){ this.ticTimeMin = value; };
	private int getTicTimeMin() { return this.ticTimeMin; }
	

	public void setDelay(int delay) { this.threadDelay = delay; }
	public int getDelay() { return this.threadDelay; }


	public void setTicTimeOffset(int value){ this.ticTimeOffset = value; };
	private int getTicTimeOffset() { return this.ticTimeOffset; }

	
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
	
	/*
	private synchronized void handleMessage(int type, String msg) 
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
	}*/

	
	public String getTicMessage(String remoteMac)
	{
		int secs = getTicTimeMin() + new Random().nextInt(getTicTimeOffset());
		if(this.currentTripSegmentInfo.isLast())
		{
			secs = BeaconDefaults.INT_NO_RECALL;
		}
		
		int lineId = this.lineInfo.getLineId();
		String lineName = this.lineInfo.getLineNm();
		String lastStop = "Next stop: " + this.currentTripSegmentInfo.getTripSegmentDestination();
		if(this.currentTripSegmentInfo.isStop())
		{
			lastStop = this.currentTripSegmentInfo.getTripSegmentOrign();
		}
		return BeaconDefaults.getTicJson(secs, lineId, lineName, lastStop);
	}
	
	/*
	private void sendTic(String msgRead) 
	{
		String mac = msgRead;
		//addHistoryEntry(mac, BeaconDefaults.OPP_MODE_AUTHENTIC, null, null);

		try 
		{
			int secs = getTicTimeMin() + new Random().nextInt(getTicTimeOffset());
			
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
	}*/

	private void addHistoryEntry(String mac, int oppMode,
			Date startDiscoveryTs, Date beaconFoundTs,
			Date firstConnectionAcceptanceTs, Date lastConnectionRequestTs, Date lastAuthenticConnectionRequestTs, 
			Date lastConnectionAcceptanceTs, Date lastTicTs, Date lastAckSentTs, int missedCalls)
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
		callInfo.setLastAuthenticConnectionRequestTs(lastAuthenticConnectionRequestTs);
		callInfo.setLastConnectionAcceptanceTs(lastConnectionAcceptanceTs);
		callInfo.setLastTicTs(lastTicTs);
		callInfo.setLastAckSentTs(lastAckSentTs);
		callInfo.setMissedCalls(missedCalls);
		
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
		gui.printTotalOnBoard(" " + getTotalOnBoard() + " (" + getTotalDubious() + " dubious)");
	}


	/**
	 * receives a message on format {MAC:'mac',OPP_MODE:0, ack:true}
	 * @param msgRead
	 */
	/*
	private void readAck(String msgRead) 
	{	
		//gui.println("onReadAck: [" +msgRead +"]");
		
		try
		{
			JSONObject json = new JSONObject(msgRead);
			if(json.has(BeaconDefaults.ACK_KEY))
			{				
				String remoteMac = json.getString(BeaconDefaults.MAC_KEY);
				
				remoteMac = remoteMac.replace(":", "");
				
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
				this.communicationService.sendMessage(remoteMac, "{TIC:-1}");
				this.communicationService.stopComunicationThread(remoteMac);
			}		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
*/

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
			gui.printStop(this.currentTripSegmentInfo.getTripSegmentOrign());
		}
		else
		{
			this.tripSegmentList.add(this.currentTripSegmentInfo);
			System.out.println("Next stop: " + this.currentTripSegmentInfo.getTripSegmentDestination() +"(" + this.currentTripSegmentInfo.getTripSegmentId() + ") at " + stopTs);
			gui.printLocation(" going to " + this.currentTripSegmentInfo.getTripSegmentDestination());
			//System.out.print("Num of threads current: ");
			//Thread.currentThread().getThreadGroup().list();
		}
		gui.printTotalOnBoard("0");
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
	
	
	public void setLineInfo(LineInfo lineInfo) 
	{
		this.lineInfo = lineInfo;
	}


	public void printReports() 
	{
		EmulationReportPrinter printer = new EmulationReportPrinter(this.deviceCalls, this.tripSegmentList);
		
		printer.printTotalBySegmentsReport();
		printer.printODReport();
		printer.printPerformanceReport();
		
		printer.writeDiscoveryReport();
		printer.writeCallsReport();
	}


	public boolean onStop() 
	{
		return this.currentTripSegmentInfo.isStop();
	}
	
	
	public void registerCall(String remoteMac, int oppMode,
			Date startDiscoveryTs, Date beaconFoundTs,
			Date firstConnectionAcceptanceTs, Date lastConnectionRequestTs, Date lastAuthenticConnectionRequestTs,
			Date lastConnectionAcceptanceTs, Date lastTicTs, Date lastAckSentTs, int missedCalls)
	{
		addHistoryEntry(remoteMac, oppMode, startDiscoveryTs, beaconFoundTs, firstConnectionAcceptanceTs, lastConnectionRequestTs, lastAuthenticConnectionRequestTs, lastConnectionAcceptanceTs, lastTicTs, lastAckSentTs, missedCalls);
		validateHistoric(remoteMac, oppMode);
	}
}
