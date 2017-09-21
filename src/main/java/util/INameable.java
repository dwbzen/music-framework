package util;


/**
 * Predictably enough, something that has a name.
 * @author DBacon
 *
 */
public interface INameable  {
	
	static final String NAME = "name";
	
	void setName(String name);
	String getName();

}
