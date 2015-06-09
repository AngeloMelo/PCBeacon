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
		boolean first = true;
		
		for(StopInfo stopInfo : this.lineInfo.getStopList())
		{
			this.managerRef.setCurrentStopInfo(stopInfo);
			
			if(first)
			{
				try { sleep(15 * 1000); } catch (InterruptedException e1) {}
				first = false;
			}
			
			try 
			{
				sleep(stopInfo.getNextStopTime()* 1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}	
		
		this.managerRef.stop();
		this.managerRef.printTotalBySegmentsReport();
		this.managerRef.printODReport();
		this.managerRef.printPerformanceReport();
	}
}

class LineInfo
{
	private int lineId;
	private String lineNm;
	private List<StopInfo> stopList;
	
	public LineInfo(int lineId, String lineNm, List<StopInfo> stopList) 
	{
		super();
		this.lineId = lineId;
		this.lineNm = lineNm;
		this.stopList = stopList;
	}

	public List<StopInfo> getStopList() 
	{
		return this.stopList;
	};
}



class StopInfo
{
	private int stopId;
	private String stopName;
	private int nextStopTime;
	private Date stopTs;
	
	
	public StopInfo(int stopId, String stopNm, int nextStopTime) 
	{
		super();
		this.stopId = stopId;
		this.stopName = stopNm;
		this.nextStopTime = nextStopTime;
	};
	
	
	public int getNextStopTime()
	{
		return this.nextStopTime;
	}
	

	public int getStopId() {
		return stopId;
	}


	public String getStopName() {
		return stopName;
	}


	public Date getStopTs() {
		return stopTs;
	}

	
	public void setStopTs(Date stopTs) {
		this.stopTs = stopTs;
	}

}