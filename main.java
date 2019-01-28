// yonatan zelba 200757367

package main;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class main {
	static int numOfProccess;
	static List<Procces> proccess;

	public static void main(String[] args) {
		ReadFileIntoList myReadFileIntoList = new ReadFileIntoList();
		List<String> l = myReadFileIntoList.readFileInList(args[1]);
		float meanTurnAround;
		numOfProccess = Integer.parseInt(l.get(0));
		proccess = new ArrayList<Procces>();
		for (int i = 1; i <= numOfProccess; i++) {
			proccess.add(new Procces(l.get(i)));
		}
		proccess.sort(Comparator.comparing(Procces::getArriveTime));

		FCFS alg1 = new FCFS();
		LCFSnonPre alg2 = new LCFSnonPre();
		LCFSPre alg3 = new LCFSPre();
		RoundRobin alg4 = new RoundRobin();
		SLFPre alg5 = new SLFPre();
		
		for (int x = 0; x < 5; x++) {

			proccess = new ArrayList<Procces>();
			l = myReadFileIntoList.readFileInList(args[x]);
			numOfProccess = Integer.parseInt(l.get(0));
			for (int i = 1; i <= numOfProccess; i++) {
				proccess.add(new Procces(l.get(i)));
			}
			System.out.println();
			System.out.println("------ Input: " + (x+1) +" ------");
			proccess.sort(Comparator.comparing(Procces::getArriveTime));
			meanTurnAround = alg1.runProccess(proccess);
			System.out.println("FCFS mta: " + meanTurnAround);
			meanTurnAround = alg2.runProccess(proccess);
			System.out.println("LCFSnonPre mta: " + meanTurnAround);
			meanTurnAround = alg3.runProccess(proccess);
			System.out.println("LCFSPre mta: " + meanTurnAround);
			meanTurnAround = alg4.runProccess(proccess);
			System.out.println("RoundRobin mta: " + meanTurnAround);
			meanTurnAround = alg5.runProccess(proccess);
			System.out.println("SLFPre mta: " + meanTurnAround);
		}
	}

}

class SLFPre {

	float curentTime;
	float TurnAround;
	LinkedList<Procces> proccesQueue;

	public float runProccess(List<Procces> plist) {
		List<Procces> proclist = new ArrayList<Procces>();
		for (Procces p : plist) {
			proclist.add(p.Copy());
		}
		Procces tempP;
		int processed = 0;
		tempP = new Procces("0,0");
		curentTime = proclist.get(0).arriveTimeStamp;
		proccesQueue = new LinkedList<Procces>();
		proccesQueue.add(proclist.get(0));
		TurnAround = 0;
		proclist.remove(0);

		while (!proclist.isEmpty() || !proccesQueue.isEmpty()) {
			if (!proccesQueue.isEmpty())
				tempP = new Procces(proccesQueue.poll());
			else
				tempP = new Procces(proclist.remove(0));

			if (tempP.computationTime - 1 >= 0) {
				if (!proclist.isEmpty()) {
					for (int j = 0; j < proclist.size(); j++) {
						if (proclist.get(j).arriveTimeStamp == curentTime) {
							proccesQueue.add(tempP);
							tempP = proclist.remove(j);
							if (j >= 0)
								j -= 1;
						}
					}
				}
			}
			proccesQueue.add(tempP);
			proccesQueue.sort(Comparator.comparing(Procces::getcomputationTime));
			tempP = proccesQueue.poll();
			tempP.computationTime -= 1;
			tempP.computed += 1;
			curentTime += 1;
			TurnAround += proccesQueue.size() + 1;
			for (Procces p : proccesQueue)
				p.waitingDuration += 1;

			if (tempP.computationTime > 0)
				proccesQueue.add(tempP);
		}

		return TurnAround / main.numOfProccess;
	}

}

class RoundRobin {

	float curentTime;
	float TurnAround;
	LinkedList<Procces> proccesQueue;

	public float runProccess(List<Procces> plist) {
		List<Procces> proclist = new ArrayList<Procces>();
		for (Procces p : plist) {
			proclist.add(p.Copy());
		}
		Procces tempP;
		int processed = 0;
		TurnAround = 0;
		tempP = new Procces("0,0");
		curentTime = proclist.get(0).arriveTimeStamp;
		proccesQueue = new LinkedList<Procces>();
		proccesQueue.add(proclist.get(0));
		proclist.remove(0);

		while (!proclist.isEmpty() || !proccesQueue.isEmpty()) {
			if (!proccesQueue.isEmpty())
				tempP = new Procces(proccesQueue.poll());
			else
				tempP = new Procces(proclist.remove(0));

			if (tempP.computationTime - 2 >= 0) {
				if (!proclist.isEmpty()) {
					for (int j = 0; j < proclist.size(); j++) {
						if (proclist.get(j).arriveTimeStamp == curentTime
								|| proclist.get(j).arriveTimeStamp == curentTime + 1) {
							proccesQueue.add(proclist.remove(j));
							if (j >= 0)
								j -= 1;
						}
					}
				}
				processed = 2;

			} else if (tempP.computationTime - 1 == 0) {
				if (!proclist.isEmpty()) {
					for (int j = 0; j < proclist.size(); j++) {
						if (proclist.get(j).arriveTimeStamp == curentTime
								|| proclist.get(j).arriveTimeStamp == curentTime + 1) {
							proccesQueue.add(proclist.remove(j));
							if (j >= 0)
								j -= 1;
						}
					}
				}
				processed = 1;
			}
			tempP.computationTime -= processed;
			tempP.computed += processed;
			curentTime += processed;
			TurnAround += proccesQueue.size() * processed + processed;
			for (Procces p : proccesQueue)
				p.waitingDuration += processed;

			if (tempP.computationTime > 0)
				proccesQueue.add(tempP);
		}

		return TurnAround / main.numOfProccess;
	}

}

class LCFSPre {

	float totalTime;
	float meanTurnAround;
	Stack<Procces> proccesStack;

	public float runProccess(List<Procces> plist) {
		List<Procces> proclist = new ArrayList<Procces>();
		for (Procces p : plist) {
			proclist.add(p.Copy());
		}
		totalTime = 0;
		meanTurnAround = 0;
		proccesStack = new Stack<Procces>();

		while (!proclist.isEmpty()) {
			Procces tempP = new Procces(proclist.get(0));
			proclist.remove(0);
			tempP.endTimeStamp = tempP.arriveTimeStamp + tempP.computationTime;
			tempP.waitingDuration = 0;
			if (proclist.size() > 0)
				for (int j = 0; j < proclist.size(); j++)
					if (proclist.get(j).arriveTimeStamp <= tempP.endTimeStamp)
						tempP.waitingDuration += proclist.get(j).computationTime;
			if (tempP.arriveTimeStamp + tempP.computationTime + tempP.waitingDuration > totalTime)
				totalTime = tempP.arriveTimeStamp + tempP.computationTime + tempP.waitingDuration;
			meanTurnAround += (tempP.computationTime + tempP.waitingDuration);
		}
		return meanTurnAround / main.numOfProccess;
	}
}

class LCFSnonPre {

	float totalTime;
	float meanTurnAround;
	Stack<Procces> stack;

	public float runProccess(List<Procces> plist) {
		List<Procces> proclist = new ArrayList<Procces>();
		for (Procces p : plist) {
			proclist.add(p.Copy());
		}
		totalTime = 0;
		meanTurnAround = 0;
		stack = new Stack<Procces>();
		int i = 0;
		int tempSize = proclist.size();
		while (!proclist.isEmpty()) {
			Procces tempP = new Procces(proclist.get(i));
			proclist.remove(i);
			tempSize--;
			totalTime += (tempP.arriveTimeStamp - totalTime) + tempP.computationTime;
			meanTurnAround += tempP.computationTime;
			tempP.startTimeStamp = tempP.arriveTimeStamp;
			tempP.endTimeStamp = (int) totalTime;
			if (tempSize != 0) {
				for (int j = 0; j < tempSize; j++) {
					if (proclist.get(0).arriveTimeStamp <= tempP.endTimeStamp) {
						stack.push(proclist.get(0));
						proclist.remove(0);
					}
				}
				while (!stack.isEmpty()) {
					tempSize--;
					tempP = stack.pop();
					tempP.startTimeStamp = (int) totalTime;
					tempP.waitingDuration = (int) totalTime - tempP.arriveTimeStamp;
					meanTurnAround += tempP.computationTime + tempP.waitingDuration;
					totalTime += tempP.computationTime;

				}
			}
		}
		return meanTurnAround / main.numOfProccess;
	}
}

class FCFS {

	float meanTurnAround;

	public float runProccess(List<Procces> plist) {
		List<Procces> proclist = new ArrayList<Procces>();
		for (Procces p : plist) {
			proclist.add(p.Copy());
		}
		float totalTime;
		totalTime = proclist.get(0).computationTime + proclist.get(0).arriveTimeStamp;
		meanTurnAround = proclist.get(0).computationTime;
		proclist.get(0).endTimeStamp = (int) totalTime;
		proclist.get(0).startTimeStamp = proclist.get(0).arriveTimeStamp;
		for (int i = 1; i < proclist.size(); i++) {

			if (proclist.get(i).arriveTimeStamp <= proclist.get(i - 1).endTimeStamp) {
				proclist.get(i).startTimeStamp = (int) totalTime;
				proclist.get(i).waitingDuration = (int) totalTime - proclist.get(i).arriveTimeStamp;
			} else {
				proclist.get(i).startTimeStamp = proclist.get(i).arriveTimeStamp;
				totalTime = proclist.get(i).arriveTimeStamp;
			}
			proclist.get(i).endTimeStamp = (int) totalTime + proclist.get(i).computationTime;
			meanTurnAround += proclist.get(i).computationTime + proclist.get(i).waitingDuration;
			totalTime += proclist.get(i).computationTime;
		}
		return meanTurnAround / main.numOfProccess;
	}
}

class Procces {

	public int arriveTimeStamp;
	public int startTimeStamp;
	public int endTimeStamp;
	public int waitingDuration;
	public int computationTime;
	public int proccesDurationLeft;
	public int interruptedOn;
	public int computed;

	public Procces Copy() {
		Procces p = new Procces("0,0");
		p.startTimeStamp = startTimeStamp;
		p.waitingDuration = waitingDuration;
		p.arriveTimeStamp = arriveTimeStamp;
		p.computationTime = computationTime;
		p.endTimeStamp = endTimeStamp;
		p.proccesDurationLeft = proccesDurationLeft;
		p.interruptedOn = interruptedOn;
		p.computed = computed;
		return p;
	}

	public Procces(Procces p) {
		waitingDuration = p.waitingDuration;
		arriveTimeStamp = p.arriveTimeStamp;
		computationTime = p.computationTime;
		interruptedOn = p.interruptedOn;
		computed = p.computed;
	}

	public Procces(String line) {
		String[] parts = line.split(",");
		startTimeStamp = 0;
		waitingDuration = 0;
		arriveTimeStamp = Integer.parseInt(parts[0]);
		computationTime = Integer.parseInt(parts[1]);
		computed = 0;
	}

	public int getArriveTime() {
		return arriveTimeStamp;
	}

	public int getcomputationTime() {
		return computationTime + computed;
	}

	public int getArriveReversedTime() {
		return -arriveTimeStamp;
	}
}

class ReadFileIntoList {

	public static List<String> readFileInList(String fileName) {

		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		}

		catch (IOException e) {

			// do something
			e.printStackTrace();
		}
		return lines;
	}
}
