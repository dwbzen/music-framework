package util;

import java.io.Serializable;

import org.bson.types.ObjectId;

public interface IEntity extends Serializable {
	String rollJSON();
	ObjectId getId();
	void setId(ObjectId id);
}

