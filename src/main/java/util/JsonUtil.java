package util;

import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class JsonUtil {
	static final org.apache.log4j.Logger log = Logger.getLogger(JsonUtil.class);
	public static final String CONFIG_FILENAME = "/config.properties";

	public static void addObjectToMap(IMapped<String> cf, Map<String,IMapped<String>> map) {
		map.put(cf.getName(), cf);
		IMapped<String> iMapped = (IMapped<String>)cf;
		Set<String> keyList = iMapped.keySet();
		if(keyList != null && keyList.size() > 0) {
			for(String key : keyList) {
				if(!map.containsKey(key)) {
					map.put(key, cf);
				}
			}
		}
	}

}
