package in.k2s.jpa.factory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class BaseFactory {
	
	private static final String FACTORY = "<NULL>";
	private  EntityManagerFactory factory;
	
	public EntityManager getEntityManager(){
		if(factory == null || !factory.isOpen()) {
			factory = Persistence.createEntityManagerFactory(FACTORY);
		}
		return factory.createEntityManager();
	};

}
