package Launcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Utility.*;
import War.*;

public class Launcher extends Thread {

	/* Comparator used to sort the missiles in the Minimum Heap, by Launch Time */
	public static final java.util.Comparator<Missile> missileComparator =
							new java.util.Comparator<Missile>() {
		@Override
		public int compare(Missile m1, Missile m2) {
			return ( (Integer)m1.getLaunchTime() ).compareTo(m2.getLaunchTime())*(-1);
		}
	};
	
	private Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private boolean canBeHidden;
	private Heap<Missile> missiles;
	private War war;	// war that belongs to
	
	private int nextLaunch;
	private boolean alive;
	
	private final int exposureTime = 5;	// how long will be exposed after launch
	private int exposedTime;	// keeps the time the exposure started
	private boolean isHidden;
	
	private int numMissileLaunch = 0;
	private int numMissileIntercepted = 0;
	private int numMissileHit = 0;
	private int numLauncherDestroyed = 0;
	private int totalDamage = 0;
	
	private FileHandler fh = null;

	
	public Launcher(String id, boolean canBeHidden, Heap<Missile> missiles, War war) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
		this.missiles = missiles;
		this.war = war;
		
		isHidden = canBeHidden;
		setHandler();
	}
	
	public Launcher(String id, boolean canBeHidden, War war) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
		this.war = war;
		missiles = new Heap<Missile>(missileComparator);
		
		isHidden = canBeHidden;
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler("logs/Launcher_" + id +"_Log.txt",false);
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
			logger.log(Level.INFO, "Launcher " + id + " created and can be hidden", this);
		else
			logger.log(Level.INFO, "Launcher " + id + " created and cannot be hidden", this);
			
		logInitMissiles();
		
		try {
			// WarStartLatch - waiting for all the threads to start together
			// only if war hasn't start yet (pre-war setup)
			if (!war.alive()) {		
				war.getWarStartLatch().countDown();
				war.getWarStartLatch().await();
			}
			
			alive = true;
			
			while (alive) {
				
				checkHidden();	// checks if launcher should be hidden or not
				
				Missile m = null;
				
				synchronized (missiles) {
					if (missiles.getSize() > 0)
						m = missiles.getHead();
				}
				
				if (m != null) {
					nextLaunch = m.getLaunchTime();
					
					if (war.getTime() >= nextLaunch) {
						logger.log(Level.INFO, "Launcher " + id + " >> Trying to launch Missile " + m.getID(),this);

						synchronized (this) {
							launchMissile(m);
							
//							System.out.println(war.getTime() + ": Launcher " + id + " entering wait");
							
							// Missile in the air, waiting for hit or intercept
							wait();
//							System.out.println(war.getTime() + ": Launcher " + id + " leave wait");
							
							missiles.remove();
						}
						
					}
				}

				sleep(War.DELAY);
			}

		} catch (InterruptedException e) {
			
			
		}
		alive = false;
		
//		war.decreaseThreadCount();
//		System.out.println(war.getTime() + ": war.decreaseThreadCount() - Launcher " + id + " done");
//		war.printThreadCount();
		
//		fh.close();
	}
	
	private void logInitMissiles() {
		Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created" +
						LogFormatter.newLine + "Destination: " + m.getDestination() + " , " + 
						"Launch Time: " + m.getLaunchTime()  + " , " +
						"Estimated Land Time: " + (m.getLaunchTime()+m.getFlyTime());
			logger.log(Level.INFO, s, this);
		}
	}
	
	public String getID() {
		return id;
	}

	public boolean isHidden() {
		return isHidden;
	}
	
	public boolean alive() {
		return alive;
	}
	
	public Heap<Missile> getMissiles() {
		return missiles;
	}

	public Missile getMissile(String id) {
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			if (id.compareTo(m.getID()) == 0)
				return m;
		}
		return null;
	}
	
	public int getNumMissileLaunch() {
		return numMissileLaunch;
	}

	public int getNumMissileIntercepted() {
		return numMissileIntercepted;
	}

	public int getNumMissileHit() {
		return numMissileHit;
	}

	public int getNumLauncherDestroyed() {
		return numLauncherDestroyed;
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public void addMissile(Missile m) {
		
		// logging the missile creation if launcher already alive
		// else it will be logged in 'logInitMissiles()' 
		if (alive) {
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created" +
						LogFormatter.newLine + "Destination: " + m.getDestination() + " , " + 
						"Launch Time: " + m.getLaunchTime()  + " , " +
						"Estimated Land Time: " + (m.getLaunchTime()+m.getFlyTime());
			logger.log(Level.INFO, s, this);
		}
		
		synchronized (missiles) {
			missiles.add(m);
		}
	}
	
	private void checkHidden() {
		synchronized (this) {
			if ( !isHidden && canBeHidden ) {
				if ( war.getTime() >= (exposedTime + exposureTime) ) {
					logger.log(Level.INFO, "Launcher " + id + " is now hidden and safe",this);
					isHidden = true;
				}
			}
		}
	}
	
	private void launchMissile(Missile m) {
		
		synchronized (m) {
			m.launch();
		}
		
		logger.log(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() + " launched",this);
		numMissileLaunch++;
		
		if (canBeHidden) {
			logger.log	(Level.INFO, "Launcher " + id + " is now not hidden and exposed " +
						"for attacks for (" + exposureTime + ") time units",this);
			isHidden = false;
			exposedTime = war.getTime();
		}
	}
	
	public synchronized void missileHit(Missile m) {

		logger.log	(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
					" Successfully hit " + m.getDestination() + "!" +
					LogFormatter.newLine + "Total damage: " + m.getDamage(), this);

		notify();
		
		numMissileHit++;
		totalDamage += m.getDamage();
	}
	
	public synchronized void missileIntercept(Missile m, int time) {

		logger.log(	Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
					" was Intercepted!" + LogFormatter.newLine +
					"Interception time: " + time, this	);

		notify();
		
		numMissileIntercepted++;
	}
	
	public boolean destroyLauncher(int time) {

		synchronized (this) {
			if (!alive || isHidden)
				return false;

			alive = false;
			notify();
			Iterator<Missile> it = missiles.iterator();
			while (it.hasNext()) {
				Missile m = it.next();
				if (!m.onAir()) {
					m.launcherDestroyed();
					logger.log	(Level.INFO, "Launcher " + id + " >> Missile " +
							m.getID() + " was destroyed because of Launcher Destruction",this);
				}
			}

			logger.log(	Level.INFO, "Launcher " + id + " was destroyed!" +
						LogFormatter.newLine + "Destruction time: " + time, this);

			numLauncherDestroyed++;
			
			return true;
		}
	}

	public void end() {
		
		alive = false;
		
		try {
			if (isAlive())
				interrupt();
			
		} catch (IllegalMonitorStateException e) {
			
		}
		
		Iterator<Missile> it = missiles.iterator();
		while (it.hasNext())
			it.next().end();

		fh.close();
	}

	@Override
	public String toString() {
		String str =	"---Launcher " + id + "\n\n\tcanBeHidden: " +
						canBeHidden + "\n\n\t---Missiles\n\n";
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
	}
	
}

