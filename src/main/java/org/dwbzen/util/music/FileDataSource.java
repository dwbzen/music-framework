package org.dwbzen.util.music;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.dwbzen.util.Configuration;

/**
 * Streams text data, line by line with no trailing delimiters, from a Json File
 * 
 * @author don_bacon
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
		/*
		/* typically: IFS,Point2D,message,stats
		 *
		 */
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
			stream1 = Files.lines(path).filter(w -> (w.contains(filters[0]) || w.contains(filters[3])) );	// "type":"IFS" or "type":"stats"
			if(randomSelection) {	// "type":"Point2D"
				stream2 = Files.lines(path).filter(w -> w.contains(filters[1])).skip(randomPredicate.getAsInt()).limit(maxSize);
			}
			else {
				stream2 = Files.lines(path).filter(w -> w.contains(filters[1])).limit(maxSize);
			}
			stream3 = Files.lines(path).filter(w -> w.contains(filters[2]));	// "type":"message"
			stream = Stream.concat(Stream.concat(stream1, stream2), stream3);
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
		return stream;
	}
    
	@Override
	/**
	 * This will also invoke close handlers for input streams
	 */
	public void close() {
		stream.close();
	}
}
