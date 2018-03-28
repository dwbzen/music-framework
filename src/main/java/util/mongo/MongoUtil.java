package util.mongo;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;


public class MongoUtil {

	public static enum UNITS {KM, MILES, RADIANS};
	
	public final static Map<UNITS, Double> conversionMap = new HashMap<UNITS, Double>();
	public final static Map<String, UNITS> unitsMap = new HashMap<String, UNITS>();
	public final static Map<UNITS, String> unitLabels =  new HashMap<UNITS, String>();
	
	static {
		conversionMap.put(UNITS.KM, 6371.0);
		conversionMap.put(UNITS.MILES, 3959.0);
		conversionMap.put(UNITS.RADIANS, 1.0);
		unitsMap.put("km", UNITS.KM);
		unitLabels.put(UNITS.KM, "km:");
		unitsMap.put("miles", UNITS.MILES);
		unitLabels.put(UNITS.MILES, "miles:");
		unitsMap.put("radians", UNITS.RADIANS);
		unitLabels.put(UNITS.RADIANS, "radians:");
	}
	
	
	public static Map<UNITS, Double> getConversionmap() {
		return conversionMap;
	}


	public static Map<String, UNITS> getUnitsmap() {
		return unitsMap;
	}


	public static Map<UNITS, String> getUnitlabels() {
		return unitLabels;
	}
	
	/**
	 * Convert a mongo DBObject to a JSON object string
	 * @param recnum optional record# to use for query. Omitted if <0
	 * @param obj the DBObject to convert
	 * @return JSON String document
	 */
	public static String recordToJSONString(int recnum, Document obj ) {
		StringBuffer sbuf = new StringBuffer( (recnum<0) ? "{ " : "rec:" + String.valueOf(recnum) + ", { ");
		sbuf.append(obj.toString());
		return sbuf.toString();
	}

	/**
	 * Convert a mongo DBObject to a JSON object string
	 * @param recnum optional record# to use for query. Omitted if <0
	 * @param obj the DBObject to convert
	 * @param fields a String[] of field names
	 * @return JSON String document
	 */
	public static String recordToJSONString(int recnum, Document obj, String[] fields ) {
		StringBuffer sbuf = new StringBuffer( (recnum<0) ? "{ " : "rec:" + String.valueOf(recnum) + ", { ");
		int fnum=0;
		for(String fieldname:fields) {
			++fnum;
			if(obj.containsKey(fieldname)) {
				if(fnum>1) {
					sbuf.append(", ");
				}
				sbuf.append(fieldname + ":");
				sbuf.append(obj.get(fieldname));
			}
		}
		sbuf.append(" }"); // close JSON object
		return sbuf.toString();
	}

}

