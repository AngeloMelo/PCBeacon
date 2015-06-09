package das.ufsc.pcbeacon;

import java.util.ArrayList;
import java.util.List;


public class Main{
	
	public static void main(String[] args) 
	{
		Manager manager = new Manager();

		List<StopInfo> stopList = new ArrayList<>();
		
		stopList.add(new StopInfo(1, "TICEN", 20));
		stopList.add(new StopInfo(2, "Prainha", 20));
		stopList.add(new StopInfo(3, "Penhasco", 20));
		stopList.add(new StopInfo(4, "José Mendes", 20));
		stopList.add(new StopInfo(5, "Saco dos Limões - Praça", 20));
		stopList.add(new StopInfo(6, "Saco dos Limões - Colégio", 20));
		stopList.add(new StopInfo(7, "Morro da Carvoeira", 20));
		stopList.add(new StopInfo(8, "Morro da Carvoeira 2", 20));
		stopList.add(new StopInfo(9, "UFSC - CFH", 20));/*
		stopList.add(new StopInfo(10, "UFSC - BU", 6));
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