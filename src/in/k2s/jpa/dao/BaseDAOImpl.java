package in.k2s.jpa.dao;

import in.k2s.core.bundle.K2Bundle;
import in.k2s.core.interfaces.Entity;
import in.k2s.jpa.factory.BaseFactory;
import in.k2s.jpa.log.JPALog;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;

import net.vidageek.mirror.dsl.Mirror;

import org.apache.commons.beanutils.PropertyUtils;


public class BaseDAOImpl implements BaseDAO {
	
	//JPA ATRIBUTES
	protected JPALog log;
	private EntityManager manager;
	private final BaseFactory factory;
	private final Mirror mirror = new Mirror();
	
	//JPA PARAMS
	private Boolean USE_TRANSACTION;
	private boolean QUERY_CACHE;
	private boolean MANUAL_TRANSACTION = false;
	
	public BaseDAOImpl() {
		this.factory = null;
		loadParams();
	}
	
	public BaseDAOImpl(EntityManager manager) {
		this.factory = null;
		this.manager = manager;
		loadParams();
	}
	
	public BaseDAOImpl(BaseFactory factory) {
		this.factory = factory;
		loadParams();
	}
	
	public void beginTransaction() {
		this.MANUAL_TRANSACTION = true;
		getEntityManager().getTransaction().begin();
	}
	
	public void commitTransaction() {
		this.MANUAL_TRANSACTION = false;
		getEntityManager().getTransaction().commit();
	}
	
	private void loadParams() {
		this.USE_TRANSACTION = K2Bundle.getParam("jpa.transaction.use").equalsIgnoreCase("TRUE") ? true : false;
	}
	
	protected void cacheQuery() {
		this.QUERY_CACHE = true;
	}
	
	protected EntityManager getEntityManager() {
		if(this.manager == null) {
			this.manager = this.factory.getEntityManager();
		}
		return this.manager;
		
	}
	
	public void detach(Entity entity) {
		getEntityManager().detach(entity);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T selectById(Entity entity) {
		return (T) getEntityManager().find(entity.getClass(), entity.getId()); 
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T selectById(Class<? extends Entity> entity, Object id) {
		return (T) getEntityManager().find(entity, id);
	}
	
	public <T extends Entity>T selectByChave(Entity entity) {
		return this.selectByChave(entity.getClass(), mirror.on(entity).get().field("chave").toString());
	}
	
	public <T extends Entity>T selectByChave(Class<? extends Entity> entity, String chave) {
		return this.selectFirstByQuery("SELECT obj FROM " + entity.getSimpleName() + " obj WHERE obj.chave = ?1", chave);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T refresh(Entity entity) {
		getEntityManager().refresh(entity);
		return (T) entity;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T insert(Entity entity) {
		if(entity == null) return null;
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().begin();
		getEntityManager().persist(entity);
		getEntityManager().flush();
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().commit();
		if(log != null) {
			log.doLog(entity, JPALog.INSERT);
		}
		return (T) entity;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T update(Entity entity) {
		if(entity == null) return null;
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().begin();
		getEntityManager().merge(entity);
		getEntityManager().flush();
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().commit();
		if(log != null) {
			log.doLog(entity, JPALog.UPDATE);
		}
		return (T) entity;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity>T remove(Entity entity) {
		if(entity == null) return null;
		entity = selectById(entity);
		if(log != null) {
			log.doLog(entity, JPALog.REMOVE);
		}
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().begin();
		getEntityManager().remove(entity);
		getEntityManager().flush();
		if(USE_TRANSACTION && !MANUAL_TRANSACTION) getEntityManager().getTransaction().commit();
		return (T) entity;
	}
	
	protected <T>T selectFirstByNamedQuery(String namedQuery) {
		List<T> list = selectByNamedQuery(namedQuery, 1);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNamedQuery(String namedQuery, Entity entity) {
		List<T> list = selectByNamedQuery(namedQuery, 1, entity);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNamedQuery(String namedQuery, Object ... parameters) {
		List<T> list = selectByNamedQuery(namedQuery, 1, parameters);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNamedQuery(String namedQuery, Integer maxResults) {
		Query query = getEntityManager().createNamedQuery(namedQuery);
		query = setCacheable(query);
		if(maxResults != null && maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();	
	}
	
	protected <T>List<T> selectByNamedQuery(String namedQuery) {
		return selectByNamedQuery(namedQuery, -1);
	}
	
	protected <T>List<T> selectByNamedQuery(String namedQuery, Entity entity) {
		return selectByNamedQuery(namedQuery, -1, entity);
	}
	
	protected <T>List<T> selectByNamedQuery(String namedQuery, Object ... parameters) {
		return selectByNamedQuery(namedQuery, -1, parameters);
	}
	
	protected <T>T selectFirstByQuery(String query) {
		List<T> list = selectByQuery(query, 1);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByQuery(String query, Entity entity) {
		List<T> list = selectByQuery(query, 1, entity);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByQuery(String query, Object ... parameters) {
		List<T> list = selectByQuery(query, 1, parameters);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>List<T> selectByQuery(String query) {
		return selectByQuery(query, -1);
	}
	
	protected <T>List<T> selectByQuery(String query, Entity entity) {
		return selectByQuery(query, -1, entity);
	}
	
	protected <T>List<T> selectByQuery(String query, Object ... parameters) {
		return selectByQuery(query, -1, parameters);
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery) {
		List<T> list = selectByNativeQuery(sqlQuery, 1);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery, Class<?> type, Entity entity) {
		List<T> list = selectByNativeQuery(sqlQuery, 1, type, entity);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery, Class<?> type, Object ... parameters) {
		List<T> list = selectByNativeQuery(sqlQuery, 1, type, parameters);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery, Class<?> type) {
		List<T> list = selectByNativeQuery(sqlQuery, 1, type);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery, Entity entity) {
		List<T> list = selectByNativeQuery(sqlQuery, 1, entity);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>T selectFirstByNativeQuery(String sqlQuery, Object ... parameters) {
		List<T> list = selectByNativeQuery(sqlQuery, 1, parameters);
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery) {
		return selectByNativeQuery(sqlQuery, -1);
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Entity entity) {
		return selectByNativeQuery(sqlQuery, -1, entity);
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Object ... parameters) {
		return selectByNativeQuery(sqlQuery, -1, parameters);
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Class<?> type) {
		return selectByNativeQuery(sqlQuery, -1, type);
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Class<?> type, Entity entity) {
		return selectByNativeQuery(sqlQuery, -1, type, entity);
	}
	
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Class<?> type, Object ... parameters) {
		return selectByNativeQuery(sqlQuery, -1, type, parameters);
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNamedQuery(String namedQuery, Integer maxResults, Entity entity) {
		try {
			Query query = getEntityManager().createNamedQuery(namedQuery);
			query = setCacheable(query);
			if(maxResults != null && maxResults > 0) {
				query.setMaxResults(maxResults);
			}
			Set<Parameter<?>> parameters = query.getParameters();
			for (Parameter<?> parameter : parameters) {
				String name = parameter.getName();
				Object value = PropertyUtils.getProperty(entity, name.replace('_', '.'));
				query.setParameter(name, value);
			}
			return query.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNamedQuery(String namedQuery, Integer maxResults, Object... parameters) {
		try {
			Query query = getEntityManager().createNamedQuery(namedQuery);
			query = setCacheable(query);
			if(maxResults != null && maxResults > 0) {
				query.setMaxResults(maxResults);
			}
			for (int i = 0; i < parameters.length; i++) {
				query.setParameter(i + 1, parameters[i]);
			}
			return query.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByQuery(String query, Integer maxResults) {
		Query qr = getEntityManager().createQuery(query);
		qr = setCacheable(qr);
		if(maxResults != null && maxResults > 0) {
			qr.setMaxResults(maxResults);
		}
		return qr.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByQuery(String query, Integer maxResults, Entity entity) {
		try {
			Query qr = getEntityManager().createQuery(query);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			Set<Parameter<?>> parameters = qr.getParameters();
			for (Parameter<?> parameter : parameters) {
				String name = parameter.getName();
				Object value = PropertyUtils.getProperty(entity, name.replace('_', '.'));
				qr.setParameter(name, value);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByQuery(String query, Integer maxResults, Object... parameters) {
		try {
			Query qr = getEntityManager().createQuery(query);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			for (int i = 0; i < parameters.length; i++) {
				qr.setParameter(i + 1, parameters[i]);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults) {
		Query qr = getEntityManager().createNativeQuery(sqlQuery);
		qr = setCacheable(qr);
		if(maxResults != null && maxResults > 0) {
			qr.setMaxResults(maxResults);
		}
		return qr.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults, Entity entity) {
		try {
			Query qr = getEntityManager().createNativeQuery(sqlQuery);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			Set<Parameter<?>> parameters = qr.getParameters();
			for (Parameter<?> parameter : parameters) {
				String name = parameter.getName();
				Object value = PropertyUtils.getProperty(entity, name.replace('_', '.'));
				qr.setParameter(name, value);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults, Object... parameters) {
		try {
			Query qr = getEntityManager().createNativeQuery(sqlQuery);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			for (int i = 0; i < parameters.length; i++) {
				qr.setParameter(i + 1, parameters[i]);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults, Class<?> type) {
		Query qr = getEntityManager().createNativeQuery(sqlQuery, type);
		qr = setCacheable(qr);
		if(maxResults != null && maxResults > 0) {
			qr.setMaxResults(maxResults);
		}
		return qr.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults, Class<?> type, Entity entity) {
		try {
			Query qr = getEntityManager().createNativeQuery(sqlQuery, type);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			Set<Parameter<?>> parameters = qr.getParameters();
			for (Parameter<?> parameter : parameters) {
				String name = parameter.getName();
				Object value = PropertyUtils.getProperty(entity, name.replace('_', '.'));
				qr.setParameter(name, value);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T>List<T> selectByNativeQuery(String sqlQuery, Integer maxResults, Class<?> type, Object... parameters) {
		try {
			Query qr = getEntityManager().createNativeQuery(sqlQuery, type);
			qr = setCacheable(qr);
			if(maxResults != null && maxResults > 0) {
				qr.setMaxResults(maxResults);
			}
			for (int i = 0; i < parameters.length; i++) {
				qr.setParameter(i + 1, parameters[i]);
			}
			return qr.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	protected void execute(String query) {
		execute(query, new Object[0]);
	}

	protected void execute(String query, Object... params) {
		Query qr = getEntityManager().createNativeQuery(query);
		for (int i = 0; i < params.length; i++) {
			qr.setParameter(i + 1, params[i]);
		}
		qr = setCacheable(qr);
		qr.executeUpdate();
	}

	private Query setCacheable(Query query) {
		query.setHint("org.hibernate.cacheable", this.QUERY_CACHE);
		this.QUERY_CACHE = false;
		return query;
	}

}
