package org.dwbzen.music;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dwbzen.music.ScorePart.State;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.mongo.MongoDBDataSource;
import org.dwbzen.util.music.DataSource;
import org.dwbzen.util.music.FileDataSource;
import org.dwbzen.util.music.RandomDataSource;

public class DataLoader  implements Runnable {
	static final Logger log = LogManager.getLogger(ProductionFlow.class);
	
	private String instrumentName = null;
	private Properties configProperties = null;
	private MessageProducer producer = null;
	private String dataSourceName = null;
	private Configuration configuration = null;
	
	private Session session = null;
	
	private ScorePart.State state = State.UNKNOWN;
	
	/**
	 * Loads data for a given instrument. The data source if file or mongodb will be a json file.
	 * @param instrumentName the name of the instrument (from config.properties)
	 * @param configuration Configuration instance
	 * @param producer the java.jms.MessageProducer for this instrument
	 * @param dataSourceName the name of the data source: random, file, or mongodb.
	 * @param connection a java.jms.Connection to ApacheMQ
	 * @param session a javax.jms.Session instance to create a java.jms.TextMessage
	 */
	public DataLoader(String instrumentName, Configuration configuration, MessageProducer producer, String dataSourceName, Session session) {
		state = State.INIT;
		this.configuration = configuration;
		this.instrumentName = instrumentName;
		this.producer = producer;
		configProperties = configuration.getProperties();
		this.session = session;
		this.dataSourceName = dataSourceName;
	}
	
	public ScorePart.State getState() {
		return state;
	}

	@Override
	public void run() {
		log.info("DataLoader(" +instrumentName + ")");
		try {
			updateState(State.WORKING);
			String instrumentSource = configProperties.getProperty("dataSource." + instrumentName);
			log.info("loadData for " + instrumentName + " source: " + instrumentSource);
			DataSource ds = null;
			
			/**********************************************************
			 * Load data from random source
			 **********************************************************/
			if(dataSourceName.equalsIgnoreCase("random") || instrumentSource.equalsIgnoreCase("random")) {
				ds = new RandomDataSource(configuration, instrumentName);
			}
			
			/********************************************************
			 * Load source data from a file
			 ********************************************************/
			else if(dataSourceName.equalsIgnoreCase("file") || instrumentSource.equalsIgnoreCase("file")) {
				ds = new FileDataSource(configuration, instrumentName);
			}
							
	    	/***************************************************
	    	 * Load data from MongoDB
	    	 ***************************************************/
			else if(dataSourceName.equalsIgnoreCase("mongodb") || instrumentSource.equalsIgnoreCase("mongodb")) {
				ds = new MongoDBDataSource(configuration, instrumentName);
			}
			/********************************************************
			 * TODO Load data from an IteratedFunctionSystem builtin
			 ********************************************************/
			else if(instrumentSource.equalsIgnoreCase("ifs")) {
				// ds = new IfsDataSource(configuration, instrumentName);
			}
			ds.stream().forEach(rec ->{
				try {
					producer.send(session.createTextMessage(rec));
				} catch (JMSException e) {
					// this is bad, this is VERY bad
					log.error("JMSException " + e.toString() + " rec: " + rec);
					log.error(" instrument: " + instrumentName);
					return;
				}
			});
			ds.close();
			updateState(State.COMPLETE);
		} catch (Exception e) {
			log.error("loadData exception: " +e.toString());
			e.printStackTrace(System.err);
			updateState(State.ERROR);
		}
		return;
	}
	
	synchronized void updateState(State s) {
		state = s;
	}
}
