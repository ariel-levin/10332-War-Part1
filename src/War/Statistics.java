package War;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import Launcher.Launcher;


/**
 * Class used to display War Statistics
 * 
 * @author Ariel Levin
 * 
 * */
public class Statistics {

	/** Display War Statistics
	 * Input: Launchers Map, Current War time */
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
	
	/** Return the total number of Missile Launched from all the Launchers */
	private static int getMissileLaunchCount(Map<String,Launcher> launchers) {
		int numMissileLaunch = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileLaunch += launcherIt.next().getMissileLaunchCount();
		
		return numMissileLaunch;
	}
	
	/** Return the total number of Missile Intercepted from all the Launchers */
	private static int getMissileInterceptedCount(Map<String,Launcher> launchers) {
		int numMissileIntercepted = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileIntercepted += launcherIt.next().getMissileInterceptedCount();
		
		return numMissileIntercepted;
	}

	/** Return the total number of Missile Hits from all the Launchers */
	private static int getMissileHitCount(Map<String,Launcher> launchers) {
		int numMissileHit = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			numMissileHit += launcherIt.next().getMissileHitCount();
		
		return numMissileHit;
	}

	/** Return the total number of Launchers Destroyed */
	private static int getLauncherDestroyedCount(Map<String,Launcher> launchers) {
		int numLauncherDestroyed = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext()) {
			if (launcherIt.next().isLauncherDestroyed())
				numLauncherDestroyed++;
		}
			
		return numLauncherDestroyed;
	}

	/** Return the total damage from all the Missile Hits */
	private static int getTotalDamage(Map<String,Launcher> launchers) {
		int totalDamage = 0;
		
		Collection<Launcher> launcherCol = launchers.values();
		Iterator<Launcher> launcherIt = launcherCol.iterator();
		while(launcherIt.hasNext())
			totalDamage += launcherIt.next().getTotalDamage();
		
		return totalDamage;
	}
	
}

