package Launcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import Utility.*;
import War.*;

public class Launcher extends Thread {

	private final int exposureTime = 5;
	private int exposedTime;
	
	private String id;
	private boolean isHidden;
	private Heap<Missile> missiles;
	
	private int nextLaunch;
	
	private FileHandler fh = null;

	
	public Launcher(String id, boolean isHidden, Heap<Missile> missiles) {
		super();
		this.id = id;
		this.isHidden = isHidden;
		this.missiles = missiles;
		
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
			
			while (missiles.getSize() > 0) {
				
				Missile m = missiles.getHead();
				nextLaunch = m.getLaunchTime();

				while (War.getTime() < nextLaunch)
					sleep(War.DELAY);

				synchronized (m) {
					launchMissile(m);
				}
				
				synchronized (this) {
					wait();
				}
				missiles.remove();
			}
			War.log(Level.INFO, "Launcher " + id + " has no more missiles",this);

		} catch (InterruptedException e) {
			
			
		}
		War.decreaseThreadCount();
		System.out.println("\nWar.decreaseThreadCount() - Launcher " + id + " done");
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
	
	private void launchMissile(Missile m) {
		War.log(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() + " launched",this);
		m.launch();
		exposedTime = War.getTime();
		isHidden = false;
	}
	
	public String getID() {
		return id;
	}

	public boolean isHidden() {
		return isHidden;
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
	
	public synchronized void missileHit(Missile m) {
		if (m == missiles.getHead()) {	// makes sure it's the right missile
			String s = 	"Launcher " + id + " >> Missile " + m.getID() +
						" Successfully hit " + m.getDestination() +
						"!" + LogFormatter.newLine + "Total damage: " + m.getDamage();
			War.log(Level.INFO, s, this);
			
			notify();
		}
	}
	
	public synchronized void missileIntercept(Missile m, int time) {
		if (m == missiles.getHead()) {	// makes sure it's the right missile
			String s = 	"Launcher " + id + " >> Missile " + m.getID() +
						" was Intercepted!" + LogFormatter.newLine +
						"Interception time: " + time;
			War.log(Level.INFO, s, this);
			
			notify();
		}
	}
	
	public synchronized void destroyLauncher(int time) {
		if (!isHidden) {
			Iterator<Missile> it = missiles.iterator();
			while (it.hasNext())
				it.next().launcherDestroyed();
			
			String s = 	"Launcher " + id + " was destroyed!" +
						LogFormatter.newLine + "Destruction time: " + time;
			War.log(Level.INFO, s, this);
			
			interrupt();
		}
	}
	
	@Override
	public String toString() {
		String str = "---Launcher " + id + "\n\n\tisHidden: " + isHidden;
		str += "\n\n\t---Missiles\n\n";
		java.util.Iterator<Missile> it = missiles.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
		
	}
	
}
