package War;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import Launcher.*;
import LauncherDestroyer.*;
import MissileDestroyer.*;
import Utility.*;


public class ProgramMain {

	private static final String FN = "war.xml";
	
	public static void main(String[] args) {
		
		cleanDir();
		Document doc = getXMLfile(FN);

		Map<String,Launcher> launchers = ReadXML.getLaunchersFromXML(doc);
		Map<String,MissileDestroyer> missileDestroyers = ReadXML.getMissileDestroyersFromXML(doc,launchers);
		List<LauncherDestroyer> launcherDestroyers = ReadXML.getLauncherDestroyersFromXML(doc,launchers);
		
		new War(launchers,missileDestroyers,launcherDestroyers).start();
		
	}

	private static Document getXMLfile(String fileName) {
		File xmlFile = new File(FN);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;

		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		doc.getDocumentElement().normalize();
		
		return doc;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	private static void cleanDir() {
		File file = new File("D:\\Ariel.L\\10332\\Home Assignments\\10332_Home01\\logs");        
		String[] myFiles;
		if(file.isDirectory()){  
			myFiles = file.list();  
			for (int i=0; i<myFiles.length; i++) {  
				File myFile = new File(file, myFiles[i]);   
				myFile.delete();  
			}  
		} 
	}
	
	private static void testHeap() {
		
		Heap<Integer> test = new Heap<Integer>(new Comparator<Integer>() {

			public int compare(Integer x, Integer y) {

				return x.compareTo(y)*(-1);

			}
		});
		
		test.add(2);
		test.add(1);
		test.add(9);
		test.add(2);
		test.add(4);
		int size = test.getSize();
		System.out.println(size);
		for(int i = 0 ; i < size ; i++){
			System.out.print(test.getHead() + "  ");
			test.remove();
		}
		
	}


}


