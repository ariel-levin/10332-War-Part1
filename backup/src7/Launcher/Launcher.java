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
	
	private static Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private boolean canBeHidden;
	private Heap<Missile> missiles;
	
	private int nextLaunch;
	private boolean alive;
	
	private final int exposureTime = 5;
	private int exposedTime;
	private boolean isHidden;
	
	private FileHandler fh = null;

	
	public Launcher(String id, boolean canBeHidden, Heap<Missile> missiles) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
		this.missiles = missiles;
		
		isHidden = canBeHidden;
		setHandler();
	}
	
	public Launcher(String id, boolean canBeHidden) {
		super();
		this.id = id;
		this.canBeHidden = canBeHidden;
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
		fh.setFormatter(new LogFormatter());
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
			if (!War.alive()) {		
				War.getWarStartLatch().countDown();
				War.getWarStartLatch().await();
			}
			
			alive = true;
			
			while (alive) {
				
				checkHidden();
				
				Missile m = null;
				
				synchronized (missiles) {
					if (missiles.getSize() > 0)
						m = missiles.getHead();
				}
				
				if (m != null) {
					nextLaunch = m.getLaunchTime();
					
					if (War.getTime() >= nextLaunch) {
						logger.log(Level.INFO, "Launcher " + id + " >> Trying to launch Missile " + m.getID(),this);

						synchronized (this) {
							launchMissile(m);
							
//							System.out.println(War.getTime() + ": Launcher " + id + " entering wait");
							
							// Missile in the air, waiting for hit or intercept
							wait();
//							System.out.println(War.getTime() + ": Launcher " + id + " leave wait");
							
							missiles.remove();
						}
						
					}
				}

				sleep(War.DELAY);
			}

		} catch (InterruptedException e) {
			
			
		}
		alive = false;
		
//		War.decreaseThreadCount();
//		System.out.println(War.getTime() + ": War.decreaseThreadCount() - Launcher " + id + " done");
//		War.printThreadCount();
		
//		fh.close();
	}
	
	private void logInitMissiles() {
		Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created :" +
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
	
	public void addMissile(Missile m) {
		missiles.add(m);
	}
	
	private void checkHidden() {
		synchronized (this) {
			if ( !isHidden && canBeHidden ) {
				if ( War.getTime() >= (exposedTime + exposureTime) ) {
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
		Statistics.increaseNumMissileLaunch();
		
		if (canBeHidden) {
			logger.log	(Level.INFO, "Launcher " + id + " is now not hidden and exposed " +
						"for attacks for (" + exposureTime + ") time units",this);
			isHidden = false;
			exposedTime = War.getTime();
		}
	}
	
	public synchronized void missileHit(Missile m) {

		logger.log	(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
					" Successfully hit " + m.getDestination() + "!" +
					LogFormatter.newLine + "Total damage: " + m.getDamage(), this);

		notify();
		
		Statistics.increaseNumMissileHit();
		Statistics.increaseTotalDamage(m.getDamage());
	}
	
	public synchronized void missileIntercept(Missile m, int time) {

		logger.log(	Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
					" was Intercepted!" + LogFormatter.newLine +
					"Interception time: " + time, this	);

		notify();
		
		Statistics.increaseNumMissileIntercepted();
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

			logger.log	(Level.INFO, "Launcher " + id + " was destroyed!" +
					LogFormatter.newLine + "Destruction time: " + time, this);

			Statistics.increaseNumLauncherDestroyed();
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

