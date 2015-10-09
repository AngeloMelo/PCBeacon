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
	public static final String TIC_LINEID_KEY = "LINEID";
	public static final String TIC_LINENM_KEY = "LINENM";
	public static final String TIC_LASTSTOPNM_KEY = "LASTSTOPNM";
	public static final String TIC_NEXTSTOPNM_KEY = "NEXTSTOPNM";
	
	public static final String DISCOVERYDATE_KEY = "DISCTS";
	public static final String BEACON_FOUND_TS_KEY = "BEACONFOUNDTS";
	public static final String FIRST_CONN_ACCEPTANCE_TS_KEY = "FIRSTCONNACCEPTANCETS";
	public static final String LAST_CONN_REQUEST_TS_KEY = "LASTCONNREQTS";
	public static final String LAST_AUTHENTIC_CONN_REQUEST_TS_KEY = "LASTAUTHENTICCONNREQTS";
	public static final String LAST_CONN_ACCEPTANCE_TS_KEY = "LASTCONNACCEPTANCETS";
	public static final String LAST_TIC_RECEIVED_TS_KEY = "LASTTICRECEIVEDTS";
	public static final String LAST_ACK_SENT_TS_KEY = "LASTACKSENTTS";
	public static final String MISSED_CALLS_KEY = "MISSEDCALLS";
	
	//OPP MODES
	public static final int OPP_MODE_AUTHENTIC = 0;
	public static final int OPP_MODE_DUBIOUS = 1;
	

	//INTERRUPT MODES
	public static final int INT_CLOSE_CONNECTION = -1;
	public static final int INT_NO_RECALL = -2;
	
	public static String getTicJson(int secs, int lineId, String lineName, String lastStop)
	{
		String msg = "{"+ BeaconDefaults.TIC_KEY + ":" + secs;
		msg = msg + "," + BeaconDefaults.TIC_LINEID_KEY + ":" + lineId;
		msg = msg + "," + BeaconDefaults.TIC_LINENM_KEY + ":'" + lineName + "'";
		msg = msg + "," + BeaconDefaults.TIC_LASTSTOPNM_KEY + ":'" + lastStop + "'";
		msg = msg + "}";
		
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

	
	public static Date getFirstConnectionAcceptanceTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.FIRST_CONN_ACCEPTANCE_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.FIRST_CONN_ACCEPTANCE_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}
	

	public static Date getLastConnectionRequestTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LAST_CONN_REQUEST_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LAST_CONN_REQUEST_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}

	
	public static Date getLastAuthenticConnectionRequestTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LAST_AUTHENTIC_CONN_REQUEST_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LAST_AUTHENTIC_CONN_REQUEST_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}
	

	
	public static Date getLastConnectionAcceptanceTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LAST_CONN_ACCEPTANCE_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LAST_CONN_ACCEPTANCE_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}

	
	public static Date getLastTicReceivedTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LAST_TIC_RECEIVED_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LAST_TIC_RECEIVED_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}

	
	public static Date getLastAckSentTs(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.LAST_ACK_SENT_TS_KEY))
		{
			String strDate = json.getString(BeaconDefaults.LAST_ACK_SENT_TS_KEY);
			
			return parseDate(strDate);
		}
		return null;
	}
	
	
	public static int getMissedCalls(JSONObject json) throws ParseException 
	{
		if(json.has(BeaconDefaults.MISSED_CALLS_KEY))
		{
			return json.getInt(BeaconDefaults.MISSED_CALLS_KEY);
		}
		return -1;
	}


		
	private static Date parseDate(String strDate) throws ParseException 
	{
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
		return formater.parse(strDate);
	}
	
	
}
