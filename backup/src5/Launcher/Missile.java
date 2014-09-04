package Launcher;

import War.*;

public class Missile extends Thread {

	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;
	
	private boolean onAir;
	
	
	public Missile	(String id, String destination, int launchTime, int flyTime,
					int damage, Launcher launcher) {
		super();
		this.id = id;
		this.destination = destination;
		this.launchTime = launchTime;
		this.flyTime = flyTime;
		this.damage = damage;
		this.launcher = launcher;
		onAir = false;
	}
	
	public void run() {
		
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			// wait for launcher to launch
			synchronized (this) {
//				System.out.println(War.getTime() + ": Missile " + id + " entering wait");
				wait();
//				System.out.println(War.getTime() + ": Missile " + id + " leave wait");
			}
			
			if (launcher.alive()) {
				onAir = true;
				
				System.out.println(War.getTime() + ": Missile " + id + " in Air - enter sleep");

				sleep(flyTime*War.DELAY);	// fly time
				
				System.out.println(War.getTime() + ": Missile " + id + " off Air - exit sleep");
				
				onAir = false;
				
				System.out.println(War.getTime() + ": Missile " + id + " trying to: launcher.missileHit");
				
				synchronized(launcher) {
					launcher.missileHit(this);
				}
				
				System.out.println(War.getTime() + ": Missile " + id + " finish: launcher.missileHit");
				
			}

//			System.out.println(War.getTime() + ": Missile " + id + " off Air");
			
		} catch (InterruptedException e) {

			System.out.println(War.getTime() + ": Missile " + id + " interrupt");
			
			synchronized (launcher) {
				launcher.missileIntercept(this, War.getTime());
			}

		}
		onAir = false;
		
//		War.decreaseThreadCount();
//		System.out.println(War.getTime() + ": War.decreaseThreadCount() - Missile " + id + " done");
//		War.printThreadCount();
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
		System.out.println(War.getTime() + ": Missile " + id + " launch");
		notify();
	}
	
	public synchronized void intercept() {
		System.out.println(War.getTime() + ": Missile " + id + " intercept");
		interrupt();
	}
	
	public synchronized void launcherDestroyed() {
		System.out.println(War.getTime() + ": Missile " + id + " launcherDestroyed()");
		notify();	// wake - in case isn't launched yet
	}

	public void kill() {
		try {
			if (isAlive())
				notify();
		} catch (IllegalMonitorStateException e) {}
		
		onAir = false;
	}
	
	@Override
	public String toString() {
		return "\t\tMissile " + id + "\n\t\tdestination: " + destination
				+ "\n\t\tlaunchTime: " + launchTime + "\n\t\tflyTime: " + flyTime
				+ "\n\t\tdamage: " + damage;
	}
	
}

