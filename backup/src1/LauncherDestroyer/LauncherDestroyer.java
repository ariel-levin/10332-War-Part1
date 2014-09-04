package LauncherDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.Target;
import Utility.Heap;
import Utility.LogFormatter;
import War.War;

public class LauncherDestroyer extends Thread {

	private static Logger log = Logger.getLogger("LauncherDestroyerLogger");
	private static int count = 0;
	
	private int id;
	private Heap<Target> targetLaunchers;
	
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
	}

	public void run() {
		try {
//			System.out.println(War.getTime() + " :: LauncherDestroyer " + id + " " + getClass().getSimpleName() + " created");
			
			War.decreaseLatch();
			War.getWarStartLatch().await();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.getHandlers()[0].close();
	}
	
	public int getID() {
		return id;
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
