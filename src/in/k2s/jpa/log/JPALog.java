package in.k2s.jpa.log;

import in.k2s.core.interfaces.Entity;
import in.k2s.core.log.BaseLog;
import in.k2s.util.data.DataUtil;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

public class JPALog extends BaseLog {
	
	public static final Integer INSERT = 1;
	public static final Integer UPDATE = 2;
	public static final Integer REMOVE = 3;
	public static final String IP      = "ip";
	public static final String USER    = "user";

	public JPALog(String path) {
		super(path);
	}
	
	public void doLog(Entity entity, Integer operacao, String ... exclude) {
		StringBuilder log = new StringBuilder();
		
		log.append(DataUtil.getTimestamp().getTime() + ";");
		log.append(operacao + ";");
		
		log.append(tableName(entity) + ";");
		
		List<Field> fields = super.getMirror().on(entity.getClass()).reflectAll().fields();
		for(Field field : fields) {
			Object object = super.getMirror().on(entity).get().field(field);
			if(field.getModifiers() != 26 && !excludeField(field, exclude) && object != null) {
				if(isPrimitiveType(field)) {
					log.append(logField(field, entity));
				} else if(object instanceof Entity) {
					log.append(logEntityId(field, object));
				}
			}
		}
		write(log.toString());
	}
	
	protected String logEntityId(Field field, Object object) {
		Object value = super.getMirror().on(object).get().field("id");
		if(value != null) {
			return "$" + columnName(field) + "{" + formatField(value.toString())  + "}";
		}
		return "";
	}
	
	protected String columnName(Field field) {
		Column column = field.getAnnotation(Column.class);
		if(column != null && column.name() != null) {
			return column.name();
		}
		return field.getName();
	}
	
	protected String logField(Field field, Entity entity) {
		String value = super.getMirror().on(entity).get().field(field).toString(); 
		return "$" + columnName(field) + "{" + formatField(value)  + "}";
	}
	
	protected String tableName(Entity entity) {
		Table column = entity.getClass().getAnnotation(Table.class);
		if(column != null && column.name() != null) {
			return column.name();
		}
		return entity.getClass().getSimpleName().toLowerCase();
	}

}
