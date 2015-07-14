package das.ufsc.pcbeacon;

import java.util.ArrayList;
import java.util.List;


public class Main{
	
	public static void main(String[] args) 
	{
		Manager manager = new Manager();
		manager.setTicTimeMin(13);
		manager.setTicTimeOffset(7);
		
		List<TripSegmentInfo> stopList = new ArrayList<>();
		
		int stopTime = 10;
		int tripSegmentTime = 45;
		
		stopList.add(new TripSegmentInfo(1, "TICEN", "", stopTime, true));
		stopList.add(new TripSegmentInfo(2, "TICEN", "Prainha", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(3, "Stop on Prainha", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(4, "Prainha", "Penhasco", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(5, "Stop on Penhasco", "", stopTime, true));
			
		stopList.add(new TripSegmentInfo(6, "Penhasco", "Jose Mendes", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(7, "Stop on Jose Mendes", "", stopTime, true));
		
		stopList.add(new TripSegmentInfo(8, "Jose Mendes", "Saco dos Limoes", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(9, "Stop on Saco dos Limoes", "", stopTime, true));
/*		
		stopList.add(new TripSegmentInfo(10, "Saco dos Limoes", "Morro da Carvoeira", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(11, "Stop on Morro da Carvoeira", "", stopTime, true));

		stopList.add(new TripSegmentInfo(12, "Morro da Carvoeira", "UFSC - CFH", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(13, "Stop on UFSC - CFH", "", stopTime, true));

		stopList.add(new TripSegmentInfo(14, "UFSC - CFH", "UFSC - BU", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(15, "Stop on UFSC - BU", "", stopTime, true));

		stopList.add(new TripSegmentInfo(16, "UFSC - BU", "Captitao Gourmet", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(17, "Stop on Captitao Gourmet", "", stopTime, true));

		stopList.add(new TripSegmentInfo(18, "Captitao Gourmet", "Hospital Infantil", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(19, "Stop on Hospital Infantil", "", stopTime, true));

		stopList.add(new TripSegmentInfo(20, "Hospital Infantil", "Beiramar Shopping", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(21, "Stop on Beiramar Shopping", "", stopTime, true));

		stopList.add(new TripSegmentInfo(22, "Beiramar Shopping", "IFSC", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(23, "Stop on IFSC", "", stopTime, true));

		stopList.add(new TripSegmentInfo(24, "IFSC", "ALESC", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(25, "Stop on ALESC", "", stopTime, true));

		stopList.add(new TripSegmentInfo(26, "ALESC", "TICEN", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(27, "Stop on TICEN", "", stopTime, true));
		*/
		
		LineInfo lineInfo = new LineInfo(513, "Volta ao Morro Carvoeira Sul", stopList);
		LineEmulator lineEmulator = new LineEmulator(lineInfo, manager);
		manager.start();
		lineEmulator.start();
	}
}