package org.dwbzen.music.transform;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.dwbzen.music.instrument.Instrument;

public abstract class Transformer implements ITransformer {

	protected ThreadLocalRandom random = ThreadLocalRandom.current();
	/**
	 * Instrument should be set for all and instrument-specific transformers
	 */
	protected Instrument instrument = null;
	protected String transformerClassName = null;
	protected String dataSourceName;

	public void configure(Properties props, Instrument instrument) {
		setInstrument(instrument);
		configure(props);
	}
	
	@Override
	public void accept(Layer layer) {
		transform(layer);
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getTransformerClassName() {
		return transformerClassName;
	}

	public void setTransformerClassName(String transformerClassName) {
		this.transformerClassName = transformerClassName;
	}
	
}
