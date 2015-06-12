package das.ufsc.pcbeacon;

import java.util.ArrayList;
import java.util.List;


public class Main{
	
	public static void main(String[] args) 
	{
		Manager manager = new Manager();

		List<TripSegmentInfo> stopList = new ArrayList<>();
		
		int stopTime = 10;
		int tripSegmentTime = 35;
		
		stopList.add(new TripSegmentInfo(1, "TICEN", "", stopTime, true));
		stopList.add(new TripSegmentInfo(2, "TICEN", "Prainha", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(3, "Stop on Prainha", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(4, "Prainha", "Penhasco", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(5, "Stop on Penhasco", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(6, "Penhasco", "Jose Mendes", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(7, "Stop on Jose Mendes", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(8, "Jose Mendes", "Saco dos Limoes", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(9, "Stop on Saco dos Limoes", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(10, "Saco dos Limoes", "Morro da Carvoeira", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(11, "Stop on Morro da Carvoeira", "", stopTime, true));

		stopList.add(new TripSegmentInfo(12, "Morro da Carvoeira", "UFSC - CFH", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(13, "Stop on UFSC - CFH", "", stopTime, true));
		
		/*
		stopList.add(new StopInfo(7, "", 35));
		stopList.add(new StopInfo(8, "", 35));
		stopList.add(new StopInfo(9, "UFSC - BU", 35));
		stopList.add(new StopInfo(10, "", 6));
		stopList.add(new StopInfo(11, "Praça Pida", 6));
		stopList.add(new StopInfo(12, "Capitão Gourmet", 6));
		stopList.add(new StopInfo(13, "Hospital Infantil", 6));
		stopList.add(new StopInfo(14, "Beiramar Shopping", 6));
		stopList.add(new StopInfo(15, "IFSC", 6));
		stopList.add(new StopInfo(16, "ALESC", 6));
		stopList.add(new StopInfo(17, "TICEN", 6));*/
		
		LineInfo lineInfo = new LineInfo(513, "Volta ao Morro Carvoeira Sul", stopList);
		LineEmulator lineEmulator = new LineEmulator(lineInfo, manager);
		
		manager.start();
		lineEmulator.start();
	}
}