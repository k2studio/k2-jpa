package in.k2s.jpa.dao;

import in.k2s.core.interfaces.Entity;

public interface BaseDAO {
	
	public void beginTransaction();
	
	public void commitTransaction();
	
	public void detach(Entity entity);
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public <T extends Entity>T selectById(Entity entity);
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public <T extends Entity>T selectByChave(Entity entity);
	
	/**
	 * 
	 * @param entity
	 * @param id
	 * @return
	 */
	public <T extends Entity>T selectById(Class<? extends Entity> entity, Object id);
	
	/**
	 * 
	 * @param entity
	 * @param id
	 * @return
	 */
	public <T extends Entity>T selectByChave(Class<? extends Entity> entity, String chave);
	
	/**
	 * Atualiza o objeto com os dados armazenados na base
	 * @param entity
	 * @return
	 */
	public <T extends Entity>T refresh(Entity entity);
	
	/**
	 * Persiste um registro na base de dados
	 * 
	 * @param entity
	 */
	public <T extends Entity>T insert(Entity entity);
	
	/**
	 * Atualiza um registro na base de dados
	 * 
	 * @param entity
	 */
	public <T extends Entity>T update(Entity entity);
	
	/**
	 * Remove um registro da base de dados
	 * 
	 * @param entity
	 */
	public <T extends Entity>T remove(Entity entity);

}
