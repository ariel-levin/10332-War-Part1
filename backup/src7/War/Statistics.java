package War;

public class Statistics {

	private static int numMissileLaunch = 0;
	private static int numMissileIntercepted = 0;
	private static int numMissileHit = 0;
	private static int numLauncherDestroyed = 0;
	private static int totalDamage = 0;
	
	
	public static void increaseNumMissileLaunch() {
		numMissileLaunch++;
	}
	
	public static void increaseNumMissileIntercepted() {
		numMissileIntercepted++;
	}

	public static void increaseNumMissileHit() {
		numMissileHit++;
	}

	public static void increaseNumLauncherDestroyed() {
		numLauncherDestroyed++;
	}

	public static void increaseTotalDamage(int dmg) {
		totalDamage += dmg;
	}
	
	public static void showStatistics() {
		System.out.println("\n<<<<< Statistics >>>>>\n");
		System.out.println("\tNumber of Missiles Launched: " + numMissileLaunch);
		System.out.println("\tNumber of Missiles Intercepted: " + numMissileIntercepted);
		System.out.println("\tNumber of Missiles Hit in Target: " + numMissileHit);
		System.out.println("\tNumber of Launchers Destroyed: " + numLauncherDestroyed);
		System.out.println("\tTotal War Damage: " + totalDamage);
		System.out.println();
	}	
	
}
