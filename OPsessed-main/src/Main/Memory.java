package Main;

public class Memory {
	Pair[] ram;
	private int inMem ;
	private int pointer;

	public Memory() {
		ram = new Pair[40];
		setInMem(0);
		setPointer(0);
	}

	public int getInMem() {
		return inMem;
	}

	public void setInMem(int inMem) {
		this.inMem = inMem;
	}

	public int getPointer() {
		return pointer;
	}

	public void setPointer(int pointer) {
		this.pointer = pointer;
	}
}
