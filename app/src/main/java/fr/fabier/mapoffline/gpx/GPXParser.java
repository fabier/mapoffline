package fr.fabier.mapoffline.gpx;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.location.Location;
import android.util.Xml;

public class GPXParser {

	private static List<Node> getChildNodes(Node node, String nodeName) {
		List<Node> nodeList = new ArrayList<>();
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeName().equals(nodeName)) {
				nodeList.add(child);
			}
		}
		return nodeList;
	}

	private static Node getFirstChildNode(Node node, String nodeName) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeName().equals(nodeName)) {
				return child;
			}
		}
		return null;
	}

	public GPXParser() {
	}

	public GPX parse(InputStream in) {
		GPX gpx = new GPX();
		try {

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(in);

			Element elementRoot = document.getDocumentElement();
			Node n = elementRoot.getFirstChild();
			System.out.println("n = " + n.getNodeName());
			NodeList trkNodes = elementRoot.getElementsByTagName("trk");
			for (int i = 0; i < trkNodes.getLength(); i++) {
				gpx.add(processTrk(trkNodes.item(i)));
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gpx;
	}

	private Track processTrk(Node trkNode) {
		Track track = new Track();
		System.out.println("trkNode = " + trkNode.getNodeName());
		List<Node> trkSegNodes = getChildNodes(trkNode, "trkseg");
		for (Node trkSegNode : trkSegNodes) {
			track.add(processTrkSeg(trkSegNode));
		}
		return track;
	}

	private TrackSeg processTrkSeg(Node trkSegNode) {
		TrackSeg trackSeg = new TrackSeg();
		System.out.println("trkNode = " + trkSegNode.getNodeName());
		List<Node> trkPtNodes = getChildNodes(trkSegNode, "trkpt");
		for (Node trkPtNode : trkPtNodes) {
			trackSeg.add(processTrkPt(trkPtNode));
		}
		return trackSeg;
	}

	private TrackPoint processTrkPt(Node trkPtNode) {
		TrackPoint trackPoint = new TrackPoint();
		System.out.println("trkPtNode = " + trkPtNode.getNodeName());
		NamedNodeMap attributes = trkPtNode.getAttributes();

		trackPoint.setLatitude(Double.parseDouble(attributes.getNamedItem("lat").getTextContent()));
		trackPoint.setLongitude(Double.parseDouble(attributes.getNamedItem("lon").getTextContent()));

		Node trkEleNode = getFirstChildNode(trkPtNode, "ele");
		if (trkEleNode != null) {
			trackPoint.setAltitude(Double.parseDouble(trkEleNode.getTextContent()));
		}
		return trackPoint;
	}
}