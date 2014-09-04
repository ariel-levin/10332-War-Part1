package Launcher;

import War.War;

public class Missile extends Thread {

	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;

	public Missile	(String id, String destination, int launchTime, int flyTime,
					int damage, Launcher launcher) {
		super();
		this.id = id;
		this.destination = destination;
		this.launchTime = launchTime;
		this.flyTime = flyTime;
		this.damage = damage;
		this.launcher = launcher;
	}
	
	public void run() {
		
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			synchronized (this) {
				wait();
			}
			sleep(flyTime*War.DELAY);	// flight
			launcher.missileHit(this);
			
		} catch (InterruptedException e) {
			
			launcher.missileIntercept(this, War.getTime());
			
		}

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

	public synchronized void launch() {
		notify();
	}
	
	public synchronized void intercept() {
		interrupt();
	}
	
	@Override
	public String toString() {
		return "\t\tMissile " + id + "\n\t\t\tdestination: " + destination
				+ "\n\t\t\tlaunchTime: " + launchTime + "\n\t\t\tflyTime: " + flyTime
				+ "\n\t\t\tdamage: " + damage;
	}
	
}

