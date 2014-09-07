package Destroyers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.Launcher;
import Utility.*;
import War.War;


/** 
 * @author Ariel Levin
 * 
 * */
public class LauncherDestroyer extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private static int idGenerator = 0;
	
	private int id;
	private String type;
	private Heap<Target> targetLaunchers;
	private War war;	// war that belongs to
	// War is stored in order to gain access to War Time and if War is still Alive
	
	private boolean alive = false;
	private boolean occupied = false;
	private String ldstring;	// "LauncherDestroyer <id> <type>"
	
	private FileHandler fh = null;
	

	public LauncherDestroyer(String type, War war) {
		super();
		this.id = ++idGenerator;
		this.type = type;
		this.war = war;
		targetLaunchers = new Heap<Target>(Target.targetComparator);
		ldstring = "LauncherDestroyer " + this.id + " : " + this.type;
		
		setHandler();
		logger.log(	Level.INFO, ldstring + " created",this);
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Sets the Logger File Handler */
	private void setHandler() {
		try {
			fh = new FileHandler(	"logs/LauncherDestroyer_" + this.id + "_" +
									type + "_Log.txt", false	);
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
				
				if (targetLaunchers.getSize() > 0) {	// if there are targets in the heap
					t = targetLaunchers.getHead();
					l = (Launcher)(t.getTarget());
				}
				else {
					synchronized (this) {
						wait();
					}
				}
				
				// if launcher was found on the Heap's head
				if ( t != null && l != null ) {

					if ( war.getTime() >= t.getDestroyTime() ) {

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
	
	/** Returns if the Launcher Destroyer is already destroying other Launcher */
	public boolean isOccupied() {
		return occupied;
	}

	/** Destroy the input Launcher normally (immediately).
	 * Used when the destruction is set in Pre-War Setup */
	private void destroyLauncherNormally(Launcher l) {
		logger.log(	Level.INFO, ldstring + " >> Trying to destroy Launcher " + l.getID(), this);

		boolean succeed = false;

		synchronized (l) {
			succeed = l.destroyLauncher(war.getTime());
		}

		if (succeed)
			logger.log(	Level.INFO, ldstring + " >> Launcher " + l.getID() +
						" was destroyed successfully", this);
		else
			logger.log(	Level.INFO, ldstring + " >> Launcher " + l.getID() +
						" destruction failed " + LogFormatter.newLine +
						"Hidden or already Destroyed", this);
	}
	
	/** Destroy the input Launcher with action delay (destruction duration) and success chance.
	 * Used when the destruction is set after War has started */
	private void destroyLauncherWithExtras(Launcher l) {

		// random number between 3 and 10, for destruction duration
		int randDuration = 3 + (int)(Math.random()*8);

		logger.log(	Level.INFO, ldstring + " >> Trying to destroy Launcher " + l.getID() +
					LogFormatter.newLine + "Destruction duration time: (" + randDuration +
					") time units", this);

		occupied = true;
		try {
			synchronized (this) {
				sleep(randDuration*War.DELAY);
			}
		} catch (InterruptedException e) {}
		occupied = false;

		// random number between 1 and 10, for success chance
		int randSuccess = 1 + (int)(Math.random()*10);
		
		// 1-7: Success , 8-10: Miss
		if (randSuccess >= 8 && randSuccess <= 10) {
			logger.log(	Level.INFO, ldstring + " >> Launcher " + l.getID() +
						" destruction failed " + LogFormatter.newLine +
						"Missed Launcher", this);
			return;
		}
		
		boolean succeed = false;

		synchronized (l) {
			succeed = l.destroyLauncher(war.getTime());
		}

		if (succeed)
			logger.log(	Level.INFO, ldstring + " >> Launcher " + l.getID() +
						" was destroyed successfully", this);
		else
			logger.log(	Level.INFO, ldstring + " >> Launcher " + l.getID() +
						" destruction failed " + LogFormatter.newLine +
						"Hidden or already Destroyed", this);
	}
	
	/** Add the input Target to the Launcher Destroyer's Target Heap */
	public synchronized void addTarget(Target t) {
		
		Launcher l = (Launcher)t.getTarget();
		logger.log(	Level.INFO, ldstring + " >> Target: Launcher " + l.getID() +
					" , Destroy time: " + t.getDestroyTime() , this );
		
		targetLaunchers.add(t);
		
		if (alive)
			notify();	// notify in case was on wait because of empty heap
	}
	
	/** End the Launcher Destroyer and close the File Handler, used on War class, in endWar() Method */
	public void end() {
		
		alive = false;
		
		try {	// surround with try because the thread might already be dead
			interrupt();
		} catch (SecurityException e) {}
		
		fh.close();
	}
	
	public String toString() {
		String str = "---" + ldstring + "\n\n\t---Launcher Targets\n\n";
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
