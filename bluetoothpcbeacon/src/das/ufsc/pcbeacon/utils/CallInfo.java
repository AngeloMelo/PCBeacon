package das.ufsc.pcbeacon.utils;

import java.util.Date;

public class CallInfo 
{
	//timestamp of receiving call
	private Date timeStamp;
	private int oppMode;
	private String mac;
	private String currentStopName;
	private int currentStopId;
	private Date currentStopTs;
	
	private Date startDiscoveryTs;
	private Date beaconFoundTs;
	private Date firstConnectionTs;
	private Date lastAcceptedConnectionTs;

	
	
	public CallInfo(Date timeStamp, int oppMode, String mac, String currentStopName, int currentStopId, 
			Date currentStopTs, Date startDiscoveryTs, Date beaconFoundTs, Date firstConnectionTs, Date lastAcceptedConnectionTs) 
	{
		super();
		this.timeStamp = timeStamp;
		this.oppMode = oppMode;
		this.mac = mac;
		this.currentStopName = currentStopName;
		this.currentStopId = currentStopId;
		this.currentStopTs = currentStopTs;
		
		this.startDiscoveryTs = startDiscoveryTs;
		this.beaconFoundTs = beaconFoundTs;
		this.firstConnectionTs = firstConnectionTs;
		this.lastAcceptedConnectionTs = lastAcceptedConnectionTs;
	}

	
	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getOppMode() {
		return oppMode;
	}

	public void setOppMode(int oppMode) {
		this.oppMode = oppMode;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public String getCurrentStopName() {
		return currentStopName;
	}

	public int getCurrentStopId() {
		return currentStopId;
	}

	public Date getCurrentStopTs() {
		return currentStopTs;
	}


	public Date getStartDiscoveryTs() {
		return startDiscoveryTs;
	}


	public Date getBeaconFoundTs() {
		return beaconFoundTs;
	}


	public Date getFirstConnectionTs() {
		return firstConnectionTs;
	}


	public Date getLastAcceptedConnectionTs() {
		return lastAcceptedConnectionTs;
	}
	
	
}