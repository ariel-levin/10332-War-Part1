package MissileDestroyer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Launcher.*;
import Utility.*;
import War.*;

public class IronDome extends Thread {

	private static Logger logger = Logger.getLogger("WarLogger");
	
	private String id;
	private Heap<Target> targetMissiles;
	
	private int nextInterception;
	private boolean alive;
	
	private FileHandler fh = null;
	
	
	public IronDome(String id, Heap<Target> targetMissiles) {
		super();
		this.id = id;
		this.targetMissiles = targetMissiles;
		
		setHandler();
	}
	
	public IronDome(String id) {
		super();
		this.id = id;
		targetMissiles = new Heap<Target>(Target.targetComparator);
		
		setHandler();
	}
	
	private void setHandler() {
		try {
			fh = new FileHandler("logs/IronDome_" + id +"_Log.txt",false);
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
		logger.log(Level.INFO, "IronDome " + id + " created",this);
		try {
			War.getWarStartLatch().countDown();
			War.getWarStartLatch().await();
			alive = true;
			
			while (alive) {

				Target t = null;
				Missile m = null;
				
				synchronized (targetMissiles) {
					if (targetMissiles.getSize() > 0) {
						t = targetMissiles.getHead();
						m = (Missile)(t.getTarget());
					}
				}

				if (t != null) {
					nextInterception = t.getDestroyTime();

					if ( War.getTime() >= nextInterception ) {

						synchronized (this) {
//							System.out.println(War.getTime() + ": IronDome " + id + " trying to intercept Missile " + m.getID());
							
							interceptMissile(m);
							
							targetMissiles.remove();
						}

					}
				}
					
				sleep(War.DELAY);
			}
			
		} catch (InterruptedException e) {
			
			
		}
		alive = false;
		
//		War.decreaseThreadCount();
//		System.out.println(War.getTime() + ": War.decreaseThreadCount() - IronDome " + id + " done");
//		War.printThreadCount();
		
//		fh.close();
	}
	
	public String getID() {
		return id;
	}
	
	public boolean alive() {
		return alive;
	}
	
	private void interceptMissile(Missile m) {
		logger.log(	Level.INFO, "IronDome " + id + " >> " +
					"Trying to intercept Missile " + m.getID(), this);
		
		boolean succeed = false;
		
		synchronized (m) {
			succeed = m.intercept();
		}

		if (succeed)
			logger.log(	Level.INFO, "IronDome " + id + " >> Missile " +
						m.getID() + " was intercepted successfully", this	);
		else 
			logger.log(	Level.INFO, "IronDome " + id + " >> Missile " +
						m.getID() + " interception failed, Missile may not be On-Air", this);
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
	
	@Override
	public String toString() {
		String str = "---IronDome " + id;
		str += "\n\n\t---Missile Targets\n\n";
		java.util.Iterator<Target> it = targetMissiles.iterator();
		while(it.hasNext()) {
			Target t = it.next();
			Missile m = (Missile)t.getTarget();
			str += 	"\t\tMissile " + m.getID() + "\n\t\tDestroy Time: " +
					t.getDestroyTime() + "\n\n";
		}
		return str;
	}
	
}
