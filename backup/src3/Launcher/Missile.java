package Launcher;

import War.*;

public class Missile extends Thread {

	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;
	
	private boolean inAir;
	

	public Missile	(String id, String destination, int launchTime, int flyTime,
					int damage, Launcher launcher) {
		super();
		this.id = id;
		this.destination = destination;
		this.launchTime = launchTime;
		this.flyTime = flyTime;
		this.damage = damage;
		this.launcher = launcher;
		inAir = false;
	}
	
	public void run() {
		
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			synchronized (this) {
				wait();
			}
			
			sleep(flyTime*War.DELAY);	// flight
			
			synchronized(launcher) {
				launcher.missileHit(this);
			}
			
		} catch (InterruptedException e) {
			

		}
		War.decreaseThreadCount();
		System.out.println("\nWar.decreaseThreadCount() - Missile " + id + " done");
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

	public boolean inAir() {
		return inAir;
	}

	public synchronized void launch() {
		notify();
		inAir = true;
	}
	
	public synchronized void intercept() {
		notify();
		interrupt();
		launcher.missileIntercept(this, War.getTime());
		inAir = false;
	}
	
	public synchronized void launcherDestroyed() {
		notify();
		interrupt();
		inAir = false;
	}
	
	@Override
	public String toString() {
		return "\t\tMissile " + id + "\n\t\tdestination: " + destination
				+ "\n\t\tlaunchTime: " + launchTime + "\n\t\tflyTime: " + flyTime
				+ "\n\t\tdamage: " + damage;
	}
	
}

