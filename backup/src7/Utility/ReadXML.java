package Utility;

import Launcher.*;
import LauncherDestroyer.*;
import MissileDestroyer.*;
import War.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ReadXML {

	public static Map<String,Launcher> getLaunchersFromXML(Document doc) {
		
		Map<String,Launcher> launcherMap = new HashMap<String,Launcher>();
		
		Element missileLaunchers = (Element)(doc.getElementsByTagName("missileLaunchers").item(0));
		NodeList launchersXML = missileLaunchers.getElementsByTagName("launcher");

		for (int i = 0; i < launchersXML.getLength(); i++) {
			
			Node launcherNode = launchersXML.item(i);

			if (launcherNode.getNodeType() == Node.ELEMENT_NODE) {
				Element launcher = (Element) launcherNode;
												
				String id = launcher.getAttribute("id");
				boolean isHidden = Boolean.parseBoolean(launcher.getAttribute("isHidden"));

				/* create minimum heap to store missiles, sorted by launch time */
				Heap<Missile> missileHeap = new Heap<Missile>(Launcher.missileComparator);
				
				Launcher l = new Launcher(id,isHidden,missileHeap);
				
				NodeList missilesXML = launcher.getElementsByTagName("missile");
				
				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;
						Missile m = new Missile	(missile.getAttribute("id"),
												missile.getAttribute("destination"),
												Integer.parseInt(missile.getAttribute("launchTime")),
												Integer.parseInt(missile.getAttribute("flyTime")),
												Integer.parseInt(missile.getAttribute("damage")),l);
						
						l.addMissile(m);
						War.increaseInitThreadCount();
					}
				}
				launcherMap.put(l.getID(),l);
				War.increaseInitThreadCount();
			}
		}
		return launcherMap;
	}

	public static Map<String,IronDome> getIronDomesFromXML(Document doc) {
		
		Map<String,IronDome> domeMap = new HashMap<String,IronDome>();
		
		Element IronDomes = (Element)(doc.getElementsByTagName("missileDestructors").item(0));
		NodeList domesXML = IronDomes.getElementsByTagName("destructor");

		for (int i = 0; i < domesXML.getLength(); i++) {

			Node domeNode = domesXML.item(i);

			if (domeNode.getNodeType() == Node.ELEMENT_NODE) {
				Element dome = (Element) domeNode;

				String id = dome.getAttribute("id");

				/* create minimum heap to store missiles to destroy, sorted by destroy time */
				Heap<Target> missileHeap = new Heap<Target>(Target.targetComparator);

				NodeList missilesXML = dome.getElementsByTagName("destructdMissile");

				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;

						String missileID = missile.getAttribute("id");

						// find the missile in the launchers
						Missile m = War.getMissile(missileID);
						
						if (m != null) {	// if missile exist
							int destroyTime = Integer.parseInt(missile.getAttribute("destructAfterLaunch"));
							Target t = new Target(m,destroyTime);
							missileHeap.add(t);
						}
						
					}
				}
				IronDome irond = new IronDome(id,missileHeap);
				domeMap.put(irond.getID(), irond);
				War.increaseInitThreadCount();
			}
		}
		return domeMap;
	}

	public static List<LauncherDestroyer> getLauncherDestroyersFromXML(Document doc) {
		
		List<LauncherDestroyer> launcherDestroyerList = new ArrayList<LauncherDestroyer>();

		Element launcherDestroyers = (Element)(doc.getElementsByTagName("missileLauncherDestructors").item(0));
		NodeList destroyersXML = launcherDestroyers.getElementsByTagName("destructor");

		for (int i = 0; i < destroyersXML.getLength(); i++) {

			Node destroyerNode = destroyersXML.item(i);

			if (destroyerNode.getNodeType() == Node.ELEMENT_NODE) {
				Element destroyer = (Element) destroyerNode;
				
				String type = destroyer.getAttribute("type");
				
				/* create minimum heap to store missile launchers to destroy, sorted by destroy time */
				Heap<Target> launcherHeap = new Heap<Target>(Target.targetComparator);

				NodeList launchersXML = destroyer.getElementsByTagName("destructedLanucher");

				for (int j = 0; j < launchersXML.getLength(); j++) {

					Node launcherNode = launchersXML.item(j);

					if (launcherNode.getNodeType() == Node.ELEMENT_NODE) {
						Element launcher = (Element) launcherNode;
						
						String launcherID = launcher.getAttribute("id");
						
						// find the launcher
						Launcher l = War.getLauncher(launcherID);
						
						if ( l != null ) {	// if launcher exist
							int destroyTime = Integer.parseInt(launcher.getAttribute("destructTime"));
							Target t = new Target(l,destroyTime);
							launcherHeap.add(t);
						}

					}
				}
				
				if (type.compareTo("plane") == 0) {
					LauncherDestroyer ld = new Aircraft(launcherHeap);
					launcherDestroyerList.add(ld);
					War.increaseInitThreadCount();
				}
				
				if (type.compareTo("ship") == 0) {
					LauncherDestroyer ld = new Battleship(launcherHeap);
					launcherDestroyerList.add(ld);
					War.increaseInitThreadCount();
				}
				
			}
		}
		return launcherDestroyerList;
	}

}
