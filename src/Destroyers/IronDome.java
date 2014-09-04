package Destroyers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.Missile;
import Utility.*;
import War.War;


/** 
 * @author Ariel Levin
 * 
 * */
public class IronDome extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	private War war;	// war that belongs to
	// War is stored in order to gain access to War Time and if War is still Alive
	
	private int nextInterception;
	private boolean alive;
	
	private FileHandler fh = null;
	
	/** Constructor with Target Heap input */
	public IronDome(String id, Heap<Target> targetMissiles, War war) {
		super();
		this.id = id;
		this.targetMissiles = targetMissiles;
		this.war = war;
		
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Constructor without Target Heap input - Creates new */
	public IronDome(String id, War war) {
		super();
		this.id = id;
		this.war = war;
		targetMissiles = new Heap<Target>(Target.targetComparator);
		
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}

	/** Sets the Logger File Handler */
	private void setHandler() {
		try {
			fh = new FileHandler("logs/IronDome_" + this.id +"_Log.txt",false);
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
		logger.log(Level.INFO, "IronDome " + this.id + " created",this);
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
					if (targetMissiles.getSize() > 0) {	// if there are targets in the heap
						t = targetMissiles.getHead();
						m = (Missile)(t.getTarget());
					}
					else {
						synchronized (this) {
							wait();
						}
					}
				}

				if (m != null) {		// if missile was found on the Heap's head
					nextInterception = t.getDestroyTime();

					if ( war.getTime() >= nextInterception ) {

						synchronized (this) {

							if (t.isSetDuringWar())
								interceptMissileWithExtras(m);
							else
								interceptMissileNormally(m);
							
							targetMissiles.remove();
						}

					}
				}
					
				sleep(War.DELAY);
			}
			
		} catch (InterruptedException e) {
			
		}
		
		alive = false;
	}
	
	/** Returns the Iron Dome ID */
	public String getID() {
		return id;
	}
	
	/** Returns if the Iron Dome is Alive */
	public boolean alive() {
		return alive;
	}
	
	/** Intercept the input Missile normally (immediately).
	 * Used when the interception is set in Pre-War Setup */
	private void interceptMissileNormally(Missile m) {
		logger.log(	Level.INFO, "IronDome " + this.id + " >> " +
					"Trying to intercept Missile " + m.getID(), this);

		boolean succeed = false;

		synchronized (m) {
			succeed = m.intercept();
		}

		if (succeed)
			logger.log(	Level.INFO, "IronDome " + this.id + " >> Missile " +
						m.getID() + " was intercepted successfully", this	);
		else 
			logger.log(	Level.INFO, "IronDome " + this.id + " >> Missile " +
						m.getID() + " interception failed, Missile may not be On-Air", this);

	}
	
	/** Intercept the input Missile with action delay (interception duration) and success chance.
	 * Used when the interception is set after War has started */
	private void interceptMissileWithExtras(Missile m) {
		
		// random number between 3 and 10, for interception duration
		int rand = 3 + (int)(Math.random()*8);

		logger.log(	Level.INFO, "IronDome " + this.id + " >> " +
					"Trying to intercept Missile " + m.getID() + LogFormatter.newLine +
					"Interception duration time: (" + rand + ") time units", this);

		try {
			synchronized (this) {
				sleep(rand*War.DELAY);
			}
		} catch (InterruptedException e) {

		}
		
		// random number between 1 and 10, for success chance
		int randSuccess = 1 + (int)(Math.random()*10);
		
		// 1-7: Success , 8-10: Miss
		if (randSuccess >= 8 && randSuccess <= 10) {
			logger.log(	Level.INFO, "IronDome " + this.id + " >> Missile " +
						m.getID() + " interception failed - Missed Missile", this);
			return;
		}

		boolean succeed = false;

		synchronized (m) {
			succeed = m.intercept();
		}

		if (succeed)
			logger.log(	Level.INFO, "IronDome " + this.id + " >> Missile " +
						m.getID() + " was intercepted successfully", this);
		else 
			logger.log(	Level.INFO, "IronDome " + this.id + " >> Missile " +
						m.getID() + " interception failed - Missile may not be On-Air", this);
	}
	
	/** Add the input Target to the Iron Dome's Target Heap */
	public void addTarget(Target t) {
		synchronized (targetMissiles) {
			targetMissiles.add(t);
		}
		notify();	// notify in case was on wait because of empty heap
	}
	
	/** End the Iron Dome and close the File Handler, used on War class, in endWar() Method */
	public void end() {
		
		alive = false;
		
		try {	// surround with try because the thread might already be dead
			notify();	// notify in case is on wait			
		} catch (IllegalMonitorStateException e) {}
		
		try {	// surround with try because the thread might already be dead
			interrupt();
		} catch (SecurityException e) {}
		
		fh.close();
	}
	
	@Override
	public String toString() {
		String str = "---IronDome " + this.id;
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
