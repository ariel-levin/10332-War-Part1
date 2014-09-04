package Launcher;

import War.War;


/** 
 * @author Ariel Levin
 * 
 * */
public class Missile extends Thread {

	/* Comparator used to sort the missiles in the Minimum Heap, by Launch Time */
	public static final java.util.Comparator<Missile> missileComparator =
							new java.util.Comparator<Missile>() {
		@Override
		public int compare(Missile m1, Missile m2) {
			return ( (Integer)m1.getLaunchTime() ).compareTo(m2.getLaunchTime())*(-1);
		}
	};
	
	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;	// launcher that belongs to
	private War war;			// war that belongs to
	// War is stored in order to gain access to War Time and if War is still Alive
	
	private boolean onAir;		// indicates if the Missile currently On-Air
	private boolean setDuringWar;
	// if Missile adds duringWar (not Pre-War Setup), we give Success chance
	
	
	public Missile(	String id, String destination, int launchTime, int flyTime,
					int damage, Launcher launcher, War war) {
		super();
		this.id = id;
		this.destination = destination;
		this.launchTime = launchTime;
		this.flyTime = flyTime;
		this.damage = damage;
		this.launcher = launcher;
		this.war = war;
		this.setDuringWar = war.alive();	// in this case if war is alive then
											// the missile was created during the war
											// else was created on Pre-War setup
		onAir = false;
		
		// if the war hasn't started yet, increases the Pre-War thread count
		if (!war.alive())
			war.increasePreWarThreadCount();
	}
	
	public void run() {
		
		try {
			// WarStartLatch - waiting for all the threads to start together
			// only if war hasn't start yet (pre-war setup)
			if (!war.alive()) {		
				war.getWarStartLatch().countDown();
				war.getWarStartLatch().await();
			}
			
			// wait for launcher to launch
			synchronized (this) {
				wait();
			}
			
			if (launcher.alive()) {		// if Launcher wasn't destroyed
				onAir = true;
				
				sleep(flyTime*War.DELAY);	// fly time
				
				onAir = false;
				
				synchronized(launcher) {
					launcher.missileHit(this, setDuringWar);
				}
				
			}

		} catch (InterruptedException e) {
			
			// we get here if the Missile is Intercepted, and no longer needs to run

			// check if the missile on-air, because we also may get here in End War option
			if (onAir) {
				synchronized (launcher) {
					launcher.missileIntercept(this, war.getTime());
				}
			}

		}
		
		onAir = false;
	}
	
	/** Returns the Missile ID */
	public String getID() {
		return id;
	}

	/** Returns the Missile Destination */
	public String getDestination() {
		return destination;
	}

	/** Returns the Missile Launch Time */
	public int getLaunchTime() {
		return launchTime;
	}

	/** Returns the Missile Fly Time */
	public int getFlyTime() {
		return flyTime;
	}

	/** Returns the Missile Damage */
	public int getDamage() {
		return damage;
	}

	/** Returns if the Missile currently On-Air */
	public boolean onAir() {
		return onAir;
	}

	/** Method called by Launcher to Launch the Missile */
	public synchronized void launch() {
		notify();	// free Missile from wait
	}
	
	/** Method called by Iron Dome to Intercept the Missile. 
	 *	Returns True or False if Succeeded or not */
	public synchronized boolean intercept() {

		if ( (!onAir) || (war.getTime() >= launchTime + flyTime) )
			return false;
		
		interrupt();
		return true;
	}
	
	/** Method called by the Launcher to let the Missile know the Launcher was Destroyed.
	 * If the Missile On-Air it is not affected */
	public synchronized void launcherDestroyed() {
		notify();	// free from wait - in case isn't launched yet
	}

	/** End the Missile, used on War class, in endWar() Method */
	public void end() {
		
		onAir = false;
		
		try {
			if (isAlive())
				interrupt();
			
		} catch (IllegalMonitorStateException e) {
			
		}
	}
	
	@Override
	public String toString() {
		return "\t\tMissile " + id + "\n\t\tdestination: " + destination
				+ "\n\t\tlaunchTime: " + launchTime + "\n\t\tflyTime: " + flyTime
				+ "\n\t\tdamage: " + damage;
	}
	
}

