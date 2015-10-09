package das.ufsc.pcbeacon;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CallHistoric
{
	private String mac;
	private Date startDiscoveryTs;
	private Date beaconFoundTs;
	private Date firstConnectionAcceptanceTs;
	private List<CallInfo> calls;
	
	public CallHistoric()
	{
		super();
		this.calls = new LinkedList<>();
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Date getStartDiscoveryTs() {
		return startDiscoveryTs;
	}

	public void setStartDiscoveryTs(Date startDiscoveryTs) {
		this.startDiscoveryTs = startDiscoveryTs;
	}

	public Date getBeaconFoundTs() {
		return beaconFoundTs;
	}

	public void setBeaconFoundTs(Date beaconFoundTs) {
		this.beaconFoundTs = beaconFoundTs;
	}

	public List<CallInfo> getCalls() {
		return calls;
	}

	public void setFirstConnectionAcceptanceTs(Date date) 
	{
		this.firstConnectionAcceptanceTs = date;
	}
	
	public Date getFirstConnectionAcceptanceTs()
	{
		return firstConnectionAcceptanceTs;
	}
}

class CallInfo 
{
	//time of this call
	private Date callTimeStamp;
	private int oppMode;
	
	//performance data
	private Date lastConnectionRequestTs;
	private Date lastAuthenticConnectionRequestTs;
	private Date lastConnectionAcceptanceTs;
	private Date lastTicTs; 
	private Date lastAckSentTs;
	
	//time of current stop
	private Date currentTripSegmentTs;
	private String currentTripSegmentOrign;
	private String currentTripSegmentDestination;
	private int currentTripSegmentId;
	private int missedCalls;
		
	public CallInfo() 
	{
		super();
	}

	public Date getCallTimeStamp() {
		return callTimeStamp;
	}

	public void setCallTimeStamp(Date callTimeStamp) {
		this.callTimeStamp = callTimeStamp;
	}

	public int getOppMode() {
		return oppMode;
	}

	public void setOppMode(int oppMode) {
		this.oppMode = oppMode;
	}

	public Date getLastConnectionRequestTs() {
		return lastConnectionRequestTs;
	}

	public void setLastConnectionRequestTs(Date lastConnectionRequestTs) {
		this.lastConnectionRequestTs = lastConnectionRequestTs;
	}

	public Date getLastConnectionAcceptanceTs() {
		return lastConnectionAcceptanceTs;
	}

	public void setLastConnectionAcceptanceTs(Date lastConnectionAcceptanceTs) {
		this.lastConnectionAcceptanceTs = lastConnectionAcceptanceTs;
	}

	public Date getLastTicTs() {
		return lastTicTs;
	}

	public void setLastTicTs(Date lastTicTs) {
		this.lastTicTs = lastTicTs;
	}

	public Date getLastAckSentTs() {
		return lastAckSentTs;
	}

	public void setLastAckSentTs(Date lastAckSentTs) {
		this.lastAckSentTs = lastAckSentTs;
	}

	public Date getCurrentTripSegmentTs() {
		return currentTripSegmentTs;
	}

	public void setCurrentTripSegmentTs(Date currentStopTs) {
		this.currentTripSegmentTs = currentStopTs;
	}

	public String getCurrentTripSegmentOrign() {
		return currentTripSegmentOrign;
	}

	public void setCurrentTripSegmentOrign(String currentTripSegmentOrign) {
		this.currentTripSegmentOrign = currentTripSegmentOrign;
	}

	public String getCurrentTripSegmentDestination() {
		return currentTripSegmentDestination;
	}

	public void setCurrentTripSegmentDestination(String currentTripSegmentDestination) {
		this.currentTripSegmentDestination = currentTripSegmentDestination;
	}

	public int getCurrentTripSegmentId() {
		return currentTripSegmentId;
	}

	public void setCurrentTripSegmentId(int currentStopId) {
		this.currentTripSegmentId = currentStopId;
	}

	public Date getLastAuthenticConnectionRequestTs() 
	{
		return lastAuthenticConnectionRequestTs;
	}
	
	public void setLastAuthenticConnectionRequestTs(Date lastAuthenticConnectionRequestTs) 
	{
		this.lastAuthenticConnectionRequestTs = lastAuthenticConnectionRequestTs;
	}

	public int getMissedCalls() 
	{
		return this.missedCalls;
	}
	
	public void setMissedCalls(int missedCalls) 
	{
		this.missedCalls = missedCalls;
	}
}