package util;

import java.io.Serializable;

import org.mongodb.morphia.Morphia;

/**
 * @deprecated use mathlib.util.IJson
 * 
 * @author don_bacon
 *
 */
public interface IJson extends Serializable {
	static Morphia morphia = new Morphia();
	
	default String toJSON() {
		return morphia.toDBObject(this).toString();
	}
}
