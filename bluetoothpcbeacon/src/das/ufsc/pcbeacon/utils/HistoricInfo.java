package das.ufsc.pcbeacon.utils;

import java.util.Date;

public class HistoricInfo 
{
	private Date timeStamp;
	private int oppMode;
	private String mac;
	
	public HistoricInfo(Date timeStamp, int oppMode, String mac) 
	{
		super();
		this.timeStamp = timeStamp;
		this.oppMode = oppMode;
		this.mac = mac;
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
	
	
}
