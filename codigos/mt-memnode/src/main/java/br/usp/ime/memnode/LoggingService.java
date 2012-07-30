package br.usp.ime.memnode;

public abstract class LoggingService {

	private LoggingService() {

	}

	public static void logOut(String marker, String text) {
		System.err.flush();
		System.out.printf("[%5s %10d] %s%n", marker,
				System.currentTimeMillis(), text);
		System.out.flush();
	}

	public static void logOut(String marker, Exception e) {
		System.err.flush();
		System.out.printf("[%5s %10d] ", marker, System.currentTimeMillis());
		e.printStackTrace(System.out);
		System.out.flush();
	}

	public static void logErr(String marker, String text) {
		System.out.flush();
		System.err.printf("[%5s %10d] %s%n", marker,
				System.currentTimeMillis(), text);
		System.err.flush();
	}

	public static void logErr(String marker, Exception e) {
		System.out.flush();
		System.err.printf("[%5s %10d] ", marker, System.currentTimeMillis());
		e.printStackTrace(System.err);
		System.err.flush();
	}
}
