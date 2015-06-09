package das.ufsc.pcbeacon.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;


public class BeaconDefaults 
{
	//JSON KEYS
	public static final String MAC_KEY = "MAC";
	public static final String OPP_MODE_KEY = "OPP_MODE";
	public static final String ACK_KEY = "ACK";
	public static final String TIC_KEY = "TIC";
	public static final String DISCOVERYDATE_KEY = "DISCTS";
	public static final String BEACON_FOUND_TS_KEY = "BEACONFOUNDTS";
	public static final String FIRSTCONNTS_KEY = "FIRSTCONNTS";
	public static final String LASTCONNTS_KEY = "LASTCONNTS";
	
	//OPP MODES
	public static final int OPP_MODE_AUTHENTIC = 0;
	public static final int OPP_MODE_DUBIOUS = 1;
	
	
	
	public static String getTicJson(int secs)
	{
		String msg = "{"+ BeaconDefaults.TIC_KEY + ":" + secs +"}";

		return msg;
	}
	
	
	/**
	 * 
	 * @param msgRead message on format {MAC:'mac',OPP_MODE:'oppMode'}
	 * @return
	 */
	public static int getOppMode(String msgRead)
	{
		//the most of cases the operation mode is authentic
		int oppMode = BeaconDefaults.OPP_MODE_AUTHENTIC;
		
		JSONObject json = new JSONObject(msgRead);
		if(json.has(BeaconDefaults.OPP_MODE_KEY))
		{
			oppMode = json.getInt(BeaconDefaults.OPP_MODE_KEY);
		}
		
		return oppMode;
	}


	public static Date getStartDiscoveryTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.DISCOVERYDATE_KEY))
		{
			String strDate = json.getString(BeaconDefaults.DISCOVERYDATE_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}


	public static Date getBeaconFoundTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.BEACON_FOUND_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.BEACON_FOUND_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}

	
	public static Date getFirstConnectionTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.FIRSTCONNTS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.FIRSTCONNTS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}
	

	public static Date getLastAcceptedConnectionTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LASTCONNTS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LASTCONNTS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}

		
	private static Date parseDate(String strDate) throws ParseException 
	{
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
		return formater.parse(strDate);
	}
	
	
}
