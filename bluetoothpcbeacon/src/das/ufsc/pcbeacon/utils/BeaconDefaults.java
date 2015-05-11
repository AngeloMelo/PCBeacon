package das.ufsc.pcbeacon.utils;

import java.util.ArrayList;
import java.util.List;

public class BeaconDefaults 
{
	private static final List<String> beaconList;
	
	static
	{
		//setting up the known device list:
		beaconList = new ArrayList<>();
		beaconList.add("");
	}
	
	public static boolean checkBeacon(String mac)
	{
		return BeaconDefaults.beaconList.contains(mac);
	}
	
}
