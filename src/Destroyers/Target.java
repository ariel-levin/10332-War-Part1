/* Ariel Levin */

package Destroyers;


/** Target class stores the Destroyers Targets with Destroy time */
public class Target {

	/* Comparator used to sort the targets in the Minimum Heap, by Destroy Time */
	public static final java.util.Comparator<Target> targetComparator =
							new java.util.Comparator<Target>() {
		@Override
		public int compare(Target t1, Target t2) {
			return ( (Integer)t1.getDestroyTime() ).compareTo(t2.getDestroyTime())*(-1);
		}
	};
	
	private Thread target;
	private int destroyTime;
	private boolean setDuringWar;
	// if Target setDuringWar (not Pre-War Setup), we give it an Action Delay, and Success chance.
	// See in IronDome class: interceptMissileWithExtras(Missile m)
	// See in LauncherDestroyer class: destroyLauncherWithExtras(Launcher l)
	
	public Target(Thread target, int destroyTime, boolean setDuringWar) {
		super();
		this.target = target;
		this.destroyTime = destroyTime;
		this.setDuringWar = setDuringWar;
	}

	public Thread getTarget() {
		return target;
	}

	public int getDestroyTime() {
		return destroyTime;
	}

	/** Returns if the Target was set during War is active, or false if was set on Pre-War setup */
	public boolean isSetDuringWar() {
		return setDuringWar;
	}
	
}

