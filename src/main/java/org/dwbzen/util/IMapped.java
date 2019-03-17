package org.dwbzen.util;

import java.util.HashSet;
import java.util.Set;
import org.dwbzen.common.util.INameable;

/**
 * A INameable that has a (possibly empty) keySet.
 * The default implementation returns an empty Set<T>.
 * The hierarchy is:
 * 				Serializable
					|
		INameable getName(), setName(String)	
 *      			|
 *   		IMapped<T>	T keySet()
 *   
 * @author don_bacon
 *
 * @param <T>
 */
public interface IMapped<T> extends INameable {

	default Set<T> keySet() {
		return new HashSet<T>();
	}
}
