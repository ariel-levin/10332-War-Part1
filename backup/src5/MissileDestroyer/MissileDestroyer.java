package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.*;
import Utility.*;
import War.*;

public class MissileDestroyer extends Thread {

	private static Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	
	private int nextInterception;
	private boolean alive;
	
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
		logger.addHandler(fh);
	}

	public void run() {
		logger.log(Level.INFO, "MissileDestroyer " + id + " created",this);
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			alive = true;
			
			while (alive) {

				Target t = null;
				Missile m = null;
				
				synchronized (targetMissiles) {
					if (targetMissiles.getSize() > 0) {
						t = targetMissiles.getHead();
						m = (Missile)(t.getTarget());
					}
				}

				if (t != null) {
					nextInterception = t.getDestroyTime();

					if ( War.getTime() >= nextInterception ) {

						synchronized (this) {
//							System.out.println(War.getTime() + ": MissileDestroyer " + id + " trying to intercept Missile " + m.getID());
							
							interceptMissile(m);
							
							targetMissiles.remove();
						}

					}
				}
					
				sleep(War.DELAY);
			}
			
		} catch (InterruptedException e) {
			
			
		}
		alive = false;
		
//		War.decreaseThreadCount();
//		System.out.println(War.getTime() + ": War.decreaseThreadCount() - MissileDestroyer " + id + " done");
//		War.printThreadCount();
		
//		fh.close();
	}
	
	public String getID() {
		return id;
	}
	
	public boolean alive() {
		return alive;
	}
	
	private void interceptMissile(Missile m) {
		logger.log	(Level.INFO, "MissileDestroyer " + id +
					" >> Trying to intercept Missile " + m.getID(),this);
		
		synchronized (m) {
			if ( (m.onAir()) && (War.getTime() <= m.getLaunchTime() + m.getFlyTime()) ) {
				m.intercept();
				logger.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
							m.getID() + " was intercepted successfully",this);
			} else {
				logger.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
							m.getID() + " interception failed, Missile not On-Air",this);
			}
		}
	}
	
	public void kill() {
		try {
			if (isAlive())
				notify();
		} catch (IllegalMonitorStateException e) {}
		
		alive = false;
		fh.close();
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
