package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.Target;
import Utility.Heap;
import Utility.LogFormatter;
import War.War;

public class MissileDestroyer extends Thread {

	private static Logger log = Logger.getLogger("MissileDestroyerLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	
	private int sleepTime;
	
	static {
		log.setUseParentHandlers(false);
		FileHandler fh = null;
		try {
			fh = new FileHandler("logs/MissileDestroyerLog.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter());
		log.addHandler(fh);
	}
	
	public MissileDestroyer(String id, Heap<Target> targetMissiles) {
		super();
		this.id = id;
		this.targetMissiles = targetMissiles;
		log.log(Level.INFO, "MissileDestroyer " + id + " created");
	}

	public void run() {
		try {
//			System.out.println(War.getTime() + " :: MissileDestroyer " + id + " created");
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.getHandlers()[0].close();
	}
	
	public String getID() {
		return id;
	}
	
	@Override
	public String toString() {
		String str = "---MissileDestroyer " + id;
		str += "\n\n\t---Missile Targets\n\n";
		java.util.Iterator<Target> it = targetMissiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
	}
	
}
