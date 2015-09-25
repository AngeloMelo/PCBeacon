package das.ufsc.pcbeacon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Main extends JFrame 
{
	private static final long serialVersionUID = 1L;
	private JLabel lbBusLocation;
	private JLabel lbTotalOnBoard;
	private JButton btStart;
	private JButton btStop;
	private JTextArea txStdOut;
	private JScrollPane spStdOut;
	private JTextField txStopTime;
	private JTextField txSegmentTime;
	private JTextField txRecalTime;
	private JTextField txRecalTimeOffset;
	private JTextField txNumStops;
	private JTextField txDelay;
	JProgressBar pbar;
	
	private Manager manager;

	
	public Main()
	{
		setUpGui();
	}
	
	
	private void setUpGui() 
	{
		this.setSize(1350, 350);
		this.setTitle("BusBeacon Server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT , 5, 5));
		
		lbBusLocation = new JLabel("Bus location:");
		
		pbar = new JProgressBar();
	    pbar.setMinimum(0);
	    
	    
		lbTotalOnBoard = new JLabel("Total on board:");

		txStdOut = new JTextArea(16, 66);
		spStdOut = new JScrollPane(txStdOut);
		spStdOut.setPreferredSize(new Dimension(830, 260));
		PrintStream printStream = new PrintStream(new CustomOutputStream());
		System.setOut(printStream);
		System.setErr(printStream);
		
		txStopTime = new JTextField("1");
		txSegmentTime = new JTextField("600");
		txRecalTime = new JTextField("240");
		txRecalTimeOffset = new JTextField("1");
		txNumStops = new JTextField("1");
		txDelay = new JTextField("0");
		btStart = new JButton("start"); 
		btStart.addActionListener(new StartListener());
		
		btStop = new JButton("stop");
		btStop.addActionListener(new StopListener());
		
		mainPanel.add(getLeftPanel());
		mainPanel.add(getRightPanel());
		
		this.add(mainPanel);
		
		this.setVisible(true);
	}



	private Component getLeftPanel() 
	{
		JPanel leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(490, 300));
		//leftPanel.setBackground(Color.RED);
		Box theBox = Box.createVerticalBox();
		
		theBox.setPreferredSize(new Dimension(490, 300));
		
		JPanel panetit = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panetit.setPreferredSize(new Dimension(490,20));
		panetit.add(new JLabel("Simulation parameters"));
		theBox.add(panetit);
		
		theBox.add(Box.createVerticalStrut(5));
		
		JPanel paramsPanel = new JPanel();
		paramsPanel.setLayout(new GridLayout(3, 4, 14, 4 ));
		paramsPanel.setPreferredSize(new Dimension(500, 100));
		theBox.add(paramsPanel);
		
		paramsPanel.add(new JLabel("Stop time:"));
		paramsPanel.add(txStopTime);
		
		paramsPanel.add(new JLabel("Segment time:"));
		paramsPanel.add(txSegmentTime);

		paramsPanel.add(new JLabel("Recall time min:"));
		paramsPanel.add(txRecalTime);

		paramsPanel.add(new JLabel("Recall time offset:"));
		paramsPanel.add(txRecalTimeOffset);
		
		paramsPanel.add(new JLabel("Num of stops:"));
		paramsPanel.add(txNumStops);
		
		paramsPanel.add(new JLabel("Exec. Delay:"));
		paramsPanel.add(txDelay);
		
		theBox.add(Box.createVerticalStrut(25));
		JPanel panetit2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panetit2.setPreferredSize(new Dimension(500,20));
		panetit2.add(new JLabel("Simulation status"));
		theBox.add(panetit2);
		theBox.add(Box.createVerticalStrut(20));
		
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayout(3, 0, 28, 4 ));
		//statusPanel.setBackground(Color.GREEN);
		statusPanel.setPreferredSize(new Dimension(500, 100));
		
		statusPanel.add(lbBusLocation);
		statusPanel.add(pbar);
		statusPanel.add(lbTotalOnBoard);
		theBox.add(statusPanel);
		theBox.add(Box.createVerticalStrut(25));
		
		JPanel buttonnPanel = new JPanel();
		buttonnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		buttonnPanel.setPreferredSize(new Dimension(500, 50));
		
		buttonnPanel.add(btStart);
		buttonnPanel.add(btStop);
		theBox.add(buttonnPanel);
		
		leftPanel.add(theBox);
		return leftPanel;
	}

	private Component getRightPanel() 
	{
		JPanel rightPanel = new JPanel();
		rightPanel.setPreferredSize(new Dimension(832, 300));
		rightPanel.setLayout(new BorderLayout());
		//rightPanel.setBackground(Color.GREEN);
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Std out"));
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(spStdOut);
		//bottomPanel.setBackground(Color.BLUE);
		
		rightPanel.add(topPanel, BorderLayout.NORTH);
		rightPanel.add(bottomPanel, BorderLayout.SOUTH);
		return rightPanel;
	}

	public static void main(String[] args) 
	{
		new Main();
	}

	
	public void updateBar(int newValue) 
	{
	    pbar.setValue(newValue);
	}
	
	class StartListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			startSimulation();			
		}
	}
	
	class StopListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			stopSimulation();			
		}
	}
	
	
	class ProgressHandler extends Thread
	{
		public void run()
		{
			int tripSegmentTime = new Integer(txSegmentTime.getText());
			for (int i = 0; i <= tripSegmentTime; i++) 
			{
				updateBar(i);
			    try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public void startSimulation() 
	{
		int ticTimeMin = new Integer(this.txRecalTime.getText());
		int ticTimeOffset = new Integer(this.txRecalTimeOffset.getText());
		int stopTime = new Integer(this.txStopTime.getText());
		int tripSegmentTime = new Integer(this.txSegmentTime.getText());
		int delay = new Integer(this.txDelay.getText());
		
		int numOfStops = getTotalOfStops();
		
		pbar.setMaximum(tripSegmentTime);
		
		this.manager = new Manager(this);
		
		manager.setTicTimeMin(ticTimeMin);
		manager.setTicTimeOffset(ticTimeOffset);
		manager.setDelay(delay);
		
		LineInfo lineInfo = getLineInfoPerformanceTest(stopTime, tripSegmentTime);
		
		LineEmulator lineEmulator = new LineEmulator(lineInfo, manager, 15);
		manager.start();
		lineEmulator.start();
		
	}
	
	private LineInfo getLineInfoPerformanceTest(int stopTime, int tripSegmentTime) 
	{
		List<TripSegmentInfo> stopList = new ArrayList<>();
		
		stopList.add(new TripSegmentInfo(1, "TERMINAL 1", "", stopTime, true));
		stopList.add(new TripSegmentInfo(2, "TERMINAL 1", "SEG 1", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(3, "TERMINAL 1", "SEG 2", 60, false));
		stopList.add(new TripSegmentInfo(4, "TERMINAL 2", "", stopTime, true));
		
		return new LineInfo(513, "Performance Test", stopList);
	}


	private LineInfo getLineInfo(int stopTime, int tripSegmentTime) 
	{
		List<TripSegmentInfo> stopList = new ArrayList<>();
		
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
		stopList.add(new TripSegmentInfo(14, "UFSC - CFH", "UFSC - BU", tripSegmentTime, false));
		stopList.add(new TripSegmentInfo(15, "Stop on UFSC - BU", "", stopTime, true));
/*
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
		
		return new LineInfo(513, "Volta ao Morro Carvoeira Sul", stopList);
	}

	
	private int getTotalOfStops() 
	{
		String strParStops = this.txNumStops.getText();
		if(strParStops == "") return 27;
		
		int parStops = new Integer(strParStops);
		
		if(parStops > 13) return 27;
		if(parStops < 0) return 3;
		
		return (parStops * 2) + 1;
	}


	public void printTotalOnBoard(String msg)
	{
		this.lbTotalOnBoard.setText("Total on Board:" + msg);
	}

	public void printLocation(String msg)
	{
		new ProgressHandler().start();
		this.lbBusLocation.setForeground(Color.blue);
		this.lbBusLocation.setText("Bus Location:" + msg);
	}

	public void printStop(String msg)
	{
		this.lbBusLocation.setForeground(Color.red);		
		this.lbBusLocation.setText(msg);
	}
	
	public void stopSimulation() 
	{
		if(manager == null) return;
		manager.stop();	
	}
	
	class CustomOutputStream extends OutputStream 
	{
	    @Override
	    public void write(int b) throws IOException 
	    {
	        // redirects data to the text area
	    	txStdOut.append(String.valueOf((char)b));
	        // scrolls the text area to the end of data
	    	txStdOut.setCaretPosition(txStdOut.getDocument().getLength());
	    }
	}
}
