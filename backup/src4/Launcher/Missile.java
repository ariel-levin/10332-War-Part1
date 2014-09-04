package Launcher;

import War.*;

public class Missile extends Thread {

	private String id;
	private String destination;
	private int launchTime;
	private int flyTime;
	private int damage;
	private Launcher launcher;
	private int hitTime;
	
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
		hitTime = launchTime+flyTime;
		inAir = false;
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
				inAir = true;
//				System.out.println(War.getTime() + ": Missile " + id + " in Air");
			}
			
			while (inAir) {
				
				if ( War.getTime() >= hitTime ) {
					synchronized(launcher) {
						launcher.missileHit(this);
					}
					inAir = false;
				}

				sleep(War.DELAY);
			}
//			System.out.println(War.getTime() + ": Missile " + id + " off Air");
			
		} catch (InterruptedException e) {
			

		}
		inAir = false;
		War.decreaseThreadCount();
		System.out.println(War.getTime() + ": War.decreaseThreadCount() - Missile " + id + " done");
		War.printThreadCount();
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
		inAir = false;
		synchronized (launcher) {
			launcher.missileIntercept(this, War.getTime());
		}
	}
	
	public synchronized void launcherDestroyed() {
		notify();	// wake - in case isn't launched yet
	}

	public void kill() {
		try {
			if (isAlive())
				notify();
		} catch (IllegalMonitorStateException e) {}
		
		inAir = false;
	}
	
	@Override
	public String toString() {
		return "\t\tMissile " + id + "\n\t\tdestination: " + destination
				+ "\n\t\tlaunchTime: " + launchTime + "\n\t\tflyTime: " + flyTime
				+ "\n\t\tdamage: " + damage;
	}
	
}

