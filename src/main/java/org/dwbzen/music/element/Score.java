package org.dwbzen.music.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.ScorePart;
import org.dwbzen.util.Configurable;
import org.dwbzen.util.Configuration;


public class Score implements Configurable, Serializable, IJson {

	/**
	 * Part name + ScorePart
	 */
	@JsonProperty	private Map<String, ScorePartEntity> parts = new HashMap<String, ScorePartEntity>();
	private static final long serialVersionUID = 7957510486314451238L;
	@JsonIgnore private Properties configProperties = null;
	@JsonIgnore private Configuration configuration = null;
	
	@JsonProperty("workNumber")	private String workNumber;		// as in "BVW1023" or K. 524
	@JsonProperty("title")		private String title;
	@JsonProperty("copyright")	private String copyright;
	@JsonProperty("name")		private String name;			// for persistence
	
	/**
	 * type, name. For example, "composer", "Don Bacon"
	 */
	@JsonIgnore		private Map<String, String> creators = new HashMap<String, String>();
	@JsonProperty("instruments")  private List<String> instrumentNames = new ArrayList<String>();

	public Score(String title) {
		this.title = title;
	}
	
	public Score(Configuration config, String title) {
		configuration = config;
		configProperties = config.getProperties();
		this.title = title;
	}
	
	public int addPart(String name, ScorePart scorePart) {
		parts.put(name, scorePart.getScorePartEntity());
		return parts.size();
	}
	public int addPart(ScorePart scorePart) {
		parts.put(scorePart.getPartName(), scorePart.getScorePartEntity());
		return parts.size();
	}
	
	public ScorePartEntity getScorePartEntityForInstrument(String instrumentName) {
		ScorePartEntity scorePartEntity = null;
		for(ScorePartEntity spe : parts.values()) {
			if(spe.getInstrument().getName().equals(instrumentName)) {
				scorePartEntity = spe;
				break;
			}
		}
		return scorePartEntity;
	}
	
	public int size() {
		return parts.size();
	}

	public String getWorkNumber() {
		return workNumber;
	}

	public void setWorkNumber(String workNumber) {
		this.workNumber = workNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, ScorePartEntity> getParts() {
		return parts;
	}

	public Map<String, String> getCreators() {
		return creators;
	}
	public void addCreator(String type, String name) {
		creators.put(type, name);
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public Properties getConfigProperties() {
		return configuration.getProperties();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public List<String> getInstrumentNames() {
		return instrumentNames;
	}
	
	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(Configuration config) {
		configuration = config;
	}
	
	@Override
	public void configure() {
		// nothing to do for Score
	}
}
