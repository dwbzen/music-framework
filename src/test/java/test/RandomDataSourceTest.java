package test;

import util.Configuration;
import util.music.RandomDataSource;

public class RandomDataSourceTest {

	
	public static void main(String... args) {
	   	Configuration config = Configuration.getInstance("/config.properties");
    	RandomDataSource ds = new RandomDataSource(config, "Koto");
    	ds.stream().forEach(s -> System.out.println(s));
		ds.close();
	}
}
