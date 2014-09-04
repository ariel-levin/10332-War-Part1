package War;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import Launcher.*;
import LauncherDestroyer.*;
import MissileDestroyer.*;
import Utility.*;


public class War {

	public static final int DELAY = 100;
	private static final String FN = "war.xml";
	
	private static Logger logger = Logger.getLogger("WarLogger");
	private static Scanner in = new Scanner(System.in);
	
	private static int time = 0;
	private static Timer timer = new Timer(DELAY, new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			time++;
		}
	});	
	
	private static int threadCount = 0;
	private static CountDownLatch warStartLatch;
	private static boolean alive;
	
	private static Map<String,Launcher> launchers;
	private static Map<String,MissileDestroyer> missileDestroyers;
	private static List<LauncherDestroyer> launcherDestroyers;
	
	private static FileHandler fh = null;
	
	
	public static void main(String[] args) {

		cleanDir();
		initLogger();
		
		Document doc = getXMLfile(FN);

		launchers = ReadXML.getLaunchersFromXML(doc);
		missileDestroyers = ReadXML.getMissileDestroyersFromXML(doc,launchers);
		launcherDestroyers = ReadXML.getLauncherDestroyersFromXML(doc,launchers);
		
		warStartLatch = new CountDownLatch(threadCount);
		
//		printThreadCount();
		
//		printWar();
		
		startInitThreads();
		
		try {
			warStartLatch.await();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		time++;
		timer.start();
		
//		while (threadCount > 0);
		
		alive = true;
		
//		int temp = time;
//		System.out.println(time);
//		while (alive) {
//			if (time != temp) {
//				System.out.println(time);
//				temp = time;
//			}
//			if (time >= 30)
//				alive = false;
//
//		}
//		
//		endWar();
//		System.out.println("\n" + time + ": END OF WAR\n");
		
		while (alive) {
			
//			printMenu();
//			System.out.print("Please enter choice: ");
//			int choice = in.nextInt();
//			if (choice == 8)
//				endWar();
			
			if (time >= 30)
				endWar();
		}
		
		System.out.println(time + ": END WAR");
	}
	
	private static Document getXMLfile(String fileName) {
		File xmlFile = new File(FN);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;

		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		doc.getDocumentElement().normalize();
		
		return doc;
	}
	
	private static void initLogger() {
		
		// if the directory does not exist, create it
		java.io.File dir = new java.io.File("logs");
		if (!dir.exists()) {
			try{
				dir.mkdir();
			} catch(SecurityException se){}
		}
		
		logger.setUseParentHandlers(false);
		try {
			fh = new FileHandler("logs/War_Log.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter());
		logger.addHandler(fh);
	}
	
	private static void printMenu() {
		System.out.println();
		System.out.println("<<<--- War Management System Menu --->>>");
		System.out.println("Please select an option:");
		System.out.println("1: Add Launcher Destroyer");
		System.out.println("2: Add Missile Destroyer");
		System.out.println("3: Add Launcher");
		System.out.println("4: Launch Missile");
		System.out.println("5: Destroy Launcher");
		System.out.println("6: Intercept Missile");
		System.out.println("7: Show Statistics");
		System.out.println("8: End War & Show Statistics");
		System.out.println();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	private static void startInitThreads() {
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) {
			Launcher l = launcherIt.next();
			l.start();
			Iterator<Missile> missileIt = l.getMissiles().iterator();
			while(missileIt.hasNext())
				missileIt.next().start();
		}

		Collection<MissileDestroyer> missileDesCol = missileDestroyers.values();
		Iterator<MissileDestroyer> missileDesIt = missileDesCol.iterator();
		while(missileDesIt.hasNext())
			missileDesIt.next().start();

		for(LauncherDestroyer ld : launcherDestroyers)
			ld.start();

	}
	
	private static void endWar(){
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) 
			launcherIt.next().kill();

		Collection<MissileDestroyer> missileDesCol = missileDestroyers.values();
		Iterator<MissileDestroyer> missileDesIt = missileDesCol.iterator();
		while(missileDesIt.hasNext())
			missileDesIt.next().kill();

		for(LauncherDestroyer ld : launcherDestroyers)
			ld.kill();
		
		alive = false;
		timer.stop();
		fh.close();
	}
	
	public static int getTime() {
		return time;
	}
	
	public static CountDownLatch getWarStartLatch() {
		return warStartLatch;
	}

	public static void decreaseLatch() {
		warStartLatch.countDown();
	}
	
	public static void increaseThreadCount() {
		threadCount++;
	}
	
	public static void decreaseThreadCount() {
		threadCount--;
	}
	
	public static void printThreadCount() {
		System.out.println("threadCount = " + threadCount);
	}

	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	public static void printWar() {
		
		Collection<Launcher> c1 = launchers.values();
		Iterator<Launcher> it1 = c1.iterator();
		while(it1.hasNext())
			System.out.println(it1.next());
		
		Collection<MissileDestroyer> c2 = missileDestroyers.values();
		Iterator<MissileDestroyer> it2 = c2.iterator();
		while(it2.hasNext())
			System.out.println(it2.next());
		
		for(LauncherDestroyer ld : launcherDestroyers)
			System.out.println(ld);
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	private static void cleanDir() {
		File file = new File("logs");        
		String[] myFiles;
		if(file.isDirectory()){  
			myFiles = file.list();  
			for (int i=0; i<myFiles.length; i++) {  
				File myFile = new File(file, myFiles[i]);   
				myFile.delete();  
			}  
		} 
	}
	
	public static void testHeap() {
		
		Heap<Integer> test = new Heap<Integer>(new Comparator<Integer>() {

			public int compare(Integer x, Integer y) {

				return x.compareTo(y)*(-1);

			}
		});
		
		test.add(2);
		test.add(1);
		test.add(9);
		test.add(2);
		test.add(4);
		int size = test.getSize();
		System.out.println(size);
		for(int i = 0 ; i < size ; i++){
			System.out.print(test.getHead() + "  ");
			test.remove();
		}
		
	}


}


