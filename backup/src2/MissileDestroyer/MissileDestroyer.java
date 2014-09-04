package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.Missile;
import Launcher.Target;
import Utility.Heap;
import Utility.LogFormatter;
import War.War;

public class MissileDestroyer extends Thread {

	private static Logger log = Logger.getLogger("MissileDestroyerLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	
	private int nextInterception;
	
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
		System.out.println("MissileDestroyer " + id + " created");
	}

	public void run() {
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			while (targetMissiles.getSize() > 0) {
				
				Target t = targetMissiles.getHead();
				Missile m = (Missile)(t.getTarget());
				nextInterception = t.getDestroyTime();
				
				while (War.getTime() < nextInterception)
					sleep(War.DELAY);
				
				interceptMissile(m);
				
				targetMissiles.remove();
			}
			log.log(Level.INFO, "MissileDestroyer " + id + " has no more targets");
			System.out.println("MissileDestroyer " + id + " has no more targets");
			
		} catch (InterruptedException e) {
			
			
		}
		log.getHandlers()[0].close();
	}
	
	public String getID() {
		return id;
	}
	
	private void interceptMissile(Missile m) {
		m.intercept();
		log.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
				m.getID() + " was intercepted successfully");
		System.out.println	("MissileDestroyer " + id + " >> Missile " +
							m.getID() + " was intercepted successfully");
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
