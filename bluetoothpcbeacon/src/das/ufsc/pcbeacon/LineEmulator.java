package das.ufsc.pcbeacon;

import java.util.Date;
import java.util.List;

public class LineEmulator extends Thread
{
	private LineInfo lineInfo;
	private Manager managerRef;

	public LineEmulator(LineInfo lineInfo, Manager mgr)
	{
		this.lineInfo = lineInfo;
		this.managerRef = mgr;
	}
	
	public void run() 
	{
		//while(!this.managerRef.isBluetoothReady());
		
		this.managerRef.setLineInfo(this.lineInfo);
		
		int cont = 2;
		for(TripSegmentInfo tripSegmentInfo : this.lineInfo.getTripSegmentList())
		{
			if((cont++) == this.lineInfo.getTripSegmentList().size())
			{
				tripSegmentInfo.setLast(true);
			}
			this.managerRef.setCurrentTripSegmentInfo(tripSegmentInfo);
			
			try 
			{
				sleep(tripSegmentInfo.getTripSegmentTime() * 1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}	
		
		this.managerRef.stop();
		this.managerRef.removeDubious();
		this.managerRef.printReports();
	}
}

class LineInfo
{
	private int lineId;
	private String lineNm;
	private List<TripSegmentInfo> tripSegmentList;
	
	public LineInfo(int lineId, String lineNm, List<TripSegmentInfo> tripSegmentList) 
	{
		super();
		this.lineId = lineId;
		this.lineNm = lineNm;
		this.tripSegmentList = tripSegmentList;
	}

	public List<TripSegmentInfo> getTripSegmentList() 
	{
		return this.tripSegmentList;
	}

	public int getLineId() {
		return lineId;
	}

	public String getLineNm() {
		return lineNm;
	};
	
	public TripSegmentInfo getNextStopOf(TripSegmentInfo from)
	{
		int index = this.tripSegmentList.indexOf(from);
		
		if(index == this.tripSegmentList.size() - 1) return from;
		
		return this.tripSegmentList.get(index + 1);
	}
}


class TripSegmentInfo
{
	private int tripSegmentId;
	private String tripSegmentDestination;
	private String tripSegmentOrign;
	private int tripSegmentTime;
	private Date tripSegmentStartTs;
	private boolean stop;
	private boolean last;
	
	public TripSegmentInfo()
	{
		super();
	}
	
	
	public TripSegmentInfo(int tripSegmentId, String tripSegmentOrign, String tripSegmentDestination, int tripSegmentTime, boolean stop) 
	{
		super();
		this.tripSegmentId = tripSegmentId;
		this.tripSegmentOrign = tripSegmentOrign;
		this.tripSegmentDestination = tripSegmentDestination;
		this.tripSegmentTime = tripSegmentTime;
		this.stop = stop;
		this.last = false;
	}


	public boolean isStop() {
		return stop;
	}

	public boolean isLast() {
		return last;
	}
	
	public int getTripSegmentId() {
		return tripSegmentId;
	}

	public void setTripSegmentId(int tripSegmentId) {
		this.tripSegmentId = tripSegmentId;
	}

	public String getTripSegmentDestination() {
		return tripSegmentDestination;
	}

	public String getTripSegmentOrign() 
	{
		return this.tripSegmentOrign;
	}

	public int getTripSegmentTime() {
		return tripSegmentTime;
	}

	public void setTripSegmentTime(int tripSegmentTime) {
		this.tripSegmentTime = tripSegmentTime;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void setLast(boolean last) {
		this.last = last;
	}
	
	public Date getTripSegmentStartTs() {
		return tripSegmentStartTs;
	}

	public void setTripSegmentStartTs(Date tripSegmentStartTs) {
		this.tripSegmentStartTs = tripSegmentStartTs;
	}
	
	
}