package br.usp.ime.memnode;

public class Main2 {

	public static void main(String[] args) {
		
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		
	}
	
}
