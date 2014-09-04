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

	public static final int DELAY = 1000;
	
	private final String FN = "war.xml";
	
	private Logger logger = Logger.getLogger("WarLogger");
	private FileHandler fh = null;
	private Scanner in = new Scanner(System.in);
	
	private int time = 0;
	private Timer timer;
	
	private int initThreadCount = 0;
	private CountDownLatch warStartLatch;
	
	private boolean alive = false;
	
	private Map<String,Launcher> launchers;
	private Map<String,IronDome> ironDomes;
	private List<LauncherDestroyer> launcherDestroyers;
	
	
	public static void main(String[] args) {
		new War();
	}
	
	public War() {

		cleanDir();
		initLogger();
		
		timer = new Timer(DELAY, new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				time++;
			}
		});	
		
		Document doc = getXMLfile(FN);

		launchers = ReadXML.getLaunchersFromXML(doc,this);
		ironDomes = ReadXML.getIronDomesFromXML(doc,this);
		launcherDestroyers = ReadXML.getLauncherDestroyersFromXML(doc,this);
		
		preWarSetup();
		printWarStarting();
		
		// war start latch for all the threads to start together
		warStartLatch = new CountDownLatch(initThreadCount);
		
		// starting all the threads
		startInitThreads();
		
		// waiting for all the threads to start together
		try {
			warStartLatch.await();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// WAR STARTED!
		time++;
		timer.start();
		alive = true;
		
		do {
			int choice = warMenuSelection();
			
			switch (choice) {
				case 1:	inputLauncherDestroyer();
						break;
				case 2:	inputIronDome();
						break;
				case 3:	inputLauncher();
						break;
				case 4:	launchMissile();
						break;
				case 5:	destroyLauncher();
						break;
				case 6:	interceptMissile();
						break;
				case 7:	Statistics.showStatistics(launchers, time);
						break;
				case 8:	endWar();
						break;
			}
			
		} while (alive);
		
		
//		runWarTest();
		
	}
	
	private Document getXMLfile(String fileName) {
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
	
	private void initLogger() {
		
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
		fh.setFormatter(new LogFormatter(this));
		logger.addHandler(fh);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	private void preWarSetup() {
		int choice = preWarMenuSelection();
		
		while (choice != 6) {
			
			switch (choice) {
				case 1:	printWar();
						break;
				case 2:	inputLauncher();
						break;
				case 3:	inputMissile(null);
						break;
				case 4:	inputIronDome();
						break;
				case 5:	inputLauncherDestroyer();
						break;
			}
			
			choice = preWarMenuSelection();
		}
	}
	
	private int preWarMenuSelection() {
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
	
	public void printWar() {
		
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
	
	private void inputLauncher() {
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

		Launcher l = new Launcher(id,isHidden,this);
		launchers.put(l.getID(), l);

		if (alive) {
			l.start();

		} else {	// if war haven't started yet we give option to add missiles

			System.out.println("Do you want to add Missiles now? ( y / n )");
			char c = in.next().charAt(0);
			while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
				System.out.println("\nERROR: invalid choice");
				System.out.println("Do you want to add Missiles now? ( y / n )");
				c = in.next().charAt(0);
			}

			if (c == 'Y' || c == 'y') {

				boolean more = true;
				while (more) {
					inputMissile(l);

					System.out.println("more? ( y / n )");
					c = in.next().charAt(0);
					while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
						System.out.println("\nERROR: invalid choice");
						System.out.println("more? ( y / n )");
						c = in.next().charAt(0);
					}

					if (c == 'N' || c == 'n')
						more = false;
				}

			}
			
			initThreadCount++;
		}
		
		System.out.println("\nLauncher Successfully added:\n\n" + l);
	}
	
	private void inputMissile(Launcher launcherInput) {
		System.out.println("Enter Missile ID (one word - preferred Mxxx):");
		String id = in.next();
		if (getMissile(id) != null) {
			System.out.println("\nMissile already exist");
			return;
		}

		Launcher launcher = null;
		
		// if no launcher was received, we ask for launcher ID
		if (launcherInput == null) {
			in.nextLine();
			System.out.println("Enter Launcher ID to add missile to (one word):");
			String launcherID = in.next();
			launcher = launchers.get(launcherID);
			while ( launcher == null ) {
				in.nextLine();
				System.out.println("\nERROR: Launcher ID doesn't exist");
				System.out.println("Enter Launcher ID to add missile to (one word) or # to cancel:");
				launcherID = in.next();
				if (launcherID.compareTo("#") == 0)
					return;
				
				launcher = launchers.get(launcherID);
			}
		} else {
			launcher = launcherInput;
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
				
				if (launchTime < time)
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
		
		Missile m = new Missile(id,destination,launchTime,flyTime,damage,launcher,this);
		launcher.addMissile(m);

		System.out.println(	"\nMissile Successfully added:\n\n" + m +
							"\n\t\tLauncher: " + launcher.getID() + "\n");

		if (alive)
			m.start();
		else
			initThreadCount++;
	}
	
	private void inputIronDome() {
		System.out.println("Enter IronDome ID (one word - preferred Dxxx):");
		String id = in.next();
		if ( ironDomes.containsKey(id) ) {
			System.out.println("\nIronDome already exist");
			return;
		}

		IronDome irond = new IronDome(id,this);
		ironDomes.put(irond.getID(), irond);

		if (alive) {
			irond.start();

		} else {	// if war haven't started yet we give option to add targets

			System.out.println("Do you want to add Missile Targets? ( y / n )");
			char c = in.next().charAt(0);
			while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
				System.out.println("\nERROR: invalid choice");
				System.out.println("Do you want to add Missile Targets? ( y / n )");
				c = in.next().charAt(0);
			}

			if (c == 'Y' || c == 'y') {

				System.out.println("\nMissile ID (enter # to stop):");
				String mid = in.next();

				while ( mid.compareTo("#") != 0 ) {

					Missile m = getMissile(mid);

					if ( m == null ) {
						System.out.println("\nMissile doesn't exist");

					} else {
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
						irond.addTarget(t);
					}

					System.out.println("\nMissile ID (enter # to stop):");
					mid = in.next();
				}

			} 

			initThreadCount++;
		}
		
		System.out.println("\nIronDome Successfully added:\n\n" + irond);
	}
	
	private void inputLauncherDestroyer() {
		
		System.out.println("Enter Launcher Destroyer type or # to cancel:");
		in.nextLine();
		String type = in.nextLine();

		if (type.compareTo("#") == 0)
			return;

		LauncherDestroyer ld = new LauncherDestroyer(type,this);
		launcherDestroyers.add(ld);

		if (alive) {
			ld.start();

		} else {	// if war haven't started yet we give option to add targets

			System.out.println("Do you want to add Launcher Targets? ( y / n )");
			char c = in.next().charAt(0);
			while (c != 'Y' && c != 'y' && c != 'N' && c != 'n' ) {
				System.out.println("\nERROR: invalid choice");
				System.out.println("Do you want to add Launcher Targets? ( y / n )");
				c = in.next().charAt(0);
			}

			if (c == 'Y' || c == 'y') {

				System.out.println("\nLauncher ID (enter # to stop):");
				String lid = in.next();

				while ( lid.compareTo("#") != 0) {

					Launcher l = getLauncher(lid);

					if ( l == null ) {
						System.out.println("\nLauncher doesn't exist");

					} else {

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
						ld.addTarget(t);
					}

					System.out.println("\nLauncher ID (enter # to stop):");
					lid = in.next();
				}
			}

			initThreadCount++;
		}
		
		System.out.println(	"\nLauncher Destroyer '" + ld.getType()
							+ "' Successfully added:\n\n" + ld);
	}

	private void printWarStarting() {
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
	
	
	private int warMenuSelection() {
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
	
	private void launchMissile() {
		
		printLaunchers();
		
		System.out.println("\nEnter Launcher ID to Launch Missile from (one word):");
		String launcherID = in.next();
		Launcher launcher = launchers.get(launcherID);
		while ( launcher == null || !launcher.alive() ) {
			in.nextLine();
			if (launcher == null)
				System.out.println("\nERROR: Launcher ID doesn't exist");
			else
				System.out.println("\nERROR: Launcher is destroyed");
			System.out.println("Enter Launcher ID to Launch Missile from (one word) or # to cancel:");
			launcherID = in.next();
			if (launcherID.compareTo("#") == 0)
				return;
			
			launcher = launchers.get(launcherID);
		}
		
		String missileID = generateRandomMissileID();
		
		in.nextLine();
		System.out.println("Destination:");
		String destination = in.nextLine();
		
		boolean ok = false;
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
		
		// time = now
		Missile m = new Missile(missileID,destination,time,flyTime,damage,launcher,this);
		launcher.addMissile(m);
		m.start();

		System.out.println(	"\nMissile Successfully added and will Launch immediately!\n\n"
							+ m + "\n\t\tLauncher: " + launcher.getID() + "\n");
		
	}
	
	private void destroyLauncher() {
		
		if (launcherDestroyers.size() == 0) {
			System.out.println("\nERROR: no Launcher Destroyer available, please add one first");
			return;
		}
		
		printLauncherDestroyers();

		int size = launcherDestroyers.size();
		boolean ok = false;
		int choice = -1;
		while (!ok) {
			try {
				System.out.println("\nPlease select Launcher Destroyer (number) or -1 to cancel:");
				choice = in.nextInt();
				
				if (choice == -1)
					return;
				
				if ( (choice < 0) || (choice >= size) )
					throw new InputMismatchException();
				
				ok = true;
			} catch (InputMismatchException e) {
				System.out.println("\nERROR: not a valid choice");
				in.nextLine();
			}
		}
		
		LauncherDestroyer ld = launcherDestroyers.get(choice);
		
		printLaunchers();
		
		System.out.println("\nEnter Launcher ID to Destroy:");
		String lid = in.next();
		Launcher l = getLauncher(lid);

		while ( l == null || !l.alive() ) {
			if (l == null)
				System.out.println("\nLauncher doesn't exist");
			else
				System.out.println("\nLauncher already destroyed");
			
			System.out.println("Enter Launcher ID to Destroy (enter # to cancel):");
			lid = in.next();
			
			if (lid.compareTo("#") == 0)
				return;
			
			l = getLauncher(lid);
		}
		
		Target t = new Target(l, time);		// time = now
		ld.addTarget(t);
		
		System.out.println(	"\nLauncher Target Successfully added and will try " +
							"to destroy immediately!\n\n" + ld );
	}
	
	private void interceptMissile() {
		printIronDomes();
		
		System.out.println("\nEnter IronDome ID to Intercept Missile with (one word):");
		String irondID = in.next();
		IronDome irond = ironDomes.get(irondID);
		while ( irond == null ) {
			in.nextLine();
			System.out.println("\nERROR: IronDome ID doesn't exist");
			System.out.println("Enter IronDome ID to Intercept Missile with (one word) or # to cancel:");
			irondID = in.next();
			if (irondID.compareTo("#") == 0)
				return;
			
			irond = ironDomes.get(irondID);
		}
		
		printMissiles();
		
		System.out.println("\nEnter Missile ID to Intercept:");
		String mid = in.next();
		Missile m = getMissile(mid);

		while ( m == null || !m.onAir()) {
			if (m == null)
				System.out.println("\nMissile doesn't exist");
			else
				System.out.println("\nCan't Intercept Missile: Not On-Air");
			System.out.println("Enter Missile ID to Intercept (enter # to cancel):");
			mid = in.next();
			
			if (mid.compareTo("#") == 0)
				return;
			
			m = getMissile(mid);
		}
		
		Target t = new Target(m, time);		// time = now
		irond.addTarget(t);
		
		System.out.println(	"\nMissile Target Successfully added and will try " +
							"to intercept immediately!\n\n" + irond	);
	}

	private String generateRandomMissileID() {
		final int MIN = 1;
		final int MAX = 999;
		
		int rand = MIN + (int)(Math.random()*(MAX-MIN+1));
		String id = "M" + rand;
		
		Missile m = getMissile(id);
		
		while (m != null) {
			rand = MIN + (int)(Math.random()*(MAX-MIN+1));
			id = "M" + rand;
			m = getMissile(id);
		}
		
		return id;
	}

	private void printLaunchers() {
		System.out.println("\nLaunchers:");
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) {
			Launcher l = launcherIt.next();
			if (l.alive()) {
				System.out.print(l.getID());
				if (launcherIt.hasNext())
					System.out.print(" | ");
			}
		}
		System.out.println();
	}
	
	private void printMissiles() {
		System.out.println("\nMissiles:");
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) {
			Launcher l = launcherIt.next();
			Iterator<Missile> missileIt = l.getMissiles().iterator();
			while(missileIt.hasNext()) {
				Missile m = missileIt.next();
				if (m.onAir()) {
					System.out.print(m.getID());
					if (missileIt.hasNext() || launcherIt.hasNext())
						System.out.print(" | ");
				}
			}
		}
		System.out.println();
	}
	
	private void printIronDomes() {
		System.out.println("\nIron Domes:");
		Collection<IronDome> ironDomeCol = ironDomes.values();
		Iterator<IronDome> ironDomeIt = ironDomeCol.iterator();
		while(ironDomeIt.hasNext()) {
			System.out.print(ironDomeIt.next().getID());
			if (ironDomeIt.hasNext())
				System.out.print(" | ");
		}
		System.out.println();
	}
	
	private void printLauncherDestroyers() {
		System.out.println("\nLauncher Destroyers:");
		int size = launcherDestroyers.size();
		for (int i = 0 ; i < size ; i++)
			System.out.println( i + " :: " + launcherDestroyers.get(i).getType());
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	private void startInitThreads() {
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
	
	private void endWar(){
		
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
		
		Statistics.showStatistics(launchers, time);
		
		System.out.println("\n<<<<< WAR ENDED >>>>>\n");
	}
	
	public int getTime() {
		return time;
	}
	
	public boolean alive() {
		return alive;
	}
	
	public CountDownLatch getWarStartLatch() {
		return warStartLatch;
	}

	public void increaseInitThreadCount() {
		initThreadCount++;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	/** Return the Launcher if exist */
	public Launcher getLauncher(String launcherID) {
		return launchers.get(launcherID);
	}
	
	/** Search the Missile in all the Launchers and Return if exist */
	public Missile getMissile(String missileID) {
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


	private void cleanDir() {
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
	
	public void testHeap() {
		
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

	public void runWarTest() {
		
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


