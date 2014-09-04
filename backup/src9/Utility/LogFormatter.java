package Utility;

import War.War;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	public static final String newLine = "\r\n\t\t\t>> ";
	
	private War war;
	
	
	public LogFormatter(War war) {
		super();
		this.war = war;
	}

	@Override
	public String format(LogRecord record) {
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String timeStamp = f.format(java.util.Calendar.getInstance().getTime());
		return timeStamp + " (" + war.getTime() + ") >\t" + record.getMessage() + "\r\n";
	}

}