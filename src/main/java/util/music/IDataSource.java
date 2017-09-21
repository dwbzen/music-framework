package util.music;

import java.util.stream.Stream;

import util.Configurable;

public interface IDataSource extends Configurable {

	 Stream<String> stream();
	 Stream<String> getStream();
	 void close();
}
