package in.k2s.jpa.sequence;

import java.util.UUID;

public class SequenceGenerator {
	
	private final static Integer PARENT_ID_SIZE = 6; //Limite do Long
	
	public synchronized static Long generate() {
		return SequenceGenerator.generate(null);
	}
	
	public synchronized static Long generate(Long parent) {
		SequenceGenerator.sleep();
		Long timestamp  = System.currentTimeMillis();
		if(parent == null) parent = 0L;
		String parentId = String.format("%0"+PARENT_ID_SIZE+"d", parent);
		return new Long(timestamp.toString() + parentId);
	}
	
	public synchronized static String generateUUID() {
		SequenceGenerator.sleep();
		return UUID.randomUUID().toString();
	}
	
	/*
	 * Dorme dois milisegundos para evitar que o hash do clock se repita
	 */
	private synchronized static void sleep() {
		try { Thread.sleep(2); } 
		catch (InterruptedException e) { e.printStackTrace(); }
	}

}