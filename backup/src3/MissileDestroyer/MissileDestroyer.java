package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import Launcher.*;
import Utility.*;
import War.*;

public class MissileDestroyer extends Thread {

	private String id;
	private Heap<Target> targetMissiles;
	
	private int nextInterception;
	
	private FileHandler fh = null;
	
	
	public MissileDestroyer(String id, Heap<Target> targetMissiles) {
		super();
		this.id = id;
		this.targetMissiles = targetMissiles;
		
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler("logs/MissileDestroyer_" + id +"_Log.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter());
		fh.setFilter(new ObjectFilter(this));
		War.getLogger().addHandler(fh);
	}

	public void run() {
		War.log(Level.INFO, "MissileDestroyer " + id + " created",this);
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			while (targetMissiles.getSize() > 0) {
				
				Target t = targetMissiles.getHead();
				Missile m = (Missile)(t.getTarget());
				nextInterception = t.getDestroyTime();
				
				while (War.getTime() < nextInterception)
					sleep(War.DELAY);
				
				synchronized (m) {
					if (m.inAir())
						interceptMissile(m);
					else
						interceptionFailed(m);
				}
				
				targetMissiles.remove();
			}
			War.log(Level.INFO, "MissileDestroyer " + id + " has no more targets",this);
			
		} catch (InterruptedException e) {
			
			
		}
		War.decreaseThreadCount();
		System.out.println("\nWar.decreaseThreadCount() - MissileDestroyer " + id + " done");
		fh.close();
	}
	
	public String getID() {
		return id;
	}
	
	private void interceptMissile(Missile m) {
		m.intercept();
		War.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
				m.getID() + " was intercepted successfully",this);
	}
	
	private void interceptionFailed(Missile m) {
		War.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
				m.getID() + " interception failed",this);
	}
	
	@Override
	public String toString() {
		String str = "---MissileDestroyer " + id;
		str += "\n\n\t---Missile Targets\n\n";
		java.util.Iterator<Target> it = targetMissiles.iterator();
		while(it.hasNext()) {
			Target t = it.next();
			Missile m = (Missile)t.getTarget();
			str += 	"\t\tMissile " + m.getID() + "\n\t\tDestroy Time: " +
					t.getDestroyTime() + "\n\n";
		}
		return str;
	}
	
}
