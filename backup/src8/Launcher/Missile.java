package Launcher;

import War.*;

public class Missile extends Thread {

	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;
	private War war;	// war that belongs to
	
	private boolean onAir;
	
	
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
		onAir = false;
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
//				System.out.println(war.getTime() + ": Missile " + id + " entering wait");
				wait();
//				System.out.println(war.getTime() + ": Missile " + id + " leave wait");
			}
			
			if (launcher.alive()) {
				onAir = true;
				
//				System.out.println(war.getTime() + ": Missile " + id + " in Air - enter sleep");

				sleep(flyTime*War.DELAY);	// fly time
				
//				System.out.println(war.getTime() + ": Missile " + id + " off Air - exit sleep");
				
				onAir = false;
				
//				System.out.println(war.getTime() + ": Missile " + id + " trying to: launcher.missileHit");
				
				synchronized(launcher) {
					launcher.missileHit(this);
				}
				
//				System.out.println(war.getTime() + ": Missile " + id + " finish: launcher.missileHit");
				
			}

//			System.out.println(war.getTime() + ": Missile " + id + " off Air");
			
		} catch (InterruptedException e) {

//			System.out.println(war.getTime() + ": Missile " + id + " interrupt");
			
			synchronized (launcher) {
				launcher.missileIntercept(this, war.getTime());
			}

		}
		onAir = false;
		
//		war.decreaseThreadCount();
//		System.out.println(war.getTime() + ": war.decreaseThreadCount() - Missile " + id + " done");
//		war.printThreadCount();
	}
	
	public String getID() {
		return id;
	}

	public String getDestination() {
		return destination;
	}

	public int getLaunchTime() {
		return launchTime;
	}

	public int getFlyTime() {
		return flyTime;
	}

	public int getDamage() {
		return damage;
	}

	public boolean onAir() {
		return onAir;
	}

	public synchronized void launch() {
//		System.out.println(war.getTime() + ": Missile " + id + " launch");
		notify();
	}
	
	public synchronized boolean intercept() {
//		System.out.println(war.getTime() + ": Missile " + id + " intercept");
		
		if ( (!onAir) || (war.getTime() >= launchTime + flyTime) )
			return false;
		
		interrupt();
		return true;
	}
	
	public synchronized void launcherDestroyed() {
//		System.out.println(war.getTime() + ": Missile " + id + " launcherDestroyed()");
		notify();	// wake - in case isn't launched yet
	}

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

