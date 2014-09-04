package LauncherDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.*;
import Utility.*;
import War.*;

public class LauncherDestroyer extends Thread {

	private static Logger log = Logger.getLogger("LauncherDestroyerLogger");
	private static int count = 0;
	
	private int id;
	private Heap<Target> targetLaunchers;
	
	private int nextDestroy;
	
	static {
		log.setUseParentHandlers(false);
		FileHandler fh = null;
		try {
			fh = new FileHandler("logs/LauncherDestroyerLog.txt",false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new LogFormatter());
		log.addHandler(fh);
	}
	
	public LauncherDestroyer(Heap<Target> targetLaunchers) {
		super();
		this.id = ++count;
		this.targetLaunchers = targetLaunchers;
		log.log	(Level.INFO, "LauncherDestroyer " + id + " " +
				this.getClass().getSimpleName() + " created");
		System.out.println	("LauncherDestroyer " + id + " " +
							this.getClass().getSimpleName() + " created");
	}

	public void run() {
		try {
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
			while (targetLaunchers.getSize() > 0) {
				
				Target t = targetLaunchers.getHead();
				Launcher l = (Launcher)(t.getTarget());
				nextDestroy = t.getDestroyTime();
				
				while (War.getTime() < nextDestroy)
					sleep(War.DELAY);
				
				destroyLauncher(l);
				
				targetLaunchers.remove();
			}
			log.log(Level.INFO, "LauncherDestroyer " + id + " has no more targets");
			System.out.println("LauncherDestroyer " + id + " has no more targets");
			
		} catch (InterruptedException e) {
			
			
		}
		log.getHandlers()[0].close();
	}
	
	public int getID() {
		return id;
	}

	private void destroyLauncher(Launcher l) {
		l.destroyLauncher(War.getTime());
		log.log	(Level.INFO, "LauncherDestroyer " + id + " >> Launcher " +
				l.getID() + " was destroyed successfully");
		System.out.println	("LauncherDestroyer " + id + " >> Launcher " +
							l.getID() + " was destroyed successfully");
	}
	
	public String toString() {
		String str = "---LauncherDestroyer " + id + " : " + this.getClass().getSimpleName();
		str += "\n\n\t---Launchers Targets\n\n";
		java.util.Iterator<Target> it = targetLaunchers.iterator();
		while(it.hasNext())
			str += it.next() + "\n\n";
		return str;
	}
	
}
