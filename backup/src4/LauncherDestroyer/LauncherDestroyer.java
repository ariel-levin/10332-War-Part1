package LauncherDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import Launcher.*;
import Utility.*;
import War.*;

public class LauncherDestroyer extends Thread {

	private static int idGenerator = 0;
	
	private int id;
	private Heap<Target> targetLaunchers;
	
	private int nextDestroy;
	private boolean alive;
	
	private FileHandler fh = null;
	
	
	public LauncherDestroyer(Heap<Target> targetLaunchers) {
		super();
		this.id = ++idGenerator;
		this.targetLaunchers = targetLaunchers;
		
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler	("logs/LauncherDestroyer_" + id + "_" +
									getClass().getSimpleName() +"_Log.txt",false);
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
		War.log	(Level.INFO, "LauncherDestroyer " + id + " " +
				this.getClass().getSimpleName() + " created",this);
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
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

					if ( War.getTime() >= nextDestroy ) {

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
		War.decreaseThreadCount();
		System.out.println	(War.getTime() + ": War.decreaseThreadCount() - LauncherDestroyer " +
							id + " " + getClass().getSimpleName() + " done");
		War.printThreadCount();
		fh.close();
	}
	
	public int getID() {
		return id;
	}

	public boolean alive() {
		return alive;
	}
	
	private void destroyLauncher(Launcher l) {
		synchronized (l) {
			if (l.alive()) {
				if (l.isHidden()) {
					War.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
							l.getID() + " destruction failed, it's hidden",this);
				} else {
					l.destroyLauncher(War.getTime());
					War.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
							l.getID() + " was destroyed successfully",this);
				}
			} else {
				War.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
						l.getID() + " destruction failed, already destroyed",this);
			}
		}
	}
	
	public void kill() {
		try {
			if (isAlive())
				notify();
		} catch (IllegalMonitorStateException e) {}
		
		alive = false;
	}
	
	public String toString() {
		String str = "---LauncherDestroyer " + id + " : " + this.getClass().getSimpleName();
		str += "\n\n\t---Launcher Targets\n\n";
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
