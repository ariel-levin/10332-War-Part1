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
		War.getLogger().addHandler(fh);
	}

	public void run() {
		War.log(Level.INFO, "MissileDestroyer " + id + " created",this);
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
		War.decreaseThreadCount();
		System.out.println(War.getTime() + ": War.decreaseThreadCount() - MissileDestroyer " + id + " done");
		War.printThreadCount();
		fh.close();
	}
	
	public String getID() {
		return id;
	}
	
	public boolean alive() {
		return alive;
	}
	
	private void interceptMissile(Missile m) {
		synchronized (m) {
			if (m.inAir()) {
				m.intercept();
				War.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
						m.getID() + " was intercepted successfully",this);
			} else {
				War.log	(Level.INFO, "MissileDestroyer " + id + " >> Missile " +
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
