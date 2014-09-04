package War;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import Launcher.Launcher;


public class Statistics {

	public static void showStatistics(Map<String,Launcher> launchers, int time) {
		System.out.println("\n<<<<< Statistics >>>>>\n");
		System.out.println("\tTime: " + time);
		System.out.println("\tNumber of Missiles Launched: " + getNumMissileLaunch(launchers));
		System.out.println("\tNumber of Missiles Intercepted: " + getNumMissileIntercepted(launchers));
		System.out.println("\tNumber of Missiles Hit in Target: " + getNumMissileHit(launchers));
		System.out.println("\tNumber of Launchers Destroyed: " + getNumLauncherDestroyed(launchers));
		System.out.println("\tTotal War Damage: " + getTotalDamage(launchers));
		System.out.println();
	}
	
	private static int getNumMissileLaunch(Map<String,Launcher> launchers) {
		int numMissileLaunch = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileLaunch += launcherIt.next().getNumMissileLaunch();
		
		return numMissileLaunch;
	}
	
	private static int getNumMissileIntercepted(Map<String,Launcher> launchers) {
		int numMissileIntercepted = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileIntercepted += launcherIt.next().getNumMissileIntercepted();
		
		return numMissileIntercepted;
	}

	private static int getNumMissileHit(Map<String,Launcher> launchers) {
		int numMissileHit = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileHit += launcherIt.next().getNumMissileHit();
		
		return numMissileHit;
	}

	private static int getNumLauncherDestroyed(Map<String,Launcher> launchers) {
		int numLauncherDestroyed = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numLauncherDestroyed += launcherIt.next().getNumLauncherDestroyed();
		
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
