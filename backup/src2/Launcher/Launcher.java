package Launcher;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Utility.*;
import War.*;

public class Launcher extends Thread {

	private static Logger log = Logger.getLogger("LauncherLogger");
	
	private String id;
	private boolean isHidden;
	private Heap<Missile> missiles;
	
	private int nextLaunch;
	
//	private Lock lock = new ReentrantLock();
//	private Condition 
	
	static {
		log.setUseParentHandlers(false);
		FileHandler fh = null;
		try {
			fh = new FileHandler("logs/LauncherLog.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter());
		log.addHandler(fh);
	}
	
	public Launcher(String id, boolean isHidden, Heap<Missile> missiles) {
		super();
		this.id = id;
		this.isHidden = isHidden;
		this.missiles = missiles;
		
		log.log(Level.INFO, "Launcher " + id + " created");
		System.out.println("Launcher " + id + " created");
		logInitMissiles();
	}

	private void logInitMissiles() {
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created :" +
						LogFormatter.newLine + "Destination: " + m.getDestination() + " , " + 
						"Launch Time: " + m.getLaunchTime()  + " , " +
						"Estimated Land Time: " + (m.getLaunchTime()+m.getFlyTime());
			log.log(Level.INFO, s);
		}
	}
	
	public void run() {
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			while (missiles.getSize() > 0) {
				
				Missile m = missiles.getHead();
				nextLaunch = m.getLaunchTime();

				while (War.getTime() < nextLaunch)
					sleep(War.DELAY);

				synchronized (this) {
					launchMissile(m);
					isHidden = false;
					wait();
				}
				missiles.remove();
			}
			log.log(Level.INFO, "Launcher " + id + " has no more missiles");
			System.out.println("Launcher " + id + " has no more missiles");

		} catch (InterruptedException e) {
			
			
		} 
		log.getHandlers()[0].close();
	}
	
	private void launchMissile(Missile m) {
		log.log(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() + " launched");
		System.out.println("Launcher " + id + " >> Missile " + m.getID() + " launched");
		m.launch();
	}
	
	public String getID() {
		return id;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public Heap<Missile> getMissiles() {
		return missiles;
	}

	public Missile getMissile(String id) {
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			if (id.compareTo(m.getID()) == 0)
				return m;
		}
		return null;
	}
	
	public synchronized void missileHit(Missile m) {
		if (m == missiles.getHead()) {	// makes sure it's the right missile
			String s = 	"Missile " + m.getID() + " Successfully hit " +
					m.getDestination() + "!" + LogFormatter.newLine +
					"Total damage: " + m.getDamage();
			log.log(Level.INFO, s);
			System.out.println(s);
			notify();
		}
	}
	
	public synchronized void missileIntercept(Missile m, int time) {
		if (m == missiles.getHead()) {	// makes sure it's the right missile
			String s = 	"Missile " + m.getID() + " was Intercepted!" +
						LogFormatter.newLine + "Interception time: " + time;
			log.log(Level.INFO, s);
			System.out.println(s);
			notify();
		}
	}
	
	public synchronized void destroyLauncher(int time) {
		if (!isHidden) {
			String s = 	"Launcher " + id + " was destroyed!" +
						LogFormatter.newLine + "Destruction time: " + time;
			log.log(Level.INFO, s);
			System.out.println(s);
			interrupt();
		}
	}
	
	@Override
	public String toString() {
		String str = "---Launcher " + id + "\n\n\tisHidden: " + isHidden;
		str += "\n\n\t---Missiles\n\n";
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n";
		return str;
		
	}
	
}
