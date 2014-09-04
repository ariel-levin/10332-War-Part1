package War;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.Timer;

import Launcher.*;
import LauncherDestroyer.*;
import MissileDestroyer.*;

public class War extends Thread {
	
	public static final int DELAY = 100;
	
	private static int time = 0;
	private Timer timer = new Timer(DELAY, new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			time++;
		}
	});	
	
	private static int numInitialThreads = 0;
	private static CountDownLatch warStartLatch;
	
	private Map<String,Launcher> launchers;
	private Map<String,MissileDestroyer> missileDestroyers;
	private List<LauncherDestroyer> launcherDestroyers;
	
	public War	(Map<String, Launcher> launchers,
				Map<String, MissileDestroyer> missileDestroyers,
				List<LauncherDestroyer> launcherDestroyers) {
		
		super();
		this.launchers = launchers;
		this.missileDestroyers = missileDestroyers;
		this.launcherDestroyers = launcherDestroyers;
		
		warStartLatch = new CountDownLatch(numInitialThreads);

		startInitThreads();
	}

	public void run() {
		try {
			warStartLatch.await();
			time++;
			timer.start();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void startInitThreads() {
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) {
			Launcher l = launcherIt.next();
			l.start();
			Iterator<Missile> missileIt = l.getMissiles().iterator();
			while(missileIt.hasNext())
				missileIt.next().start();
		}

		Collection<MissileDestroyer> missileDesCol = missileDestroyers.values();
		Iterator<MissileDestroyer> missileDesIt = missileDesCol.iterator();
		while(missileDesIt.hasNext())
			missileDesIt.next().start();

		for(LauncherDestroyer ld : launcherDestroyers)
			ld.start();

	}

	public static int getTime() {
		return time;
	}
	
	public static CountDownLatch getWarStartLatch() {
		return warStartLatch;
	}

	public static void increaseInitThreadCount() {
		numInitialThreads++;
	}
	
	public static void decreaseLatch() {
		warStartLatch.countDown();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	private void printWar() {
		
		Collection<Launcher> c1 = launchers.values();
		Iterator<Launcher> it1 = c1.iterator();
		while(it1.hasNext())
			System.out.println(it1.next());
		
		Collection<MissileDestroyer> c2 = missileDestroyers.values();
		Iterator<MissileDestroyer> it2 = c2.iterator();
		while(it2.hasNext())
			System.out.println(it2.next());
		
		for(LauncherDestroyer ld : launcherDestroyers)
			System.out.println(ld);
		
	}
	
}
