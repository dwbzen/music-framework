package org.dwbzen.util.music;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.dwbzen.common.math.CommandMessage;
import org.dwbzen.common.math.Point2D;
import org.dwbzen.common.math.PointSet;
import org.dwbzen.common.math.PointSetStats;
import org.dwbzen.common.math.ProbabilityDensityFunction;
import org.dwbzen.common.math.ifs.IteratedFunctionSystem;
import org.dwbzen.util.Configuration;
import org.apache.commons.math3.util.Precision;

/**
 * Streams random data (Point2D) with bounds set by configuration or defaults to X, Y >=0 and 1<br>
 * 
 * Can be configured to return probability density function (PDF) values for a given distribution.<br>
 * Normal distribution specified with a configured std. deviation (sigma) and mean (mu)<br>
 * Standard normal distribution is sigma = 1, mu = 0.<br>
 * Domain values should be chosen appropriately for a given mu and sigma. Some suggestions:<br>
 * sigma=1, mu=0: [-2.0. 2.0]<br>
 * sigma=1, mu=-2: [-4.0, 0.0]<br>
 * sigma=1.5, mu=-2: [-6.0, 2.0]<br>
 * Sigma, mu and domain values can be chosen to skew values in the upper or lower range.<br>
 * Note that there are different PDFs for x and y point values.</p>
 * 
 * For Normal distribution, the rangeX, rangeY values are set to the domainX and domainY values
 * Otherwise, domainX, domainY are not used.
 * 
 * @see org.dwbzen.common.math.ProbabilityDensityFunction
 * @author don_bacon
 *
 */
public class RandomDataSource  extends DataSource {
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private String dataSetName;
	private int size;
	private PointSet<Double> pointSet;
	private String startCommand;
	private String shutdownCommand;
	private Point2D<Double> randomRangeX;
	private Point2D<Double> randomRangeY;
	String 	rangesx;
	String 	rangesy;
	String[] distributions;
	ProbabilityDensityFunction PDFx;
	ProbabilityDensityFunction PDFy;

	public RandomDataSource(Configuration config, String instrumentName) {
		super(config, instrumentName);
	}
	
	public RandomDataSource(Configuration config, String instrumentName, int dataSetSize) {
		super(config, instrumentName);
		setSize(dataSetSize);		// overrides the configured value "dataSource.random.size"
	} 

	@Override
	public void close() {
		// nothing to do
	}

	@Override
	public void configure() {
		size=Integer.parseInt(configProperties.getProperty("dataSource.random.size", "100"));
		parseRanges("rangeX", "rangeY");
		distributions = configProperties.getProperty("dataSource.random.distribution", "none,none").split(",");
		String[] mus = configProperties.getProperty("dataSource.random.mu", "0,0").split(",");
		String[] sigmas = configProperties.getProperty("dataSource.random.sigma", "1,1").split(",");
		ProbabilityDensityFunction[] pdfs = new ProbabilityDensityFunction[2];
		dataSetName = configProperties.getProperty("dataSource.random.dataSetName","random");
		for(int i=0; i<2; i++) {
			pdfs[i] = null;
			if(distributions[i].equalsIgnoreCase("normal")) {
				double mu = Double.parseDouble(mus[i]);
				double sigma = Double.parseDouble(sigmas[i]);
				parseRanges(i==0 ? "domainX" : null, i==1? "domainY" : null);
				pdfs[i] = new ProbabilityDensityFunction(mu, sigma);
			}
		}
		PDFx = pdfs[0];
		PDFy = pdfs[1];
		if(PDFx != null) { PDFx.setRange(randomRangeX); }
		if(PDFy != null) { PDFy.setRange(randomRangeY); }
	}
	
	private void parseRanges(String keyx, String keyy) {
		if(keyx != null) {
			rangesx = configProperties.getProperty("dataSource.random." + keyx, "[0.0,1.0]");
			randomRangeX = new Point2D<Double>(rangesx);
		}
		if(keyy != null) {
			rangesy = configProperties.getProperty("dataSource.random." + keyy, "[0.0,1.0]");
			randomRangeY = new Point2D<Double>(rangesy);
		}
	}

	/**
	 * Creates JSON record types as in the examples:</p>
	 * 
	 * {"name": <dataSetName>,"type": "message","command": "START" }<br>
	 * {"name": <dataSetName>,"type": "random" }<br>
	 * {"name": <dataSetName>,"type": "stats", ... }<br>
	 * {"name": <dataSetName>, "type": "point", "Point2D": [ 0.02034002, 0.4716790 ] }<br>
	 * {"name": <dataSetName>,"type": "message","command": "SHUTDOWN" }<br>
	 *
	 */
	public void createDataSet() {
		pointSet = new PointSet<Double>();
		pointSet.setIteratedFunctionSystem(IteratedFunctionSystem.NONE);
		CommandMessage cmStart = new CommandMessage(dataSetName, "START");
		CommandMessage cmShutdown = new CommandMessage(dataSetName, "SHUTDOWN");
		startCommand = cmStart.toJson();
		shutdownCommand = cmShutdown.toJson();
		pointSet.setName(dataSetName);
		pointSet.setDataSource(PointSet.DataSource.RANDOM);
		generatePointSet();
	}

	protected void generatePointSet() {
		double x;
		double y;
		
		Point2D<Double> point = null;
		for(int i=0; i<size; i++) {
			x = (PDFx != null) ? PDFx.randomPDF() : random.nextDouble(randomRangeX.getX().doubleValue(), randomRangeX.getY().doubleValue());
			y = (PDFy != null) ? PDFy.randomPDF() :  random.nextDouble(randomRangeY.getX().doubleValue(), randomRangeY.getY().doubleValue());
			double xrounded = Precision.round(x, 4);
			double yrounded = Precision.round(y,4);
			point = new Point2D<Double>(xrounded, yrounded);
			point.setName(dataSetName);
			pointSet.add(point);
		}
	}

	@Override
	public Stream<String> stream() {
		createDataSet();
		PointSetStats<Double> psStats = pointSet.getStats();
		Stream.Builder<String> builder =  Stream.builder();
		builder.add(startCommand);
		String pointSetStatsString = psStats.toJson();
		builder.add(pointSetStatsString);
		for(Point2D<Double> point : pointSet.getPoints()) {
			builder.add(point.toJson());
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

	public String[] getDistributions() {
		return distributions;
	}

	public void setDistributions(String[] distributions) {
		this.distributions = distributions;
	}

	public PointSet<Double> getPointSet() {
		return pointSet;
	}

}
	
