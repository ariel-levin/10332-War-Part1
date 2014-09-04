package Launcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import Utility.*;
import War.*;

public class Launcher extends Thread {

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
		War.getLogger().addHandler(fh);
	}

	public void run() {
		War.log(Level.INFO, "Launcher " + id + " created", this);
		logInitMissiles();
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			alive = true;
			
			while (alive) {
				
				if ( !isHidden && canBeHidden )
					if ( War.getTime() >= (exposedTime + exposureTime) )
						isHidden = true;
				
				Missile m = null;
				
				synchronized (missiles) {
					if (missiles.getSize() > 0)
						m = missiles.getHead();
				}
				
				if (m != null) {
					nextLaunch = m.getLaunchTime();
					
					if (War.getTime() >= nextLaunch) {

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
		War.decreaseThreadCount();
		System.out.println(War.getTime() + ": War.decreaseThreadCount() - Launcher " + id + " done");
		War.printThreadCount();
		fh.close();
	}
	
	private void logInitMissiles() {
		Iterator<Missile> it = missiles.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			String s =	"Launcher " + id + " >> Missile " + m.getID() + " created :" +
						LogFormatter.newLine + "Destination: " + m.getDestination() + " , " + 
						"Launch Time: " + m.getLaunchTime()  + " , " +
						"Estimated Land Time: " + (m.getLaunchTime()+m.getFlyTime());
			War.log(Level.INFO, s, this);
		}
	}
	
	public String getID() {
		return id;
	}

	public boolean isHidden() {
		return canBeHidden;
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
	
	private void launchMissile(Missile m) {
		War.log(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() + " launched",this);
		
		if (canBeHidden) {
			isHidden = false;
			exposedTime = War.getTime();
		}
		synchronized (m) {
			m.launch();
		}
	}
	
	public synchronized void missileHit(Missile m) {

		War.log	(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
				" Successfully hit " + m.getDestination() + "!" +
				LogFormatter.newLine + "Total damage: " + m.getDamage(), this);

		notify();
	}
	
	public synchronized void missileIntercept(Missile m, int time) {

		War.log	(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
				" was Intercepted!" + LogFormatter.newLine +
				"Interception time: " + time, this);

		notify();
	}
	
	public synchronized void destroyLauncher(int time) {
		if (!isHidden) {
			alive = false;
			notify();
			Iterator<Missile> it = missiles.iterator();
			while (it.hasNext()) {
				Missile m = it.next();
				if (!m.inAir()) {
					m.launcherDestroyed();
					War.log	(Level.INFO, "Launcher " + id + " >> Missile " +
							m.getID() + " was destroyed because of Launcher Destruction",this);
				}
			}

			War.log	(Level.INFO, "Launcher " + id + " was destroyed!" +
					LogFormatter.newLine + "Destruction time: " + time, this);
		}
	}

	public void kill() {
		try {
			if (isAlive())
				notify();
		} catch (IllegalMonitorStateException e) {}
		
		alive = false;
		
		Iterator<Missile> it = missiles.iterator();
		while (it.hasNext()) {
			Missile m = it.next();
			m.kill();
		}
	}
	
	@Override
	public String toString() {
		String str = "---Launcher " + id + "\n\n\tisHidden: " + canBeHidden;
		str += "\n\n\t---Missiles\n\n";
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
		
	}
	
}
