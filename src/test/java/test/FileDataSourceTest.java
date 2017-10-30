package test;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.instrument.Koto;
import util.Configuration;
import util.music.DataSource;
import util.music.FileDataSource;

public class FileDataSourceTest {


    public static void main(String... args) throws Exception {
    	Koto instrument = new Koto();
    	
    	Configuration configuration = Configuration.getInstance("/config.properties");
    	String scaleName = "Hirajoshi Japan";
    	ScaleFormula sf = DataSource.getScaleFormula("common_scaleFormula.json", scaleName, instrument);
    	System.out.println(sf.toJSON());
    	String mode = sf.getMode();
		ScaleType st = sf.getScaleType();
		Scale scale = new Scale(scaleName, mode, st, Pitch.D, sf);
    	System.out.println(scale.toJSON());
		
    	FileDataSource ds = new FileDataSource(configuration, instrument.getName());
    	if(ds.stream() != null) {
    		ds.stream().forEach(s -> System.out.println(s));
    		ds.close();
    	}
    }
}
