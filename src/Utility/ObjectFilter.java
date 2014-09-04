/* Ariel Levin */

package Utility;

import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class ObjectFilter implements Filter {

	private Object filtered;
	
	public ObjectFilter(Object toFilter) {
		filtered = toFilter;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		if (record.getParameters() != null) {
			Object temp = record.getParameters()[0];
			return filtered == temp;
		}
		return false;
	}
	
}