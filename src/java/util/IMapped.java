package util;

import java.util.HashSet;
import java.util.Set;

/**
 * A INameable (which is also an IEntity) that has a (possibly empty) keySet.
 * The default implementation returns an empty Set<T>.
 * The hierarchy is:
 * 				Serializable
 * 		_____________|________________________
 *      |									 |
 *   IEntity  getId(), setId(ObjectId)		INameable getName(), setName(String)
 *      |____________________________________|
 *      			|
 *  		INameableEntity		
 *      			|
 *   		IMapped<T>	T keySet()
 *   
 * @author don_bacon
 *
 * @param <T>
 */
public interface IMapped<T> extends INameableEntity {

	default Set<T> keySet() {
		return new HashSet<T>();
	}
}
