package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.*;
import Utility.*;
import War.*;

public class IronDome extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	private War war;	// war that belongs to
	
	private int nextInterception;
	private boolean alive;
	
	private FileHandler fh = null;
	
	
	public IronDome(String id, Heap<Target> targetMissiles, War war) {
		super();
		this.id = id;
		this.targetMissiles = targetMissiles;
		this.war = war;
		
		setHandler();
	}
	
	public IronDome(String id, War war) {
		super();
		this.id = id;
		this.war = war;
		targetMissiles = new Heap<Target>(Target.targetComparator);
		
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler("logs/IronDome_" + id +"_Log.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter(war));
		fh.setFilter(new ObjectFilter(this));
		logger.addHandler(fh);
	}

	public void run() {
		logger.log(Level.INFO, "IronDome " + id + " created",this);
		try {
			// WarStartLatch - waiting for all the threads to start together
			// only if war hasn't start yet (pre-war setup)
			if (!war.alive()) {		
				war.getWarStartLatch().countDown();
				war.getWarStartLatch().await();
			}
			
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

					if ( war.getTime() >= nextInterception ) {

						synchronized (this) {
//							System.out.println(war.getTime() + ": IronDome " + id + " trying to intercept Missile " + m.getID());
							
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
		
//		war.decreaseThreadCount();
//		System.out.println(war.getTime() + ": war.decreaseThreadCount() - IronDome " + id + " done");
//		war.printThreadCount();
		
//		fh.close();
	}
	
	public String getID() {
		return id;
	}
	
	public boolean alive() {
		return alive;
	}
	
	private void interceptMissile(Missile m) {
		logger.log(	Level.INFO, "IronDome " + id + " >> " +
					"Trying to intercept Missile " + m.getID(), this);
		
		boolean succeed = false;
		
		synchronized (m) {
			succeed = m.intercept();
		}

		if (succeed)
			logger.log(	Level.INFO, "IronDome " + id + " >> Missile " +
						m.getID() + " was intercepted successfully", this	);
		else 
			logger.log(	Level.INFO, "IronDome " + id + " >> Missile " +
						m.getID() + " interception failed, Missile may not be On-Air", this);
	}
	
	public void addTarget(Target t) {
		synchronized (targetMissiles) {
			targetMissiles.add(t);
		}
	}
	
	public void end() {
		
		alive = false;
		
		try {
			if (isAlive())
				interrupt();
			
		} catch (IllegalMonitorStateException e) {
			
		}
		
		fh.close();
	}
	
	@Override
	public String toString() {
		String str = "---IronDome " + id;
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
