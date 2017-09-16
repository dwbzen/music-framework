package util.music;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.instrument.Koto;
import util.Configuration;

/**
 * Streams text data, line by line with no trailing delimiters, from a File
 * Statically creates and registers Scale and ScaleFormulas
 * Each instrument will have a FileDataSource.
 * 
 * @author donbacon
 *
 */
public class FileDataSource extends DataSource {

	private String[] filters;
	private Path path;
	private String filename;
	private String filePath;
	private String fileKey;
	
	public FileDataSource(Configuration config, String instrumentName) {
		super(config, instrumentName);
	}

	@Override
	/**
	 * file.name is specified by instrument
	 * There will be a FileDataSource for each instrument.
	 * @throws IllegalArgumentException if no find the source
	 */
	public void configure() {
		fileKey = "dataSource.".concat(instrumentName);
		// typically: stats,point,message
		String queryString = configuration.getProperties().getProperty("dataSource.file.queryString");
		filters = queryString.split(",");
		filePath = configuration.getProperties().getProperty("dataSource.file.path");
		if(configuration.getProperties().containsKey(fileKey)) {
			filename = filePath + configuration.getProperties().getProperty(fileKey).concat(".json");
			path = FileSystems.getDefault().getPath(filename);
		}
		else {
			throw new IllegalArgumentException("No configured source for instrument: " + fileKey);
		}
	}

	@Override
	public Stream<String> stream()  {
		Stream<String> stream1 = null;
		Stream<String> stream2 = null;
		Stream<String> stream3 = null;
		try {
			stream1 = Files.lines(path).filter(w -> w.contains(filters[0]));
			if(randomSelection) {
				stream2 = Files.lines(path).filter(w -> w.contains(filters[1])).skip(randomPredicate.getAsInt()).limit(maxSize);
			}
			else {
				stream2 = Files.lines(path).filter(w -> w.contains(filters[1])).limit(maxSize);
			}
			stream3 = Files.lines(path).filter(w -> w.contains(filters[2]));
			stream = Stream.concat(Stream.concat(stream1, stream2), stream3);
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
		return stream;
	}

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
    
	@Override
	public void close() {
		stream.close();
	}
}
