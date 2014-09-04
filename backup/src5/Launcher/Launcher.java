package Launcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Utility.*;
import War.*;

public class Launcher extends Thread {

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
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
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
	
	public FileHandler getFileHandler() {
		return fh;
	}

	private void checkHidden() {
		if ( !isHidden && canBeHidden )
			if ( War.getTime() >= (exposedTime + exposureTime) ) {
				logger.log(Level.INFO, "Launcher " + id + " is now hidden and safe",this);
				isHidden = true;
			}
	}
	
	private void launchMissile(Missile m) {
		
		synchronized (m) {
			m.launch();
		}
		
		logger.log(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() + " launched",this);
		
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
	}
	
	public synchronized void missileIntercept(Missile m, int time) {

		logger.log	(Level.INFO, "Launcher " + id + " >> Missile " + m.getID() +
				" was Intercepted!" + LogFormatter.newLine +
				"Interception time: " + time, this);

		notify();
	}
	
	public synchronized void destroyLauncher(int time) {
//		interrupt();
		notify();
		alive = false;
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
		
		fh.close();
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
