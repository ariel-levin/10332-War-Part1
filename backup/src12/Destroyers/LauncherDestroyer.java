/* Ariel Levin */

package Destroyers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import Launcher.Launcher;
import Utility.*;
import War.War;


public class LauncherDestroyer extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private static int idGenerator = 0;
	
	private int id;
	private String type;
	private Heap<Target> targetLaunchers;
	private War war;	// war that belongs to
	// War is stored in order to gain access to War Time and if War is still Alive
	
	private int nextDestroy;
	private boolean alive;
	
	private FileHandler fh = null;
	
	/** Constructor with Target Heap input */
	public LauncherDestroyer(String type, Heap<Target> targetLaunchers, War war) {
		super();
		this.id = ++idGenerator;
		this.type = type;
		this.targetLaunchers = targetLaunchers;
		this.war = war;
		
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Constructor without Target Heap input - Creates new */
	public LauncherDestroyer(String type, War war) {
		super();
		this.id = ++idGenerator;
		this.type = type;
		this.war = war;
		targetLaunchers = new Heap<Target>(Target.targetComparator);
		
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Sets the Logger File Handler */
	private void setHandler() {
		try {
			fh = new FileHandler(	"logs/LauncherDestroyer_" + this.id + "_" +
									type +"_Log.txt", false	);
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
		logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " " +
					this.getClass().getSimpleName() + " created",this);
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
				Launcher l = null;
				
				synchronized (targetLaunchers) {
					if (targetLaunchers.getSize() > 0) {	// if there are targets in the heap
						t = targetLaunchers.getHead();
						l = (Launcher)(t.getTarget());
					}
				}
				
				if (l != null) {		// if launcher was found on the Heap's head
					nextDestroy = t.getDestroyTime();

					if ( war.getTime() >= nextDestroy ) {

						synchronized (this) {
							
							if (t.isSetDuringWar())
								destroyLauncherWithExtras(l);
							else
								destroyLauncherNormally(l);
							
							targetLaunchers.remove();
						}
					}
				}

				sleep(War.DELAY);
			}
			
		} catch (InterruptedException e) {
			
		}
		
		alive = false;
	}
	
	/** Returns the Launcher Destroyer ID */
	public int getID() {
		return id;
	}

	/** Returns the Launcher Destroyer Type */
	public String getType() {
		return type;
	}

	/** Returns if the Launcher Destroyer is Alive */
	public boolean alive() {
		return alive;
	}
	
	/** Destroy the input Launcher normally (immediately).
	 * Used when the destruction is set in Pre-War Setup */
	private void destroyLauncherNormally(Launcher l) {
		logger.log(	Level.INFO, "LauncherDestroyer " + this.id +
					" >> Trying to destroy Launcher " + l.getID(), this	);

		boolean succeed = false;

		synchronized (l) {
			succeed = l.destroyLauncher(war.getTime());
		}

		if (succeed)
			logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " >> Launcher " +
						l.getID() + " was destroyed successfully",this	);
		else
			logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " >> Launcher " +
						l.getID() + " destruction failed - hidden or already destroyed", this);
	}
	
	/** Destroy the input Launcher with action delay (destruction duration) and success chance.
	 * Used when the destruction is set after War has started */
	private void destroyLauncherWithExtras(Launcher l) {

		// random number between 3 and 10, for destruction duration
		int randDuration = 3 + (int)(Math.random()*8);

		logger.log(	Level.INFO, "LauncherDestroyer " + this.id +
					" >> Trying to destroy Launcher " + l.getID() + LogFormatter.newLine +
					"Destruction duration time: (" + randDuration + ") time units", this);

		try {
			synchronized (this) {
				sleep(randDuration*War.DELAY);
			}
		} catch (InterruptedException e) {

		}

		// random number between 1 and 10, for success chance
		int randSuccess = 1 + (int)(Math.random()*10);
		
		// 1-7: Success , 8-10: Miss
		if (randSuccess >= 8 && randSuccess <= 10) {
			logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " >> Launcher " +
						l.getID() + " destruction failed - Missed Launcher", this);
			return;
		}
		
		boolean succeed = false;

		synchronized (l) {
			succeed = l.destroyLauncher(war.getTime());
		}

		if (succeed)
			logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " >> Launcher " +
						l.getID() + " was destroyed successfully", this);
		else
			logger.log(	Level.INFO, "LauncherDestroyer " + this.id + " >> Launcher " +
						l.getID() + " destruction failed - hidden or already destroyed", this);
	}
	
	/** Add the input Target to the Launcher Destroyer's Target Heap */
	public void addTarget(Target t) {
		synchronized (targetLaunchers) {
			targetLaunchers.add(t);
		}
	}
	
	/** End the Launcher Destroyer and close the File Handler, used on War class, in endWar() Method */
	public void end() {
		
		alive = false;
		
		try {
			if (isAlive())
				interrupt();
			
		} catch (IllegalMonitorStateException e) {
			
		}
		
		fh.close();
	}
	
	public String toString() {
		String str = 	"---LauncherDestroyer " + this.id + " : " + type +
						"\n\n\t---Launcher Targets\n\n";
		java.util.Iterator<Target> it = targetLaunchers.iterator();
		while(it.hasNext()) {
			Target t = it.next();
			Launcher l = (Launcher)t.getTarget();
			str += 	"\t\tLauncher " + l.getID() + "\n\t\tDestroy Time: " +
			t.getDestroyTime() + "\n\n";
		}
		return str;
	}
	
}
