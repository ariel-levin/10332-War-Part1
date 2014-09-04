package Launcher;

import java.io.IOException;
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
		
		if (missiles.getSize() > 0)
			nextLaunch = missiles.getHead().getLaunchTime();
		
		log.log(Level.INFO, "Launcher " + id + " created");
		logInitMissiles();
	}

	private void logInitMissiles() {
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created :" +
						"\r\n\t\t\tDestination: " + m.getDestination() + " , " + 
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
				
				while (War.getTime() < nextLaunch);
				Missile m = missiles.getHead();
				
				
			}
			

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.getHandlers()[0].close();
	}
	
	public String getID() {
		return id;
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
