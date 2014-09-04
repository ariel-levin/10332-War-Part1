package LauncherDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.*;
import Utility.*;
import War.*;

public class LauncherDestroyer extends Thread {

	private Logger logger = Logger.getLogger("WarLogger");
	
	private static int idGenerator = 0;
	
	private int id;
	private String type;
	private Heap<Target> targetLaunchers;
	private War war;	// war that belongs to
	
	private int nextDestroy;
	private boolean alive;
	
	private FileHandler fh = null;
	
	
	public LauncherDestroyer(String type, Heap<Target> targetLaunchers, War war) {
		super();
		this.id = ++idGenerator;
		this.type = type;
		this.targetLaunchers = targetLaunchers;
		this.war = war;
		
		setHandler();
	}
	
	public LauncherDestroyer(String type, War war) {
		super();
		this.id = ++idGenerator;
		this.type = type;
		this.war = war;
		targetLaunchers = new Heap<Target>(Target.targetComparator);
		
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler(	"logs/LauncherDestroyer_" + id + "_" +
									type +"_Log.txt", false	);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter(war));
		fh.setFilter(new ObjectFilter(this));
		logger.addHandler(fh);
	}

	public void run() {
		logger.log(	Level.INFO, "LauncherDestroyer " + id + " " +
					this.getClass().getSimpleName() + " created",this);
		try {
			// WarStartLatch - waiting for all the threads to start together
			// only if war hasn't start yet (pre-war setup)
			if (!war.alive()) {		
				war.getWarStartLatch().countDown();
				war.getWarStartLatch().await();
			}
			
			alive = true;
			
			while (alive) {
				
				Target t = null;
				Launcher l = null;
				
				synchronized (targetLaunchers) {
					if (targetLaunchers.getSize() > 0) {
						t = targetLaunchers.getHead();
						l = (Launcher)(t.getTarget());
					}
				}
				
				if (t != null) {
					nextDestroy = t.getDestroyTime();

					if ( war.getTime() >= nextDestroy ) {

						synchronized (this) {
							destroyLauncher(l);
							
							targetLaunchers.remove();
						}
					}
				}

				sleep(War.DELAY);
			}
			
		} catch (InterruptedException e) {
			
			
		}
		alive = false;
		
//		war.decreaseThreadCount();
//		System.out.println	(war.getTime() + ": war.decreaseThreadCount() - LauncherDestroyer " +
//							id + " " + getClass().getSimpleName() + " done");
//		war.printThreadCount();
		
//		fh.close();
	}
	
	public int getID() {
		return id;
	}

	public String getType() {
		return type;
	}

	public boolean alive() {
		return alive;
	}
	
	private void destroyLauncher(Launcher l) {
		logger.log(	Level.INFO, "LauncherDestroyer " + id +
					" >> Trying to destroy Launcher " + l.getID(), this	);
		
		boolean succeed = false;
		
		synchronized (l) {
			succeed = l.destroyLauncher(war.getTime());
		}
		
		if (succeed)
			logger.log(	Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
						l.getID() + " was destroyed successfully",this	);
		else
			logger.log(	Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
						l.getID() + " destruction failed - hidden or already destroyed", this);
	}
	
	public void addTarget(Target t) {
		synchronized (targetLaunchers) {
			targetLaunchers.add(t);
		}
	}
	
	public void end() {
		
		alive = false;
		
		try {
			if (isAlive())
				interrupt();
			
		} catch (IllegalMonitorStateException e) {
			
		}
		
		fh.close();
	}
	
	public String toString() {
		String str = 	"---LauncherDestroyer " + id + " : " + type +
						"\n\n\t---Launcher Targets\n\n";
		java.util.Iterator<Target> it = targetLaunchers.iterator();
		while(it.hasNext()) {
			Target t = it.next();
			Launcher l = (Launcher)t.getTarget();
			str += 	"\t\tLauncher " + l.getID() + "\n\t\tDestroy Time: " +
			t.getDestroyTime() + "\n\n";
		}
		return str;
	}
	
}
