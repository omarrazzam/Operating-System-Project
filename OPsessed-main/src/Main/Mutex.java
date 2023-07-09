package Main;

import java.util.ArrayList;

public class Mutex {
	int currentID;
	ArrayList<Integer> blockedList;
	
	public Mutex() {
		currentID = -1;
		blockedList = new ArrayList<>();
	}
	
	public boolean wait(int processId) {
		if (currentID == -1) {
			currentID = processId;
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			System.out.println("MUTEX GRANTED SUCCESFULLY");
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			return true;
		}
		blockedList.add(processId);
		System.out.println(":::::::::::::::::::::::::::::::::::::");
		System.out.println("MUTEX COULDN'T BE GRANTED");
		System.out.println(":::::::::::::::::::::::::::::::::::::");
		return false;
	}
	public boolean signal(int processId) {
		if (currentID == processId) {
			currentID = -1;
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			System.out.println("MUTEX RELEASED SUCCESFULLY");
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			if(!blockedList.isEmpty()) {
				int x = blockedList.get(0);
				blockedList.remove(0);
				currentID = x;
				System.out.println(":::::::::::::::::::::::::::::::::::::");
				System.out.println("MOVED "+ currentID +" FROM BLOCKED TO THE MUTEX");
				System.out.println(":::::::::::::::::::::::::::::::::::::");
				
				return true;
			}
		}
		else {
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			System.out.println("MUTEX COULDN'T BE RELEASED");
			System.out.println(":::::::::::::::::::::::::::::::::::::");
			return false;
		}
		return false;
		
	}
	
}
