package War;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import Launcher.Launcher;


public class Statistics {

	public static void showStatistics(Map<String,Launcher> launchers, int time) {
		System.out.println("\n<<<<< Statistics >>>>>\n");
		System.out.println("\tTime: " + time);
		System.out.println("\tNumber of Missiles Launched: " + getMissileLaunchCount(launchers));
		System.out.println("\tNumber of Missiles Intercepted: " + getMissileInterceptedCount(launchers));
		System.out.println("\tNumber of Missiles Hit in Target: " + getMissileHitCount(launchers));
		System.out.println("\tNumber of Launchers Destroyed: " + getLauncherDestroyedCount(launchers));
		System.out.println("\tTotal War Damage: " + getTotalDamage(launchers));
		System.out.println();
	}
	
	private static int getMissileLaunchCount(Map<String,Launcher> launchers) {
		int numMissileLaunch = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileLaunch += launcherIt.next().getMissileLaunchCount();
		
		return numMissileLaunch;
	}
	
	private static int getMissileInterceptedCount(Map<String,Launcher> launchers) {
		int numMissileIntercepted = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileIntercepted += launcherIt.next().getMissileInterceptedCount();
		
		return numMissileIntercepted;
	}

	private static int getMissileHitCount(Map<String,Launcher> launchers) {
		int numMissileHit = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileHit += launcherIt.next().getMissileHitCount();
		
		return numMissileHit;
	}

	private static int getLauncherDestroyedCount(Map<String,Launcher> launchers) {
		int numLauncherDestroyed = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numLauncherDestroyed += launcherIt.next().getLauncherDestroyed();
		
		return numLauncherDestroyed;
	}

	private static int getTotalDamage(Map<String,Launcher> launchers) {
		int totalDamage = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			totalDamage += launcherIt.next().getTotalDamage();
		
		return totalDamage;
	}
	
}

