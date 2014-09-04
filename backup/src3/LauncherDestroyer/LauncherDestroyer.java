package LauncherDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import Launcher.*;
import Utility.*;
import War.*;

public class LauncherDestroyer extends Thread {

	private static int count = 0;
	
	private int id;
	private Heap<Target> targetLaunchers;
	
	private int nextDestroy;
	
	private FileHandler fh = null;
	
	
	public LauncherDestroyer(Heap<Target> targetLaunchers) {
		super();
		this.id = ++count;
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
			
			while (targetLaunchers.getSize() > 0) {
				
				Target t = targetLaunchers.getHead();
				Launcher l = (Launcher)(t.getTarget());
				nextDestroy = t.getDestroyTime();
				
				while (War.getTime() < nextDestroy)
					sleep(War.DELAY);
				
				synchronized (l) { 
					destroyLauncher(l);
				}
				
				targetLaunchers.remove();
			}
			War.log(Level.INFO, "LauncherDestroyer " + id + " has no more targets",this);
			
		} catch (InterruptedException e) {
			
			
		}
		War.decreaseThreadCount();
		System.out.println	("\nWar.decreaseThreadCount() - LauncherDestroyer " +
							id + " " + getClass().getSimpleName() + " done");
		fh.close();
	}
	
	public int getID() {
		return id;
	}

	private void destroyLauncher(Launcher l) {
		if (l.isAlive()) {
			l.destroyLauncher(War.getTime());
			War.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
					l.getID() + " was destroyed successfully",this);
		} else {
			War.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
					l.getID() + " destruction failed",this);
		}
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
