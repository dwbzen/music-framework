package util;
import mathlib.util.INameable;

/**
 * Predictably enough, something that has a name that is also an IEntity.
 * So it also has an Object Id
 * @author DBacon
 *
 */
public interface INameableEntity extends INameable, IEntity {
	
	@Override
	default String rollJSON() {
		return( "{ " + "name:" + ": " + getName() + "}");
		
	}
	
	// INamable
	//	void setName(String name);
	//  String getName();
	
	// IEntity
	//	String rollJSON();
	//	ObjectId getId();
	//	void setId(ObjectId id)
}
