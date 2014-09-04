package Utility;

import Destroyers.*;
import Launcher.*;
import War.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** 
 * @author Ariel Levin
 * 
 * */
public class ReadXML {

	/** Get a XML Document and Returns a Launchers Map from it.
	 * War object input is used to send the Launchers and Missiles the War they belong to */
	public static Map<String,Launcher> getLaunchersFromXML(Document doc, War war) {
		
		Map<String,Launcher> launcherMap = new HashMap<String,Launcher>();
		
		Element missileLaunchers = (Element)(doc.getElementsByTagName("missileLaunchers").item(0));
		NodeList launchersXML = missileLaunchers.getElementsByTagName("launcher");

		for (int i = 0; i < launchersXML.getLength(); i++) {
			
			Node launcherNode = launchersXML.item(i);

			if (launcherNode.getNodeType() == Node.ELEMENT_NODE) {
				Element launcher = (Element) launcherNode;
												
				String id = launcher.getAttribute("id");
				boolean isHidden = Boolean.parseBoolean(launcher.getAttribute("isHidden"));

				Launcher l = new Launcher(id,isHidden,war);
				
				NodeList missilesXML = launcher.getElementsByTagName("missile");
				
				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;
						Missile m = new Missile(missile.getAttribute("id"),
												missile.getAttribute("destination"),
												Integer.parseInt(missile.getAttribute("launchTime")),
												Integer.parseInt(missile.getAttribute("flyTime")),
												Integer.parseInt(missile.getAttribute("damage")),l,war);
						
						l.addMissile(m);
					}
				}
				launcherMap.put(l.getID(),l);
			}
		}
		return launcherMap;
	}

	/** Get a XML Document and Returns an Iron Domes Map from it.
	 * War object input is used to send the Iron Domes the War they belong to */
	public static Map<String,IronDome> getIronDomesFromXML(Document doc, War war) {
		
		Map<String,IronDome> domeMap = new HashMap<String,IronDome>();
		
		Element IronDomes = (Element)(doc.getElementsByTagName("missileDestructors").item(0));
		NodeList domesXML = IronDomes.getElementsByTagName("destructor");

		for (int i = 0; i < domesXML.getLength(); i++) {

			Node domeNode = domesXML.item(i);

			if (domeNode.getNodeType() == Node.ELEMENT_NODE) {
				Element dome = (Element) domeNode;

				String id = dome.getAttribute("id");

				IronDome irond = new IronDome(id,war);

				NodeList missilesXML = dome.getElementsByTagName("destructdMissile");

				for (int j = 0; j < missilesXML.getLength(); j++) {

					Node missileNode = missilesXML.item(j);

					if (missileNode.getNodeType() == Node.ELEMENT_NODE) {
						Element missile = (Element) missileNode;

						String missileID = missile.getAttribute("id");

						// find the missile in the launchers
						Missile m = war.getMissile(missileID);
						
						if (m != null) {	// if missile exist
							int destroyTime = Integer.parseInt(missile.getAttribute("destructAfterLaunch"));
							Target t = new Target(m, destroyTime, false);
							irond.addTarget(t);
						}
						
					}
				}
				domeMap.put(irond.getID(), irond);
			}
		}
		return domeMap;
	}

	/** Get a XML Document and Returns a Launcher Destroyers Map from it.
	 * War object input is used to send the Launcher Destroyers the War they belong to */
	public static List<LauncherDestroyer> getLauncherDestroyersFromXML(Document doc, War war) {
		
		List<LauncherDestroyer> launcherDestroyerList = new ArrayList<LauncherDestroyer>();

		Element launcherDestroyers = (Element)(doc.getElementsByTagName("missileLauncherDestructors").item(0));
		NodeList destroyersXML = launcherDestroyers.getElementsByTagName("destructor");

		for (int i = 0; i < destroyersXML.getLength(); i++) {

			Node destroyerNode = destroyersXML.item(i);

			if (destroyerNode.getNodeType() == Node.ELEMENT_NODE) {
				Element destroyer = (Element) destroyerNode;
				
				String type = destroyer.getAttribute("type");
				
				LauncherDestroyer ld = new LauncherDestroyer(type,war);

				NodeList launchersXML = destroyer.getElementsByTagName("destructedLanucher");

				for (int j = 0; j < launchersXML.getLength(); j++) {

					Node launcherNode = launchersXML.item(j);

					if (launcherNode.getNodeType() == Node.ELEMENT_NODE) {
						Element launcher = (Element) launcherNode;
						
						String launcherID = launcher.getAttribute("id");
						
						// find the launcher
						Launcher l = war.getLauncher(launcherID);
						
						if ( l != null ) {	// if launcher exist
							int destroyTime = Integer.parseInt(launcher.getAttribute("destructTime"));
							Target t = new Target(l, destroyTime, false);
							ld.addTarget(t);
						}

					}
				}
				launcherDestroyerList.add(ld);
			}
		}
		return launcherDestroyerList;
	}

}
