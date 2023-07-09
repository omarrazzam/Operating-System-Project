package Main;

public class PCB {
	int pid;
	State state;
	int pc;
	int memStart;
	int memEnd;
	
	public PCB(int pid,State state,int pc, int memStart,int memEnd) {
		this.pid = pid;
		this.state = State.READY;
		this.pc = pc;
		this.memStart = memStart;
		this.memEnd = memEnd;
	}
}

