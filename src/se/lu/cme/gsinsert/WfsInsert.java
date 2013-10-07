package se.lu.cme.gsinsert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/*/******************************COPYRIGHT***********************************
 * This file is part of the GeoserverInsert application.					*
 * Copyright &copy; 2013 Lund University.										*
 * 																			*
 *********************************LICENSE************************************
 * GeoserverInsert is free software: you can redistribute it and/or modify 	*
 * it under	the terms of the GNU General Public License as published by the *
 * Free Software Foundation, either version 3 of the License, or (at your	*
 * option) any later version.												*
 *																			*
 * GeoserverInsert is distributed in the hope that it will be useful, but	*
 * WITHOUT ANY WARRANTY; without even the implied warranty of				*
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General *
 * Public License for more details.											*
 *																			*
 * You should have received a copy of the GNU General Public License along	*
 * with GeoserverInsert. If not, see "http://www.gnu.org/licenses/".		*
 * 																			*
 * The latest source for this software can be accessed at					*
 * "github.org/mattiassp/geoserverinsert".									*
 * 																			*
 * For other enquiries, e-mail to: mattias.spangmyr@gmail.com				*
 * 																			*
 ****************************************************************************/
/**
 * Class for making WFS Transaction Insert requests to a Geoserver
 * instance and parsing the response XML to announce the result.
 * 
 * @author Mattias Sp&aring;ngmyr
 * @version 0.2, 2013-10-07
 */
public class WfsInsert implements Runnable {
	private static final String NAMESPACE = "ns";
	private GSInsert mUi;
	private String mUrl;
	private String mUser;
	private String mPass;
	private String mLayer;
	private String mNamespace;
	private HashMap<String, String> mAttributes;
	/** A list of all the fields' field types, by their name. */
	private HashMap<String, String> mFieldTypes = new HashMap<String, String>();
	private boolean mSuccess = false;

	/**
	 * Basic constructor for creating this WfsInsert object.
	 * @param ui The main GSInsert UI object to post the result to.
	 * @param url The web URL to send the data to.
	 * @param user The user name in case authentication is required.
	 * @param pass The password in case authentication is required.
	 * @param layer The layer name of the layer to update.
	 * @param attributes The attributes to give the new feature.
	 * @param fieldtypes The field types of the layer's fields.
	 */
	public WfsInsert(GSInsert ui, String url, String user, String pass, String layer, String namespace, HashMap<String, String> attributes, HashMap<String, String> fieldtypes) {
		mUi = ui;
		mUrl = url;
		mUser = user;
		mPass = pass;
		mLayer = layer;
		mNamespace = namespace;
		mAttributes = attributes;
		mFieldTypes = fieldtypes;
	}

	@Override
	public void run() {
		BufferedReader reply = makeRequest();
		if(reply == null) // Stop immediately if the request did not generate a response.
			return;

		/* Send the request to be interpreted and alert the user
		 * about the response. */
		if(parseXml(reply)) {
			if(mSuccess) {
				mUi.displayAlert(GSInsert.INSERT_SUCCESS_TITLE, GSInsert.INSERT_SUCCESS_MESSAGE, JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Upload success.");
			}
			else {
				mUi.displayAlert(GSInsert.INSERT_FAILURE_TITLE, GSInsert.INSERT_FAILURE_MESSAGE, JOptionPane.ERROR_MESSAGE);
				System.out.println("Upload failed.");
			}
		}
		else
			mUi.displayAlert(GSInsert.PARSE_ERROR_TITLE, GSInsert.PARSE_ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);

	}
	
	/**
	 * Generate the URL and make a request to it.
	 * @return A BufferedReader as the response to the request, or null if the request failed.
	 */
	private BufferedReader makeRequest() {
		URL request;
		String authtext = "";
		if(!mUser.equalsIgnoreCase("") && !mPass.equalsIgnoreCase("")) // If the user provided both username and password, include them.
			authtext = mUser + ":" + mPass + "@";
		
		URL url;
		try { // Check that the URL is ok and form the request URL.
			url = new URL(mUrl);
			System.out.println(url.getProtocol() + "://" + authtext +
					mUrl.substring(url.getProtocol().length() + 3) +
					"/wfs");
			request = new URL(url.getProtocol() + "://" + authtext +
					mUrl.substring(url.getProtocol().length() + 3) +
					"/wfs");
		} catch (MalformedURLException e) {
			mUi.displayAlert(GSInsert.URL_WARNING_MESSAGE, GSInsert.URL_WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
			return null;
		}
		
		String insertXml = formXml();
		System.out.println(insertXml);
		
		/* Perform the request. */
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) request.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("content-type", "text/xml");
			DataOutputStream stream = new DataOutputStream(con.getOutputStream());
			stream.write(insertXml.getBytes("UTF-8"));
			stream.flush();
			stream.close();

			BufferedReader response = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			mUi.displayAlert(GSInsert.CON_ERROR_MESSAGE, GSInsert.CON_ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
			return null;
		}		
	}

	/**
	 * Forms an Xml String based on the layer and
	 * attributes to send to a Geoserver instance.
	 * @return The Xml String containing a WFS Transaction Insert message.
	 */
	private String formXml() {
		String xml = "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" handle=\"GeoserverInsert" + (mUser.equalsIgnoreCase("") ? "" : ":" + mUser) + "\"" +
				" xmlns:" + NAMESPACE + "=\"" + mNamespace + "\"" +
				" xmlns:wfs=\"http://www.opengis.net/wfs\"" +
				" xmlns:gml=\"http://www.opengis.net/gml\"" +
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
				" xsi:schemaLocation=\"" +
				mUrl + "/DescribeFeatureType?typename=" + mLayer +
				" http://www.opengis.net/wfs\">";
		
		xml = xml + formInsertElement() + " </wfs:Transaction>";

		return xml;
	}

	/**
	 * Forms a single string containing the Insert XML element of a
	 * WFS Transaction Insert request.
	 * @return A string with the Insert element of a WFS-T Insert XML. 
	 */
	private String formInsertElement() {
		/* Get the un-prefixed layer name. */
		String[] layerparts = mLayer.split(":");
		String layername = layerparts[layerparts.length - 1];

		/* The parts of the Insert statements to form. */
		String wfsStart = " <wfs:Insert>" + " <" + NAMESPACE + ":" + layername + ">";
		String fieldStart = "";
		String attributes = "";
		String fieldEnd = "";
		String wfsEnd = " </" + NAMESPACE + ":" + layername + ">" + " </wfs:Insert>";
		String insert = wfsStart; // The total resulting String.
		
		String SRS = "srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\"";

		/* Go through and add all fields. */
		Set<String> keyset = mAttributes.keySet();// Fetch the set of keys pertaining to the layer attributes.
		for(String field : keyset) {
			
			/* Get the un-prefixed field name. */
			String[] fieldparts = field.split(":");
			String fieldname = fieldparts[fieldparts.length - 1];
			
			fieldStart = " <" + NAMESPACE + ":" + fieldname + ">";
			fieldEnd = "</" + NAMESPACE + ":" + fieldname + ">";
			
			/* Form the element starters and enders required by the corresponding geometry type before the coordinates. */
			if(mFieldTypes.get(field).equalsIgnoreCase("gml:PointPropertyType")) {
				fieldStart = fieldStart + " <gml:Point " + SRS + ">";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:Point>" + fieldEnd;
			}
			else if(mFieldTypes.get(field).equalsIgnoreCase("gml:MultiPointPropertyType")) {
				fieldStart = fieldStart + " <gml:MultiPoint " + SRS + ">" + " <gml:pointMember>" + " <gml:Point>";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:Point>" + " </gml:pointMember>" + " </gml:MultiPoint>" + fieldEnd;
			}
			else if(mFieldTypes.get(field).equalsIgnoreCase("gml:LineStringPropertyType")) {
				fieldStart = fieldStart + " <gml:LineString " + SRS + ">";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:LineString>" + fieldEnd;
			}
			else if(mFieldTypes.get(field).equalsIgnoreCase("gml:MultiLineStringPropertyType")) {
				fieldStart = fieldStart + " <gml:MultiLineString " + SRS + ">" + " <gml:lineStringMember>" + " <gml:LineString>";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:LineString>" + " </gml:lineStringMember>" + " </gml:MultiLineString>" + fieldEnd;
			}
			else if(mFieldTypes.get(field).equalsIgnoreCase("gml:PolygonPropertyType") || mFieldTypes.get(field).equalsIgnoreCase("gml:SurfacePropertyType")) {
				fieldStart = fieldStart + " <gml:Polygon " + SRS + ">" + " <gml:outerBoundaryIs>" + " <gml:LinearRing>";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:LinearRing>" + " </gml:outerBoundaryIs>" + " </gml:Polygon>" + fieldEnd;
			}
			else if(mFieldTypes.get(field).equalsIgnoreCase("gml:MultiPolygonPropertyType") || mFieldTypes.get(field).equalsIgnoreCase("gml:MultiSurfacePropertyType")) {
				fieldStart = fieldStart + " <gml:MultiPolygon " + SRS + ">" + " <gml:polygonMember>" + " <gml:Polygon>" + " <gml:outerBoundaryIs>" + " <gml:LinearRing>";
				attributes = formCoordinates(fieldname);
				fieldEnd = " </gml:LinearRing>" + " </gml:outerBoundaryIs>" + " </gml:Polygon>" + " </gml:polygonMember>" + " </gml:MultiPolygon>" + fieldEnd;
			}
			else { // If it's not a geometry field, just add the user input.
				attributes = mAttributes.get(field);
			}
			
			insert = insert + fieldStart + attributes + fieldEnd; // Add all the parts of the current field to the resulting Insert String.
		}		
		insert = insert + wfsEnd; // Add the end tags to the Insert element.

		return insert;
	}

	/**
	 * Forms the coordinates element to include as geometry content
	 * in the Insert element.
	 * @param field The name of the field that contains the geometry text.
	 * @return The coordinate element as a single String.
	 */
	private String formCoordinates(String field) {
		String coords = " <gml:coordinates decimal=\".\" cs=\" \" ts=\",\">";
		String[] coordinates = mAttributes.get(field).split("\n");

		/* Form the Coordinate GML. */
		for(int i=0; i < coordinates.length; i++)  { // For each coordinate:
			if(i != 0)
				coords = coords + ","; // Insert commas between coordinates.
			coords = coords + coordinates[i];
		}
		return coords + "</gml:coordinates>";
	}

	/**
	 * Parses an XML response from a WFS Transaction Insert request.
	 * @param xml A reader wrapped around an InputStream containing the XML response from a WFS Transaction Insert request.
	 * @return True if the XML was parsed without errors.
	 */
	private boolean parseXml(BufferedReader xml) {
		try {
			SAXParserFactory spfactory = SAXParserFactory.newInstance(); // Make a SAXParser factory.
			spfactory.setValidating(false); // Tell the factory not to make validating parsers.
			SAXParser saxParser = spfactory.newSAXParser(); // Use the factory to make a SAXParser.
			XMLReader xmlReader = saxParser.getXMLReader(); // Get an XML reader from the parser, which will send event calls to its specified event handler.
			XmlEventHandler handler = new XmlEventHandler(); // Create the event handler to use.
			xmlReader.setContentHandler(handler); // Set where to send content calls.
			xmlReader.setErrorHandler(handler); // Also set where to send error calls.
			xmlReader.parse(new InputSource(xml)); // Make an InputSource from the XML input to give to the reader and start parsing the XML.
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Receiver class for XML parser callbacks.
	 */
	private class XmlEventHandler extends DefaultHandler {
		private static final String ELEMENT_SUCCESS = "wfs:SUCCESS";

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			System.out.print("<" + qName + ">");
			if(qName.equalsIgnoreCase(ELEMENT_SUCCESS))
				mSuccess = true;
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			System.out.print(new String(ch, start, length) + "\n");
			super.characters(ch, start, length);
		}
		
		
	}
}
