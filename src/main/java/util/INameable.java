package util;


/**
 * Predictably enough, something that has a name.
 * @author DBacon
 * @deprecated use mathlib.util.INameable
 *
 */
public interface INameable  {
	
	static final String NAME = "name";
	
	String getName();
	
	void setName(String aname);

}
