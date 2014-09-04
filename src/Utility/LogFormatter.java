/* Ariel Levin */

package Utility;

import War.War;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	// used to add new line in log files
	public static final String newLine = "\r\n" + String.format("%35s", ">> ");
	
	private War war;	// stored to have access to War Time
	
	
	public LogFormatter(War war) {
		super();
		this.war = war;
	}

	@Override
	public String format(LogRecord record) {
		
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String str = f.format(java.util.Calendar.getInstance().getTime());
		str += " (" + war.getTime() + ") > ";
		
		return String.format("%-32s", str) + record.getMessage() + "\r\n";
	}

}