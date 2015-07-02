package das.ufsc.pcbeacon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EmulationReportPrinter 
{
	private Map<String, CallHistoric> deviceCalls;
	private List<TripSegmentInfo> tripSegmentList;
	

	public EmulationReportPrinter(Map<String, CallHistoric> deviceCalls, List<TripSegmentInfo> tripSegmentList) 
	{
		super();
		this.deviceCalls = deviceCalls;
		this.tripSegmentList = tripSegmentList;
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
	

	public void printPerformanceReport()
	{
		List<Long> discoveryTimes = new ArrayList<>() ;
		List<Long> firstConnTimes = new ArrayList<>() ;
		List<Long> connectionAcceptanceTimes = new ArrayList<>() ;
		List<Long> protocolTimes = new ArrayList<>() ;
		
		int totalOnBoard = 0;
		int totalCall = 0;
		
		System.out.println("-------------------------------------------------------------------------------------");
		
		String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Dados\\" + fileName + ".csv"), "utf-8"))) 
		{
		    
			for(String mac : deviceCalls.keySet())
			{
				totalOnBoard++;
				
				CallHistoric callHistoric = deviceCalls.get(mac);
				
				long discoveryTime = callHistoric.getBeaconFoundTs().getTime() - callHistoric.getStartDiscoveryTs().getTime();
				discoveryTimes.add(discoveryTime);
				
				long firstCallTime = callHistoric.getFirstConnectionAcceptanceTs().getTime() - callHistoric.getBeaconFoundTs().getTime();			
				firstConnTimes.add(firstCallTime);
				
				for(CallInfo callInfo : callHistoric.getCalls())
				{
					totalCall++;
	
					long connAcceptanceTime = callInfo.getLastConnectionAcceptanceTs().getTime() - callInfo.getLastConnectionRequestTs().getTime();
					connectionAcceptanceTimes.add(connAcceptanceTime);
					
					long protocolTime = callInfo.getLastAckSentTs().getTime() - callInfo.getLastConnectionAcceptanceTs().getTime();
					protocolTimes.add(protocolTime);
					
					//write data to file
					writer.write(discoveryTime + "," + firstCallTime + "," + connAcceptanceTime + "," + protocolTime + "\n");
				}
			}
		
		}
		catch (IOException ex) 
		{
		    ex.printStackTrace();
		} 

		DataRank discoveryDataRank = new DataRank(discoveryTimes);
		DataRank firstConnDataRank = new DataRank(firstConnTimes);
		DataRank connAcceptanceDataRank = new DataRank(connectionAcceptanceTimes);
		DataRank protocolDataRank = new DataRank(protocolTimes);
		
		System.out.println("Discovery time: " + discoveryDataRank.getAvg() + "(avg) " + discoveryDataRank.getMedian() + "(median) " + discoveryDataRank.getMin() + "(min) " + discoveryDataRank.getMax() + "(max)");
		System.out.println("First connection acceptance time: " + firstConnDataRank.getAvg() + "(avg) " + firstConnDataRank.getMedian() + "(median) " + firstConnDataRank.getMin() + "(min) " + firstConnDataRank.getMax() + "(max)");
		System.out.println("Connection acceptance time: " + connAcceptanceDataRank.getAvg() + " (avg) " + connAcceptanceDataRank.getMedian() + "(median) " + connAcceptanceDataRank.getMin() + " (min) " + connAcceptanceDataRank.getMax() + "(max)");
		System.out.println("Protocol execution time: " +  protocolDataRank.getAvg() + " (avg) " + protocolDataRank.getMedian() + "(median) " + protocolDataRank.getMin() + " (min) " + protocolDataRank.getMax() + " (max)");
		System.out.println("Total of passengers:" +  totalOnBoard);
		System.out.println("Total of calls:" +  totalCall);
	}
	
	
	
	public void printODReport()
	{
		//prints the header
		Object []arr = new Object[tripSegmentList.size()+1];
		String formatter = "%-SIZEs ";
		int maxLength = 0;
		int i = 1;
		arr[0] = "Destinos: ";
		for(TripSegmentInfo tripSegmentInfo : tripSegmentList)
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
		for(TripSegmentInfo tripSegmentOrign : tripSegmentList)
		{
			arr[0] = tripSegmentOrign.getTripSegmentOrign();
			int lineIdx = 1;
			
			formatter = "%-" + maxLength + "s ";
			for(TripSegmentInfo tripSegmentDestin : tripSegmentList)
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
}



class DataRank
{
	private long min = 0;
	private long max = 0;
	private long avg = 0;
	private long median = 0;
	
	public DataRank(List<Long> data)
	{
		super();
		Long[] arr = data.toArray(new Long[data.size()]);
		Arrays.sort(arr);
		
		this.min = arr[0];
		this.max = arr[arr.length - 1];
		
		long sum = 0;
		for(int i = 0; i < arr.length; i++)
		{
			sum = sum + arr[i];
		}
		this.avg = sum / arr.length;
		
		if( (arr.length % 2) == 0)
		{
			int medianIdx2 = (arr.length / 2);
			int medianIdx1 = medianIdx2 - 1;
			this.median = (arr[medianIdx1] + arr[medianIdx2])/2;
		}
		else
		{
			int medianIdx = (arr.length / 2);
			this.median = arr[medianIdx];
		}
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public long getAvg() {
		return avg;
	}

	public long getMedian() {
		return median;
	}
}
