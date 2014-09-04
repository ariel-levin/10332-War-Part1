package Utility;

import Launcher.*;
import LauncherDestroyer.*;
import MissileDestroyer.*;
import War.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
				Heap<Missile> missileHeap = new Heap<Missile>(new Comparator<Missile>() {
					@Override
					public int compare(Missile m1, Missile m2) {
						return ( (Integer)m1.getLaunchTime() ).compareTo(m2.getLaunchTime())*(-1);
					}
				});
				
				NodeList missilesXML = launcher.getElementsByTagName("missile");

				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;
						Missile m = new Missile	(missile.getAttribute("id"),
												missile.getAttribute("destination"),
												Integer.parseInt(missile.getAttribute("launchTime")),
												Integer.parseInt(missile.getAttribute("flyTime")),
												Integer.parseInt(missile.getAttribute("damage")));
						
						missileHeap.add(m);
						War.increaseThreadCount();
					}
				}
				
				Launcher l = new Launcher(id,isHidden,missileHeap); 
				launcherMap.put(l.getID(),l);
				War.increaseThreadCount();
			}
		}
		return launcherMap;
	}

	public static Map<String,MissileDestroyer> getMissileDestroyersFromXML(Document doc, Map<String,Launcher> launcherMap) {
		
		Map<String,MissileDestroyer> destroyerMap = new HashMap<String,MissileDestroyer>();
		
		// launcher collection to find the given missile from XML in the existing Launcher's missiles
		Collection<Launcher> launcherCol = launcherMap.values();
		
		Element launcherDestroyers = (Element)(doc.getElementsByTagName("missileDestructors").item(0));
		NodeList destroyersXML = launcherDestroyers.getElementsByTagName("destructor");

		for (int i = 0; i < destroyersXML.getLength(); i++) {

			Node destroyerNode = destroyersXML.item(i);

			if (destroyerNode.getNodeType() == Node.ELEMENT_NODE) {
				Element destroyer = (Element) destroyerNode;

				String id = destroyer.getAttribute("id");

				/* create minimum heap to store missiles to destroy, sorted by destroy time */
				Heap<Target> missileHeap = new Heap<Target>(new Comparator<Target>() {
					@Override
					public int compare(Target t1, Target t2) {
						return ( (Integer)t1.getDestroyTime() ).compareTo(t2.getDestroyTime())*(-1);
					}
				});

				NodeList missilesXML = destroyer.getElementsByTagName("destructdMissile");

				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;

						String missileID = missile.getAttribute("id");

						// find the missile in the launchers
						Missile m = null;
						java.util.Iterator<Launcher> it = launcherCol.iterator();
						while (it.hasNext() && m==null)
							m = it.next().getMissile(missileID);
						
						if (m != null) {	// if missile found
							int destroyTime = Integer.parseInt(missile.getAttribute("destructAfterLaunch"));
							Target t = new Target(m,destroyTime);
							missileHeap.add(t);
						}
						
					}
				}
				MissileDestroyer md = new MissileDestroyer(id,missileHeap);
				destroyerMap.put(md.getID(), md);
				War.increaseThreadCount();
			}
		}
		return destroyerMap;
	}

	public static List<LauncherDestroyer> getLauncherDestroyersFromXML(Document doc, Map<String,Launcher> launcherMap) {
		
		List<LauncherDestroyer> launcherDestroyerList = new ArrayList<LauncherDestroyer>();

		Element launcherDestroyers = (Element)(doc.getElementsByTagName("missileLauncherDestructors").item(0));
		NodeList destroyersXML = launcherDestroyers.getElementsByTagName("destructor");

		for (int i = 0; i < destroyersXML.getLength(); i++) {

			Node destroyerNode = destroyersXML.item(i);

			if (destroyerNode.getNodeType() == Node.ELEMENT_NODE) {
				Element destroyer = (Element) destroyerNode;
				
				String type = destroyer.getAttribute("type");
				
				/* create minimum heap to store missile launchers to destroy, sorted by destroy time */
				Heap<Target> launcherHeap = new Heap<Target>(new Comparator<Target>() {
					@Override
					public int compare(Target t1, Target t2) {
						return ( (Integer)t1.getDestroyTime() ).compareTo(t2.getDestroyTime())*(-1);
					}
				});

				NodeList launchersXML = destroyer.getElementsByTagName("destructedLanucher");

				for (int j = 0; j < launchersXML.getLength(); j++) {

					Node launcherNode = launchersXML.item(j);

					if (launcherNode.getNodeType() == Node.ELEMENT_NODE) {
						Element launcher = (Element) launcherNode;
						
						String launcherID = launcher.getAttribute("id");
						
						// if launcher exist
						if (launcherMap.containsKey(launcherID)) {
							Launcher l = launcherMap.get(launcherID);
							int destroyTime = Integer.parseInt(launcher.getAttribute("destructTime"));
							Target t = new Target(l,destroyTime);
							launcherHeap.add(t);
						}

					}
				}
				
				if (type.compareTo("plane") == 0) {
					LauncherDestroyer ld = new Aircraft(launcherHeap);
					launcherDestroyerList.add(ld);
					War.increaseThreadCount();
				}
				
				if (type.compareTo("ship") == 0) {
					LauncherDestroyer ld = new Battleship(launcherHeap);
					launcherDestroyerList.add(ld);
					War.increaseThreadCount();
				}
				
			}
		}
		return launcherDestroyerList;
	}

}
