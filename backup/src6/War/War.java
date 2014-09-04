package War;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.InputMismatchException;
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
	private static FileHandler fh = null;
	private static Scanner in = new Scanner(System.in);
	
	private static int time = 0;
	private static Timer timer;
	
	private static int initThreadCount = 0;
	private static CountDownLatch warStartLatch;
	
	private static boolean alive;
	
	private static Map<String,Launcher> launchers;
	private static Map<String,IronDome> ironDomes;
	private static List<LauncherDestroyer> launcherDestroyers;
	
	
	
	public static void main(String[] args) {

		cleanDir();
		initLogger();
		
		timer = new Timer(DELAY, new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				time++;
			}
		});	
		
		Document doc = getXMLfile(FN);

		launchers = ReadXML.getLaunchersFromXML(doc);
		ironDomes = ReadXML.getIronDomesFromXML(doc);
		launcherDestroyers = ReadXML.getLauncherDestroyersFromXML(doc);
		
		preWarSetup();
		printWarStarting();
		
		// war start latch
		warStartLatch = new CountDownLatch(initThreadCount);
		
		startInitThreads();
		
		// waiting for all the threads to start
		try {
			warStartLatch.await();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		time++;
		timer.start();
		alive = true;
		
		do {
			int choice = warMenuSelection();
			
			switch (choice) {
				case 1:	
						break;
				case 2:	
						break;
				case 3:	
						break;
				case 4:	
						break;
				case 5:	
						break;
				case 6:	
						break;
				case 7:	
						break;
				case 8:	endWar();
						break;
		}
			
		} while (alive);
		
		
//		runWarTest();
		
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
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	private static void preWarSetup() {
		int choice = preWarMenuSelection();
		
		while (choice != 6) {
			
			switch (choice) {
				case 1:	printWar();
						break;
				case 2:	inputLauncher();
						break;
				case 3:	inputMissile();
						break;
				case 4:	inputIronDome();
						break;
				case 5:	inputLauncherDestroyer();
						break;
			}
			
			choice = preWarMenuSelection();
		}
	}
	
	private static int preWarMenuSelection() {
		int choice = 0;
		System.out.println();
		System.out.println("<<<--- Pre-War Initialize Menu --->>>");
		System.out.println("Please select an option:");
		System.out.println("1: Show Current War Setup");
		System.out.println("2: Add Launcher");
		System.out.println("3: Add Missile");
		System.out.println("4: Add Iron Dome");
		System.out.println("5: Add Launcher Destroyer");
		System.out.println("6: Finish Setup And Start War!");
		System.out.println();
		
		boolean ok = false;
		while (!ok) {
			try {
				System.out.println("Please enter your choice (number):");
				choice = in.nextInt();
				
				if (choice < 1 || choice > 6)
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid choice");
				in.nextLine();
			}
		}
		System.out.println();
		return choice;
	}
	
	public static void printWar() {
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			System.out.println(launcherIt.next());
		
		Collection<IronDome> ironDomeCol = ironDomes.values();
		Iterator<IronDome> ironDomeIt = ironDomeCol.iterator();
		while(ironDomeIt.hasNext())
			System.out.println(ironDomeIt.next());
		
		for(LauncherDestroyer ld : launcherDestroyers)
			System.out.println(ld);
		
	}
	
	private static void inputLauncher() {
		System.out.println("Enter Launcher ID (one word - preferred Lxxx):");
		String id = in.next();
		if ( launchers.containsKey(id) ) {
			System.out.println("\nLauncher already exist");
			return;
		}

		in.nextLine();
		System.out.println("Can be Hidden? (true / false):");
		String str = in.next();
		boolean isHidden = Boolean.parseBoolean(str);

		Launcher l = new Launcher(id,isHidden);
		launchers.put(l.getID(), l);
		
		System.out.println("\nLauncher Successfully added:\n\n" + l);
	}
	
	private static void inputMissile() {
		System.out.println("Enter Missile ID (one word - preferred Mxxx):");
		String id = in.next();
		if (getMissile(id) != null) {
			System.out.println("\nMissile already exist");
			return;
		}

		in.nextLine();
		System.out.println("Enter Launcher ID to add missile to (one word):");
		String launcherid = in.next();
		while ( !launchers.containsKey(launcherid) ) {
			in.nextLine();
			System.out.println("\nERROR: Launcher ID doesn't exist");
			System.out.println("Enter Launcher ID to add missile to (one word) or # to cancel:");
			launcherid = in.next();
			if (launcherid.compareTo("#") == 0)
				return;
		}
		
		in.nextLine();
		System.out.println("Destination:");
		String destination = in.nextLine();
		
		boolean ok = false;
		int launchTime = 0;
		while (!ok) {
			try {
				System.out.println("Launch Time:");
				launchTime = in.nextInt();
				
				if (launchTime < 0)
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid time");
				in.nextLine();
			}
		}
		
		ok = false;
		int flyTime = 0;
		while (!ok) {
			try {
				System.out.println("Fly Time:");
				flyTime = in.nextInt();
				
				if (flyTime < 0)
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid fly time");
				in.nextLine();
			}
		}
		
		ok = false;
		int damage = 0;
		while (!ok) {
			try {
				System.out.println("Damage:");
				damage = in.nextInt();
				
				if (damage < 0)
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid damage");
				in.nextLine();
			}
		}
		
		Launcher l = launchers.get(launcherid);
		Missile m = new Missile(id,destination,launchTime,flyTime,damage,l);
		l.addMissile(m);

		System.out.println("\nMissile Successfully added:\n\n" + l);
	}
	
	private static void inputIronDome() {
		System.out.println("Enter IronDome ID (one word - preferred Dxxx):");
		String id = in.next();
		if ( ironDomes.containsKey(id) ) {
			System.out.println("\nIronDome already exist");
			return;
		}
		System.out.println("Do you want to add Missile Targets? ( y / n )");
		char c = in.next().charAt(0);
		while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
			System.out.println("\nERROR: invalid choice");
			System.out.println("Do you want to add Missile Targets? ( y / n )");
			c = in.next().charAt(0);
		}
		
		if (c == 'N' || c == 'n') {
			IronDome irond = new IronDome(id);
			ironDomes.put(irond.getID(), irond);
			System.out.println("\nIronDome Successfully added:\n\n" + irond);
			return;
		}
		
		Heap<Target> targetMissiles = new Heap<Target>(Target.targetComparator);
		String mid = null;
		do {
			System.out.println("\nMissile ID (enter # to stop):");
			mid = in.next();
			if (mid.compareTo("#") != 0) {
				Missile m = getMissile(mid);

				if ( m != null ) {
					
					boolean ok = false;
					int destroyTime = 0;
					while (!ok) {
						try {
							System.out.println("Destroy Time:");
							destroyTime = in.nextInt();
							
							if (destroyTime < 0)
								throw new InputMismatchException();
							
							ok = true;
						} catch (InputMismatchException e) {
							System.out.println("\nERROR: not a valid time");
							in.nextLine();
						}
					}
					
					Target t = new Target(m, destroyTime);
					targetMissiles.add(t);
					
				} else {
					System.out.println("\nMissile doesn't exist");
				}
			}
		} while ( mid.compareTo("#") != 0);
		
		IronDome irond = new IronDome(id, targetMissiles);
		ironDomes.put(irond.getID(), irond);
		System.out.println("\nIronDome Successfully added:\n\n" + irond);
	}
	
	private static void inputLauncherDestroyer() {
		System.out.println("Enter Launcher Destroyer type ( plane / ship ):");
		String type = in.next();
		while ( type.compareTo("plane")!=0 && type.compareTo("ship")!=0 ) {
			System.out.println("\nERROR: incorrect type");
			System.out.println("Enter Launcher Destroyer type ( plane / ship ) or # to cancel:");
			type = in.next();
			if (type.compareTo("#") == 0)
				return;
		}
		
		System.out.println("Do you want to add Missile Targets? ( y / n )");
		char c = in.next().charAt(0);
		while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
			System.out.println("\nERROR: invalid choice");
			System.out.println("Do you want to add Missile Targets? ( y / n )");
			c = in.next().charAt(0);
		}
		
		LauncherDestroyer ld = null;
		
		if (c == 'N' || c == 'n') {
			
			if (type.compareTo("plane")==0)
				ld = new Aircraft();
			else
				ld = new Battleship();
			
			launcherDestroyers.add(ld);
			System.out.println(	"\nLauncher Destroyer " + ld.getClass().getSimpleName()
								+ " Successfully added:\n\n" + ld);
			return;
		}
		
		Heap<Target> targetLaunchers = new Heap<Target>(Target.targetComparator);
		String lid = null;
		do {
			System.out.println("\nLauncher ID (enter # to stop):");
			lid = in.next();
			if (lid.compareTo("#") != 0) {
				Launcher l = getLauncher(lid);

				if ( l != null ) {
					
					boolean ok = false;
					int destroyTime = 0;
					while (!ok) {
						try {
							System.out.println("Destroy Time:");
							destroyTime = in.nextInt();
							
							if (destroyTime < 0)
								throw new InputMismatchException();
							
							ok = true;
						} catch (InputMismatchException e) {
							System.out.println("\nERROR: not a valid time");
							in.nextLine();
						}
					}
					
					Target t = new Target(l, destroyTime);
					targetLaunchers.add(t);
					
				} else {
					System.out.println("\nLauncher doesn't exist");
				}
			}
		} while ( lid.compareTo("#") != 0);
		
		if (type.compareTo("plane")==0)
			ld = new Aircraft(targetLaunchers);
		else
			ld = new Battleship(targetLaunchers);
		
		launcherDestroyers.add(ld);
		System.out.println(	"\nLauncher Destroyer " + ld.getClass().getSimpleName()
							+ " Successfully added:\n\n" + ld);
	}

	private static void printWarStarting() {
		System.out.println("\n<<<<< STARTING WAR IN >>>>>");
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {}
		System.out.println("\t<<-- 3 -->>");
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {}
		System.out.println("\t<<-- 2 -->>");
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {}
		System.out.println("\t<<-- 1 -->>");
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {}
		System.out.println();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	private static int warMenuSelection() {
		int choice = 0;
		System.out.println();
		System.out.println("<<<--- War Management System Menu --->>>");
		System.out.println("Please select an option:");
		System.out.println("1: Add Launcher Destroyer");
		System.out.println("2: Add Iron Dome");
		System.out.println("3: Add Launcher");
		System.out.println("4: Launch Missile");
		System.out.println("5: Destroy Launcher");
		System.out.println("6: Intercept Missile");
		System.out.println("7: Show Statistics");
		System.out.println("8: End War & Show Statistics");
		System.out.println();
		System.out.print("Please enter your choice (number): ");

		boolean ok = false;
		while (!ok) {
			try {
				choice = in.nextInt();
				
				if (choice < 1 || choice > 8)
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid choice");
				System.out.print("Please enter your choice (number): ");
			}
		}
		System.out.println();
		return choice;
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

		Collection<IronDome> ironDomeCol = ironDomes.values();
		Iterator<IronDome> ironDomeIt = ironDomeCol.iterator();
		while(ironDomeIt.hasNext())
			ironDomeIt.next().start();

		for(LauncherDestroyer ld : launcherDestroyers)
			ld.start();

	}
	
	private static void endWar(){
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) 
			launcherIt.next().end();

		Collection<IronDome> ironDomeCol = ironDomes.values();
		Iterator<IronDome> ironDomeIt = ironDomeCol.iterator();
		while(ironDomeIt.hasNext())
			ironDomeIt.next().end();

		for(LauncherDestroyer ld : launcherDestroyers)
			ld.end();
		
		alive = false;
		timer.stop();
		fh.close();
		
		System.out.println("\n<<<<< WAR ENDED >>>>>\n");
	}
	
	public static int getTime() {
		return time;
	}
	
	public static CountDownLatch getWarStartLatch() {
		return warStartLatch;
	}

	public static void increaseInitThreadCount() {
		initThreadCount++;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	/** Return the Launcher if exist */
	public static Launcher getLauncher(String launcherID) {
		return launchers.get(launcherID);
	}
	
	/** Search the Missile in all the Launchers and Return if exist */
	public static Missile getMissile(String missileID) {
		Missile m = null;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while (launcherIt.hasNext() && m==null)
			m = launcherIt.next().getMissile(missileID);

		return m;
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

	public static void runWarTest() {
		
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

}


