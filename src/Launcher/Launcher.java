package Launcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import Utility.*;
import War.War;


/** 
 * @author Ariel Levin
 * 
 * */
public class Launcher extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private boolean canBeHidden;
	private Heap<Missile> missiles;
	private War war;	// war that belongs to
	// War is stored in order to gain access to War Time and if War is still Alive
	
	// if the launcher canBeHidden, it will be exposed after launching a missile, while it on-air
	private boolean isHidden;
	private boolean alive;
	private boolean waitingForMissiles;	// if heap is empty and the launcher waiting for missiles

	private int missileLaunchCount = 0;
	private int missileInterceptedCount = 0;
	private int missileHitCount = 0;
	private boolean launcherDestroyed = false;
	private int totalDamage = 0;
	
	private FileHandler fh = null;

	
	/** Constructor with Missile Heap input */
	public Launcher(String id, boolean canBeHidden, Heap<Missile> missiles, War war) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
		this.missiles = missiles;
		this.war = war;
		
		isHidden = canBeHidden;
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Constructor without Missile Heap input - Creates new */
	public Launcher(String id, boolean canBeHidden, War war) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
		this.war = war;
		missiles = new Heap<Missile>(Missile.missileComparator);
		
		isHidden = canBeHidden;
		setHandler();
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	/** Sets the Logger File Handler */
	private void setHandler() {
		try {
			fh = new FileHandler("logs/Launcher_" + this.id + "_Log.txt",false);
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
		if (canBeHidden)
			logger.log(Level.INFO, "Launcher " + this.id + " created and can be hidden", this);
		else
			logger.log(Level.INFO, "Launcher " + this.id + " created and cannot be hidden", this);
			
		logPreWarMissiles();
		
		try {
			// WarStartLatch - waiting for all the threads to start together
			// only if war hasn't start yet (pre-war setup)
			if (!war.alive()) {		
				war.getWarStartLatch().countDown();
				war.getWarStartLatch().await();
			}
			
			alive = true;
			
			while (alive) {
				
				Missile m = null;
				
				synchronized (missiles) {
					if (missiles.getSize() > 0)		// if there are missiles in the heap
						m = missiles.getHead();
					else {
						waitingForMissiles = true;
						synchronized (this) {
							wait();
						}
						waitingForMissiles = false;
					}
				}
				
				if (m != null) {	// if missile was found on the Heap's head
					int nextLaunch = m.getLaunchTime();
					
					if (war.getTime() >= nextLaunch) {
						logger.log(Level.INFO, "Launcher " + this.id + " >> Trying to launch Missile " + m.getID(),this);

						synchronized (this) {
							launchMissile(m);

							// Missile in the air, waiting for hit or intercept
							wait();
							
							missiles.remove();
						}
						
					}
				}

				sleep(War.DELAY);
			}

		} catch (InterruptedException e) {
			
		}
		
		alive = false;
	}
	
	/** Log all the Missiles in the Heap (if the War hasn't started yet) */
	private void logPreWarMissiles() {
		Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() +
						" created" + LogFormatter.newLine + "Destination: " + m.getDestination() +
						" , Launch Time: " + m.getLaunchTime()  + " , Estimated Land Time: " +
						(m.getLaunchTime()+m.getFlyTime()), this);
		}
	}
	
	/** Returns the Launcher ID */
	public String getID() {
		return id;
	}

	/** Returns if the Launcher is Currently Hidden */
	public boolean isHidden() {
		return isHidden;
	}
	
	/** Returns if the Launcher is Alive */
	public boolean alive() {
		return alive;
	}
	
	/** Return all the Missiles Heap */
	public Heap<Missile> getMissiles() {
		// we use this method in War class
		return missiles;
	}

	/** Return the Missile from the Launcher by ID, return null if not found */
	public Missile getMissile(String id) {
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			if (id.compareTo(m.getID()) == 0)
				return m;
		}
		return null;
	}
	
	/** Return the amount of Launches made from this Launcher */
	public int getMissileLaunchCount() {
		return missileLaunchCount;
	}

	/** Return the amount of Intercepted Missiles that belongs to this Launcher */
	public int getMissileInterceptedCount() {
		return missileInterceptedCount;
	}

	/** Return the amount of Missiles Successfully Hit that belongs to this Launcher */
	public int getMissileHitCount() {
		return missileHitCount;
	}

	/** Return if the Launcher Destroyed */
	public boolean isLauncherDestroyed() {
		return launcherDestroyed;
	}

	/** Return the Total Damage made from Missiles Hits that belong to this Launcher */
	public int getTotalDamage() {
		return totalDamage;
	}

	/** Add the input Missile to the Launcher's Missile Heap */
	public void addMissile(Missile m) {
		
		// logging the missile creation if launcher is already/still alive
		if (alive) {
			logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() +
						" created" + LogFormatter.newLine + "Destination: " + m.getDestination() +
						" , Launch Time: " + m.getLaunchTime()  + " , Estimated Land Time: " +
						(m.getLaunchTime()+m.getFlyTime()), this );
		}
		
		synchronized (missiles) {
			missiles.add(m);
		}
		
		// notify in case was on wait because of empty heap
		if (waitingForMissiles)
			notify();
	}
	
	/** Launch the input Missile */
	private void launchMissile(Missile m) {
		
		synchronized (m) {
			m.launch();
		}
		
		logger.log(Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() + " launched!",this);
		missileLaunchCount++;
		
		if (canBeHidden) {
			logger.log(	Level.INFO, "Launcher " + this.id + " is now not hidden and exposed " +
						"for attacks " + LogFormatter.newLine + "Exposed while Missile " + m.getID() +
						" On-Air: (" + m.getFlyTime() + ") time units", this);
			isHidden = false;
		}
	}
	
	/** Method called by input Missile, to let the Launcher know it Hit the target Successfully.
	 * If Missile was set during war (not on Pre-Setup), then it has Success Chance of Hitting */
	public synchronized void missileHit(Missile m, boolean setDuringWar) {

		if (setDuringWar) {
			// random number between 1 and 10, for success chance
			int randSuccess = 1 + (int)(Math.random()*10);
			
			// 1-7: Success , 8-10: Miss
			if (randSuccess >= 8 && randSuccess <= 10) {
				logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() +
							" Missed destination " + m.getDestination() + "!" , this);
				
				notify();	// free the launcher from wait
				return;
			}
		}
		
		logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() +
					" Successfully hit " + m.getDestination() + "!" +
					LogFormatter.newLine + "Total damage: " + m.getDamage(), this);

		if (canBeHidden && alive) {
			isHidden = true;
			logger.log(Level.INFO, "Launcher " + this.id + " is now hidden and safe",this);
		}
		
		notify();	// free the launcher from wait
		
		missileHitCount++;
		totalDamage += m.getDamage();
	}
	
	/** Method called by input Missile, to let the Launcher know it was Intercepted on input time */
	public synchronized void missileIntercept(Missile m, int time) {

		logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " + m.getID() +
					" was Intercepted!" + LogFormatter.newLine +
					"Interception time: " + time, this	);

		if (canBeHidden && alive) {
			isHidden = true;
			logger.log(Level.INFO, "Launcher " + this.id + " is now hidden and safe",this);
		}
		
		notify();	// free the launcher from wait
		
		missileInterceptedCount++;
	}

	/** Method called by Launcher Destroyer, to Destroy the Launcher on input time.
	 * Returns True or False if Succeeded or not */
	public boolean destroyLauncher(int time) {

		synchronized (this) {
			if (!alive || isHidden)
				return false;

			alive = false;
			notify();	// free the launcher if was on wait
			
			// let all the Missiles that belongs to the Launcher that it was Destroyed
			Iterator<Missile> it = missiles.iterator();
			while (it.hasNext()) {
				Missile m = it.next();
				if (!m.onAir()) {
					m.launcherDestroyed();
					logger.log(	Level.INFO, "Launcher " + this.id + " >> Missile " +
								m.getID() + " was destroyed because of Launcher Destruction",this);
				}
			}

			logger.log(	Level.INFO, "Launcher " + this.id + " was destroyed!" +
						LogFormatter.newLine + "Destruction time: " + time, this);

			launcherDestroyed = true;
			
			return true;
		}
	}

	/** End the Launcher and close the File Handler, used on War class, in endWar() Method */
	public void end() {
		
		alive = false;
		
		try {	// surround with try because the thread might already be dead
			notify();	// notify in case is on wait			
		} catch (IllegalMonitorStateException e) {}
		
		try {	// surround with try because the thread might already be dead
			interrupt();
		} catch (SecurityException e) {}

		Iterator<Missile> it = missiles.iterator();
		while (it.hasNext())
			it.next().end();

		fh.close();
	}

	@Override
	public String toString() {
		String str =	"---Launcher " + this.id + "\n\n\tcanBeHidden: " +
						canBeHidden + "\n\n\t---Missiles\n\n";
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
	}
	
}

