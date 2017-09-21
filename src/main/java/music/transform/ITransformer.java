package music.transform;

import java.util.Properties;
import java.util.function.Consumer;

import music.instrument.Instrument;

public interface ITransformer extends Consumer<Layer> {

	void transform(Layer layer);
	void configure(Properties props);
	void configure(Properties props, Instrument instrument);
	
	void setInstrument(Instrument instrument);
	Instrument getInstrument();
	
	void setDataSourceName(String name);
	String getDataSourceName();
	
	String getTransformerClassName();
	void setTransformerClassName(String cname);
	
	public static enum Preference {
		Up(0), Down(1), Random(2);
		Preference(int val)  { this.value = val;}
		private final int value;
	    public int value() { return value; }
	}
}
