package Utility;

import War.War;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	public static final String newLine = "\r\n\t\t\t";
	
	@Override
	public String format(LogRecord record) {
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String timeStamp = f.format(java.util.Calendar.getInstance().getTime());
		return timeStamp + " (" + War.getTime() + ") >\t" + record.getMessage() + "\r\n";
	}

}