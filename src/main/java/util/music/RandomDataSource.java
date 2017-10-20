package util.music;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import mathlib.CommandMessage;
import mathlib.Point2D;
import mathlib.PointSet;
import mathlib.ProbabilityDensityFunction;
import util.Configuration;

/**
 * Streams random data (Point2D) with bounds set by configuration
 * or defaults to X, Y >=0 and <1
 * 
 * Can be configured to return probability density function (PDF)
 * values for a given distribution.
 * Normal distribution speficied with a configured std. deviation (sigma) and mean (mu);
 * Standard normal distribution is sigma = 1, mu = 0.
 * Domain values should be chosen appropriately for a given mu and sigma. Some suggestions:
 * sigma=1, mu=0: [-2.0. 2.0]
 * sigma=1, mu=-2: [-4.0, 0.0]
 * sigma=1.5, mu=-2: [-6.0, 2.0]
 * Sigma, mu and domain values can be chosen to skew values in the upper or lower range.
 * Note that there are different PDFs for x and y point values.
 * @author donbacon
 *
 */
public class RandomDataSource  extends DataSource {
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private String dataSetName;
	private int size;
	private PointSet<Double> pointSet;
	private String startCommand;
	private String shutdownCommand;
	private double[] randomRangeX;
	private double[] randomRangeY;
	String[] rangesx;
	String[] rangesy;
	String[] distributions;
	ProbabilityDensityFunction PDFx;
	ProbabilityDensityFunction PDFy;

	public RandomDataSource(Configuration config, String instrumentName) {
		super(config, instrumentName);
	}

	@Override
	public void close() {
		// nothing to do
	}

	@Override
	public void configure() {
		size=Integer.parseInt(configProperties.getProperty("dataSource.random.size", "100"));
		randomRangeX = new double[2];
		randomRangeY = new double[2];
		parseRanges("rangeX", "rangeY");
		distributions = configProperties.getProperty("dataSource.random.distribution", "none,none").split(",");
		String[] mus = configProperties.getProperty("dataSource.random.mu", "0,0").split(",");
		String[] sigmas = configProperties.getProperty("dataSource.random.sigma", "1,1").split(",");
		ProbabilityDensityFunction[] pdfs = new ProbabilityDensityFunction[2];
		StringBuffer dsname = new StringBuffer(configProperties.getProperty("dataSource.random.dataSetName",""));
		for(int i=0; i<2; i++) {
			dsname.append(distributions[i]);
			pdfs[i] = null;
			if(distributions[i].equals("Normal")) {
				double mu = Double.parseDouble(mus[i]);
				double sigma = Double.parseDouble(sigmas[i]);
				pdfs[i] = new ProbabilityDensityFunction(mu, sigma);
			}
		}
		dataSetName = dsname.toString();
		parseRanges("domainX", "domainY");
		PDFx = pdfs[0];
		PDFy = pdfs[1];
		if(PDFx != null) { PDFx.setRange(randomRangeX); }
		if(PDFy != null) { PDFy.setRange(randomRangeY); }
	}
	
	private void parseRanges(String keyx, String keyy) {
		rangesx = configProperties.getProperty("dataSource.random." + keyx, "0.0,1.0").split(",");
		randomRangeX[0] = Double.parseDouble(rangesx[0]);
		randomRangeX[1] =  Double.parseDouble(rangesx[1]);
		rangesy = configProperties.getProperty("dataSource.random." + keyy, "0.0,1.0").split(",");
		randomRangeY[0] = Double.parseDouble(rangesy[0]);
		randomRangeY[1] =  Double.parseDouble(rangesy[1]);
	}

	/**
	 * Creates 3 JSON record types as in the examples:
	 * 
	 * {"name": <dataSetName>,"type": "message","command": "START" }
	 * {"LinearFunction": "random", "name": <dataSetName>,"type": "stats", "n": <size>,"minX": -5.957154E-7,"minY": -4.809322E-7,"maxX": 0.9961879,"maxY": 0.9981611,"minPoint":[ 0.002261410, 0.003954140 ],"maxPoint":[ 0.8678628, 0.1321371 ] }
	 * {"name": <dataSetName>, "type": "point", "Point2D": [ 0.02034002, 0.4716790 ] }
	 * {"name": <dataSetName>,"type": "message","command": "SHUTDOWN" }
	 *
	 */
	public void createDataSet() {
		pointSet = new PointSet<Double>();
		CommandMessage cmStart = new CommandMessage(dataSetName, "START");
		CommandMessage cmShutdown = new CommandMessage(dataSetName, "SHUTDOWN");
		startCommand = cmStart.toJSON();
		shutdownCommand = cmShutdown.toJSON();
		pointSet.setLinearFunction("random");
		pointSet.setName(dataSetName);
		generatePointSet();
	}

	private void generatePointSet() {
		double x;
		double y;
		Point2D<Double> point = null;
		for(int i=0; i<size; i++) {
			x = (PDFx != null) ? PDFx.randomPDF() : random.nextDouble(randomRangeX[0], randomRangeX[1]);
			y = (PDFy != null) ? PDFy.randomPDF() :  random.nextDouble(randomRangeY[0], randomRangeY[1]);
			point = new Point2D<Double>(x, y);
			point.setName(dataSetName);
			pointSet.add(point);
		}
	}

	@Override
	public Stream<String> stream() {
		createDataSet();
		Stream.Builder<String> builder =  Stream.builder();
		builder.add(startCommand);
		builder.add(pointSet.toJSON());
		for(Point2D<Double> point : pointSet.getPoints()) {
			builder.add(point.toJSON());
		}
		builder.add(shutdownCommand);
		stream = builder.build();
		return stream;
	}

	public String getDataSetName() {
		return dataSetName;
	}

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public static void main(String... args) {
	   	Configuration config = Configuration.getInstance("/config.properties");
    	RandomDataSource ds = new RandomDataSource(config, "Koto");
    	ds.stream().forEach(s -> System.out.println(s));
		ds.close();
	}

}
	
