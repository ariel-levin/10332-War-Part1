package Launcher;

public class Target {

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