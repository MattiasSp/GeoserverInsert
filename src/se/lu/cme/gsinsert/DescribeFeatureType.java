package se.lu.cme.gsinsert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
 * A class that sends a DescribeFeatureType request to a
 * specified URL using the provided credentials and receives
 * information about the given layer in return. This is
 * forwarded to a method translating the info into UI components. 
 * 
 * @author Mattias Sp&aring;ngmyr
 * @version 0.2, 2013-10-07
 */
public class DescribeFeatureType implements Runnable {
	private GSInsert mUi;
	private String mUrl;
	private String mUser;
	private String mPass;
	private String mLayer;
	private String mLayerNamespace;
	
	private ArrayList<LayerField> mFields = new ArrayList<LayerField>();

	/**
	 * Basic constructor that sets the url, the user, the
	 * password and the layername to use for the request.
	 * @param ui The UI to which to reply with a list of LayerFields.
	 * @param url The URL of the Geoserver instance.
	 * @param user The username to provide as authentication.
	 * @param pass The password to authenticate with.
	 * @param layer The name of the layer to request information about.
	 */
	public DescribeFeatureType(GSInsert ui, String url, String user, String pass, String layer) {
		mUi = ui;
		mUrl = url;
		mUser = user;
		mPass = pass;
		mLayer = layer;
	}
	
	@Override
	public void run() {
		BufferedReader reply = makeRequest();
		if(reply == null) // Stop immediately if the request did not generate a response.
			return;

		/* Send the request to be interpreted and forward the found layers
		 * to the main GSInsert object. */
		if(parseXml(reply)) {
			mUi.publishFields(mFields);
			mUi.setNamespace(mLayerNamespace);
			for(int i=0; i < mFields.size(); i++) {
				System.out.println(mFields.get(i).getName() + " " + mFields.get(i).getType() + ((mFields.get(i).getNullable()) ? "" : " NOT NULL"));
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
					"/wfs?service=wfs&version=1.1.0&request=DescribeFeatureType&typeName=" +
					mLayer);
			request = new URL(url.getProtocol() + "://" + authtext +
					mUrl.substring(url.getProtocol().length() + 3) +
					"/wfs?service=wfs&version=1.1.0&request=DescribeFeatureType&typeName=" +
					mLayer);
		} catch (MalformedURLException e) {
			mUi.displayAlert(GSInsert.URL_WARNING_MESSAGE, GSInsert.URL_WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
			return null;
		}
		
		/* Perform the request. */
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) request.openConnection();		 
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
	 * Parses an XML response from a DescribeFeatureType request and stores
	 * available fields in the instance ArrayList<LayerField>.
	 * @param xml A reader wrapped around an InputStream containing the XML response from a DescribeFeatureType request.
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
		private static final String ELEMENT_SEQUENCE = "xsd:sequence";
		private static final String ELEMENT_ELEMENT = "xsd:element";
		private static final String ELEMENT_SCHEMA = "xsd:schema";
		private boolean mWithinSequence = false;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase(ELEMENT_SCHEMA))
				mLayerNamespace = attributes.getValue("targetNamespace");
			else if(qName.equalsIgnoreCase(ELEMENT_SEQUENCE))
				mWithinSequence = true;
			else if(qName.equalsIgnoreCase(ELEMENT_ELEMENT) && mWithinSequence) {
				mFields.add(new LayerField(attributes.getValue("name"), Boolean.parseBoolean(attributes.getValue("nillable")), attributes.getValue("type")));
			}
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(qName.equalsIgnoreCase(ELEMENT_SEQUENCE))
				mWithinSequence = false;
			super.endElement(uri, localName, qName);
		}
	}
}
