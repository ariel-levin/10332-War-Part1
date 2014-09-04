package Launcher;

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
	
	public Target(Thread target, int destroyTime) {
		super();
		this.target = target;
		this.destroyTime = destroyTime;
	}

	public Thread getTarget() {
		return target;
	}

	public int getDestroyTime() {
		return destroyTime;
	}
	
}

