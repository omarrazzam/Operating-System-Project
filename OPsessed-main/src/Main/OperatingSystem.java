package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class OperatingSystem {

	static Queue<Integer> ready;
	static Queue<Integer> blocked;
	Memory memory;
	Mutex mutexInput;
	Mutex mutexOutput;
	Mutex mutexFile;
	Hashtable<Integer , String> lafat;
	Hashtable<Integer , String> assignRead;
	int p1Time = 0;
	int p2Time = 1;
	int p3Time = 4;
	int roundTime =2;


	public OperatingSystem() {
		ready = new LinkedList<Integer>();
		blocked = new LinkedList<Integer>();
		memory = new Memory();
		mutexInput = new Mutex();
		mutexOutput = new Mutex();
		mutexFile = new Mutex();
		lafat= new Hashtable<Integer, String>();
		assignRead= new Hashtable<Integer, String>();

	}
	
	

	public void loadPrograms(String path, int i) throws IOException {
			addProgToMem(path, i);
			ready.add(i);
			
	}

	public void addProgToMem(String string, int ind) throws IOException {
		String instr = " ";
		int memIndex = 0;
		if (this.memory.getInMem() == 2)
			swapOut();

		int starting;
		if (memory.ram[0] == null)
			starting=0;
		else 
			starting=20;
		memory.ram[starting] = new Pair("pid", ind + "");
		memory.ram[starting + 1] = new Pair("status", "" + State.READY);
		memory.ram[starting + 2] = new Pair("pc", (starting + 5) + "");
		memory.ram[starting + 3] = new Pair("startBound", starting + "");
		memory.ram[starting + 4] = new Pair("endBound", (starting + 20) + "");
		starting += 5;
		int instCount = 1;
		BufferedReader br = new BufferedReader(new FileReader(string));
		instr = br.readLine();
		while (instr != null) {
//			System.out.println(instr);
			memory.ram[starting] = new Pair("Instruction" + (instCount), instr);
			instCount++;
			starting++;
			instr = br.readLine();

		}
		memory.setInMem(memory.getInMem() + 1);
		memory.setPointer(memory.getPointer() + 20);

                                                            

	}

	public void swapOut() throws IOException {
		Pair state1 = memory.ram[((memory.getInMem()-2) * 20)+1];
		Pair state2 = memory.ram[((memory.getInMem()-1) * 20)+1];
		int starting=0;
		int pidOut;
		
//		if (state1.val.equals("READY")) {
//			starting = 0;
//			pidOut = Integer.parseInt(memory.ram[0].val);
//		} else if (state2.val.equals("READY")) {
//			starting = 20;
//			pidOut = Integer.parseInt(memory.ram[20].val);
//		}
//		if (state1.val.equals("BLOCKED")) {
//			starting = 0;
//			pidOut = Integer.parseInt(memory.ram[0].val);
//		} else if (state2.val.equals("BLOCKED")) {
//			starting = 20;
//			pidOut = Integer.parseInt(memory.ram[20].val);
//		}
		if(!state1.val.equals("RUNNING")){
			starting = 0;
		}
			//remove process1 
		 if(!state2.val.equals("RUNNING")){
			starting = 20;
		}
		if (state1.val.equals("BLOCKED")) {
			starting = 0;
			pidOut = Integer.parseInt(memory.ram[0].val);
		} else if (state2.val.equals("BLOCKED")) {
			starting = 20;
			pidOut = Integer.parseInt(memory.ram[20].val);
		}
		 String path= "src/process"+memory.ram[starting].val + ".txt";
//		File nf  = new File (path);
//		nf.createNewFile();
		 System.out.println("Swapped Out: Program " + memory.ram[starting].val);
		 System.out.println("Program " + memory.ram[starting].val + "stored in" + path);

		FileWriter file = new FileWriter( path);
		BufferedWriter bw =new BufferedWriter(file);
		for (int i=starting ; i<starting+19 ; i++)
		{
			if (memory.ram[i]==null)
				bw.write("null");
			else
			{
				if (i%20 > 16) {					
					bw.write(memory.ram[i].key + ":" +memory.ram[i].val);
				}
				else 
					bw.write(memory.ram[i].val);
				memory.ram[i]=null;
			}

			bw.flush();
			bw.newLine();
			
		}
	
		memory.setInMem(memory.getInMem()-1);
		
	}

	public void executeInst(String inst, int pid) throws IOException {
		String[] terms = inst.split("\\s+");
//		boolean blk = false;
		switch (terms[0]) {
		case "print":
//			if (mutexInput.currentID==pid || mutexInput.currentID==-1)
				print(terms[1], pid);
//			else 
//			{
//				blocked.add(ready.remove());
//				blk =true;
//			}
			break;
			
		case "assign":
			if(terms[2].equals("readFile")) {
				if (assignRead.get(pid)==null)
				{
					assignRead.put(pid, readFile(terms[3], pid));
					decPC(pid);
				}
				else 
				{
					assign(terms[1],"readOut",pid);
					assignRead.remove(pid);
				}
			}
			else {
				assign(terms[1], terms[2], pid);
			}
			break;
		case "writeFile":
//			if (mutexFile.currentID==pid || mutexFile.currentID==-1)
				writeFile(terms[1], terms[2], pid);
//			else {
//				blocked.add(ready.remove());
//				blk=true;
//			}
			break;
		case "readFile":
//			if (mutexFile.currentID==pid || mutexFile.currentID==-1)
				readFile(terms[1],pid);
//			else {
//				blocked.add(ready.remove());
//				blk=true;
//			}
			break;
		case "printFromTo": 
			printFromTo(terms[1],terms[2],pid);
			break;
		case "semWait": 
			semWait(terms[1],pid);
			break;
		case "semSignal":
			semSignal(terms[1],pid);
			break;
		}
//		if (!blk)
			incPC(pid);
	}

	private void incPC(int pid) {
		int pc;
		if (memory.ram[0]!=null  && Integer.parseInt(memory.ram[0].val )== pid )
		{
			pc = Integer.parseInt(memory.ram[2].val) ;
			pc++;
			memory.ram[2].val = pc+"";
		}
		else 
		{
			pc = Integer.parseInt(memory.ram[22].val) ;
			pc++;
			memory.ram[22].val = pc+"";
		}
		
	}

	private void decPC(int pid) {
		int pc;
		if (memory.ram[0]!=null  && Integer.parseInt(memory.ram[0].val )== pid )
		{
			pc = Integer.parseInt(memory.ram[2].val) ;
			pc--;
			memory.ram[2].val = pc+"";
		}
		else 
		{
			pc = Integer.parseInt(memory.ram[22].val) ;
			pc--;
			memory.ram[22].val = pc+"";
		}
		
	}


//	private void add(String string, String string2, int pid) {
//		int x;
//		if (string2.equals("input")) {
//			System.out.println("Please enter a value");
//			Scanner sc = new Scanner(System.in);
//			x = Integer.parseInt(sc);
//			
//		} else {
//			int position2 = searchVar(pid, string2);
//			createVar(pid, string, memory.ram[position2].val);
//		}
//
//	}
//		
//	}
	
	private void semSignal(String string, int pid) {
		if (string.equals("userInput")) {
			if (mutexInput.signal(pid)) {
				if (mutexInput.currentID > 0) {
					int x = mutexInput.currentID;
					ready.add(x);
					blocked.remove(x);
					setStatus(x, State.READY);
					printQueue(ready);
				}
			}
		} else if (string.equals("userOutput")) {
			if (mutexOutput.signal(pid)) {
				if (mutexOutput.currentID > 0) {
					int x = mutexOutput.currentID;
					ready.add(x);
					blocked.remove(x);
					setStatus(x, State.READY);
					printQueue(ready);
				}
			}

		} else if (string.equals("file")) {
			if (mutexFile.signal(pid)) {
				if (mutexFile.currentID > 0) {
					int x = mutexFile.currentID;
					ready.add(x);
					blocked.remove(x);
					setStatus(x, State.READY);
					printQueue(ready);
				}
			}
		}
	}

//	private void semSignal(String string, int pid) 
//	{
//		if(string.equals("userInput")) {
//			if (mutexInput.currentID==pid)
//				mutexInput.currentID=-1;
//		}
//		else if (string.equals("userOutput"))
//		{
//			if (mutexOutput.currentID==pid)
//				mutexOutput.currentID=-1;
//		}
//		else if (string.equals("file"))
//		{
//			if (mutexFile.currentID==pid)
//				mutexFile.currentID=-1;
//		}
//	}
	

	public static void printQueue(Queue<Integer> queue) {
		// Create a temporary queue to preserve the original queue
		Queue<Integer> tempQueue = new LinkedList<>(queue);

		// Print each element in the queue
		while (!tempQueue.isEmpty()) {
			System.out.print(tempQueue.poll());
		}
	}
	
	private void setStatus(int pid, State status) {
		int statusTarget = 0;
		if (memory.ram[0] != null && Integer.parseInt(memory.ram[0].val) == pid)
			statusTarget = 1;
		else 
			statusTarget = 21;
//		System.out.println(memory.ram[statusTarget]);
		memory.ram[statusTarget].val = "" + status;
		
//		System.out.println(memory.ram[statusTarget]);
	}
	
	private void semWait(String string, int pid) {
		if (string.equals("userInput")) {
			if (!mutexInput.wait(pid)) {
				blocked.add(pid);
				setStatus(pid, State.BLOCKED);
				ready.remove();
			}
		} else if (string.equals("userOutput")) {
			if (!mutexOutput.wait(pid)) {
				blocked.add(pid);
				setStatus(pid, State.BLOCKED);
				ready.remove();
			}
		} else if (string.equals("file")) {
			if (!mutexFile.wait(pid)) {
				blocked.add(pid);
				setStatus(pid, State.BLOCKED);
				ready.remove();
			}
		}

	}

//	private void semWait(String string, int pid) {
//		if(string.equals("userInput")) {
//			if (mutexInput.currentID==-1)
//				mutexInput.currentID=pid;
//			else
//			{
//				
//				blocked.add(pid);
//				
//			}
//		}
//		else if (string.equals("userOutput"))
//		{
//			if (mutexOutput.currentID==-1)
//				mutexOutput.currentID=pid;
//			else
//			{
//				blocked.add(pid);
//			}
//		}
//		else if (string.equals("file"))
//		{
//			if (mutexFile.currentID==-1)
//				mutexFile.currentID=pid;
//			else
//			{
//				blocked.add(pid);
//				
//			}
//		}
//		
//	}

	private void printFromTo(String string, String string2, int pid) {
		int position1=searchVar(pid, string);
		int position2=searchVar(pid, string2);
		int val1=Integer.parseInt(memory.ram[position1].val);
		int val2=Integer.parseInt(memory.ram[position2].val);
		for(; val1<=val2;val1++)
			System.out.println(val1);
		
	}

	private String readFile(String string, int pid) throws IOException {
		int position=searchVar(pid, string);
		BufferedReader br=new BufferedReader(new FileReader( "src/" +memory.ram[position].val));
		String str=br.readLine();
		String result="";
		while(str!=null) {
			System.out.println("File "+memory.ram[position].val+":  " + str);
			result=str+result;
			str=br.readLine();
		}
		br.close();
		return result;
	}

	private void writeFile(String string, String string2, int pid) throws IOException {
		int position = searchVar(pid, string);
		int position2 = searchVar(pid, string2);
		File x = new File("src/" + memory.ram[position].val);
		x.createNewFile();
		FileWriter writer = new FileWriter("src/" + memory.ram[position].val);
		writer.write(memory.ram[position2].val);
		writer.close();
	}

	private void assign(String string, String string2, int pid) {
		String x;
		if (string2.equals("input")) {
			
			if (lafat.get(pid)==null)
			{
				System.out.print("Please enter a value: ");
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
				x = input + "";
				lafat.put(pid, x);
				decPC(pid);
			}
			else 
			{
				String in = lafat.get(pid);
				createVar(pid, string, in);
				lafat.remove(pid);
			}
		}
		else if (string2.equals("readOut"))
			createVar(pid, string, assignRead.get(pid));
		else 
		{
			int position2 = searchVar(pid, string2);
			createVar(pid, string, memory.ram[position2].val);
		}
		
	}

	public void createVar(int pid, String key, String value) {
		if (memory.ram[0]!=null &&  pid == Integer.parseInt(memory.ram[0].val)) {
			for (int i = 17; i < 20; i++) {
				if (memory.ram[i] == null) {
					memory.ram[i] = new Pair(key, value);
					break;
				}
			}
		} else {
			for (int i = 37; i < 40; i++) {
				if (memory.ram[i] == null) {
					memory.ram[i] = new Pair(key, value);
					break;
				}
			}
		}
	}

	public int searchVar(int pid, String key) {
		
		
		if (pid == Integer.parseInt(memory.ram[0].val)) {
			for (int i = 17; i < 20; i++) {
				if (memory.ram[i]!=null &&  memory.ram[i].key.equals(key)) {
					return i;
				}
			}
		} else {
			for (int i = 37; i < 40; i++) {
				if (memory.ram[i]!=null && memory.ram[i].key.equals(key)) {
					return i;

				}
			}
		}
		return -1;
	}


	private void print(String key, int pid) {
		int position = searchVar(pid, key);
		System.out.println(" The " + key + " = " + memory.ram[position].val);
	}

	public String getInst(int peek) throws IOException {
		if ( memory.ram[0]!=null && Integer.parseInt(memory.ram[0].val)==peek)
		{
			int pcVal = Integer.parseInt( memory.ram[2].val);
			if (memory.ram[pcVal] !=null)
				return memory.ram[pcVal].val;
		}
		else if  (memory.ram[20]!=null &&  Integer.parseInt(memory.ram[20].val)==peek)
		{
			int pcVal = Integer.parseInt( memory.ram[22].val);
			if (memory.ram[pcVal] !=null)
				return memory.ram[pcVal].val;
		}
		else {
			swapIn(peek);

			return getInst(peek);
		}
		
		return null;
	}
	
	private void swapIn(int peek) throws IOException {
		if (memory.getInMem()==2)
			swapOut();
		int starting;
		if (memory.ram[0]==null)
			starting = 0;
		else 
			starting= 20;
		
		System.out.println("Swapped In: Program " + peek);

		BufferedReader br = new BufferedReader(new FileReader("src/process" + peek + ".txt"));
		String line=br.readLine();
		for (int i=0 ;line!=null && i< 20 ; i ++)
		{
			
			if (starting==0 || starting==20)
			{
				memory.ram[starting] = new Pair("pid", line);
			}
			else if (starting==1 || starting==21)
				memory.ram[starting] = new Pair("status", line);
			else if (starting==2 || starting==22)
			{
				int temp =Integer.parseInt(line);
				if (starting==2 && temp>19)
				{
					temp= temp-20;
					memory.ram[starting] = new Pair("pc",  temp+"");
				}
				else if (starting==22 && temp<19)
				{
					temp= temp+20;
					memory.ram[starting] = new Pair("pc",  temp+"");
				}
				else 
					memory.ram[starting] = new Pair("pc",  line);
			}
			else if (starting==3 || starting==23)
			{
				
				int temp =Integer.parseInt(line);
				if (starting==3 && temp>19)
				{
					temp= temp-20;
					memory.ram[starting] = new Pair("startBound",  temp+"");
				}
				else if (starting==23 && temp<19)
				{
					temp= temp+20;
					memory.ram[starting] = new Pair("startBound",  temp+"");
				}
				else 
					memory.ram[starting] = new Pair("startBound", line);
				
			}
			else if (starting==4 || starting==24)
			{
				int temp =Integer.parseInt(line);
				if (starting==4 && temp>19)
				{
					temp= temp-20;
					memory.ram[starting] = new Pair("endBound",  temp+"");
				}
				else if (starting==23 && temp<19)
				{
					temp= temp+20;
					memory.ram[starting] = new Pair("endBound",  temp+"");
				}
				else
					memory.ram[starting] = new Pair("endBound", line);
			}
			else if ((starting>4 && starting<17) || (starting>24 && starting<37))
			{
				if (line.equals("null"))
				{
					memory.ram[starting]= null;
				}
				else 
				{
					memory.ram[starting] = new Pair("Instruction" + ((starting%20)-4), line);
				}
			}
			else 
			{
				if (line.equals("null"))
					memory.ram[starting]= null;
				else 
				{
					String [] var = line.split(":");
					memory.ram[starting] = new Pair(var[0] , var[1]);
				}
			}
			starting++;
			line=br.readLine();
		}
		br.close();
		File f = new File("process" + peek + ".txt");
		f.delete();
	}

	////////////////////////////Updates///////////////////////////////
	
	public void nullMemory(int pid) {
		int starting = 0;
		if (memory.ram[0] != null && Integer.parseInt(memory.ram[0].val) == pid)
			starting = 0;
		else
			starting = 20;
		int end = starting + 20;
		for (int i = starting; i < end; i++) {
			memory.ram[i] = null;
		}
		memory.setInMem(memory.getInMem() - 1);

	}

	
//	private void printMem() {
//
//		for (int i=0 ; i<40 ; i++)
//		{
//			if (memory.ram[i]==null)
//				System.out.println("null");
//			else
//				System.out.println(memory.ram[i]);
//		}
//	}


	private void printMem() {
	    System.out.println("+-------+----------------------------------+");
	    System.out.println("|address|        Memory                    |");
	    System.out.println("+-------+----------------------------------+");
	    
	    for (int i = 0; i < 40; i++) {
	        System.out.format("| %5d | ", i);
	        
	        if (memory.ram[i] == null)
	            System.out.format("%-40s |%n", "null");
	        else
	            System.out.format("%-40s |%n", memory.ram[i]);
	    }
	    
	    System.out.println("+-------+----------------------------------+");
	}




	public void sheduler() throws IOException 
	{
		int robin=0;
		boolean firstIn =true;
		for (int i=0 ; firstIn || !ready.isEmpty() ; i++)
		{
			if (firstIn)
				firstIn=false;
			
			System.out.println("    ------------------------------------       ");

			System.out.println("------------------ Cycle " + i + " ------------------");
			
			System.out.println("    ------------------------------------       ");

			
			if (i== p1Time)
			{
				loadPrograms("src/Program_1.txt", 1);
			}
			else if (i==p2Time)
			{
				loadPrograms("src/Program_2.txt", 2);
			}
			else if (i==p3Time)
			{
				loadPrograms("src/Program_3.txt", 3);
			}

			printMem();

			String thisInst = getInst(ready.peek());

//			System.out.println("------------------------");
//			for (int t = 0; t < 40; t++) {
//				System.out.println(memory.ram[t]);
//			}  
			int peek = ready.peek();
			setStatus(peek, State.RUNNING);
			
			System.out.print("Ready Queue: " ); 
			printQueue(ready);
			System.out.println("");
			System.out.print("Blocked Queue: " ); 
			printQueue(blocked);
			System.out.println("");

			System.out.println("Executing Program " + peek + ", Instruction: " + thisInst);
			
			executeInst(thisInst, peek);
			robin++;
			
			System.out.print("Ready Queue: ");
			printQueue(ready);
			System.out.print("Blocked Queue: ");
			printQueue(blocked);
			System.out.println("");
			
			if (peek != ready.peek())
			{
				robin=0;
			}
			else if ( getInst(peek)==null)
			{
				nullMemory(ready.peek());
				ready.remove();
				robin=0;

			}
			else if (robin == roundTime)
			{
				setStatus(peek, State.READY);
				ready.add(ready.remove());
				robin=0;
			}
			
			
			
			
//			else if (i== p2Time) 
//				loadPrograms("src/my_program2.txt", 2);
//			else if (i==p3Time) {
//				loadPrograms("src/my_program3.txt", 3);

//				}
//			if (getInst(ready.peek())==null)
//				ready.remove();
//			if(count==2 && getInst(ready.peek())!=null) {
//			int x = ready.remove();
//			ready.add(x);
//			count=0;
//			}
//			System.out.println(ready.peek() +"peeeeeeeeeeeeeeeeeeeeeeeeeek");
//			String thisInst = getInst(ready.peek());
//			if (thisInst!=null)
//			{
//				executeInst(thisInst, ready.peek());
//				count++;
//				
//			}
//			
//
//			
//			for (int j=0 ;getInst(ready.peek())!=null &&  j<2 ;j++)
//			{
//
////				System.out.println("");
//				String thisInst = getInst(ready.peek());
//				System.out.println("Executing: " + ready.peek());
//				executeInst(thisInst, ready.peek());
//				if(j==1)
//					i++;
//
//				System.out.println("----------------------------------");
//			}
			
//			int x = ready.remove();
//			System.out.println(ready.peek() + " Readyyyyyyyyyyyyyyyy ");
//
//			ready.add(x);
//			System.out.println(ready.peek() + " Readyyyyyyyyyyyyyyyy ");
		}
	}






	public static void main(String[] args) throws IOException {
		OperatingSystem sys = new OperatingSystem();
		sys.sheduler();

//		sys.addProgToMem("src/Program_1.txt", 0);

//		sys.addProgToMem("src/Program_2.txt", 1);
//		sys.addProgToMem("src/Program_3.txt", 2);

	}
}
