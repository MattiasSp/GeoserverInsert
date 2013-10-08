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
 * A class that takes a Geoserver url, makes a GetCapabiities
 * request to it and sends the reply to an XML parser.
 *
 * @author Mattias Sp&aring;ngmyr
 * @version 0.4, 2013-10-08
 */
public class GetCapabilities implements Runnable {
	private GSInsert mUi;
	private String mUrl;
	private String mUser;
	private String mPass;
	private ArrayList<String> mLayers = new ArrayList<String>();
	
	/**
	 * Basic constructor that sets the ui source, url, the user and
	 * the password for the request.
	 * @param ui The UI to which to reply with a layer list.
	 * @param input The URL of the Geoserver instance.
	 * @param user The username to provide as authentication.
	 * @param pass The password to authenticate with.
	 */
	public GetCapabilities(GSInsert ui, String input, String user, String pass) {
		mUi = ui;
		mUrl = input;
		mUser = user;
		mPass = pass;
	}

	@Override
	public void run() {
		BufferedReader reply = makeRequest();
		if(reply == null) // Stop immediately if the request did not generate a response.
			return;
 
		/* Send the request to be interpreted and forward the found layers
		 * to the main GSInsert object. */
		if(parseXml(reply)) {
			mUi.publishLayers(mLayers);
			for(int i=0; i < mLayers.size(); i++) {
				System.out.println(mLayers.get(i));
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
					"/wfs?service=wfs&version=1.1.0&request=GetCapabilities");
			request = new URL(url.getProtocol() + "://" + authtext +
					mUrl.substring(url.getProtocol().length() + 3) +
					"/wfs?service=wfs&version=1.1.0&request=GetCapabilities");
		} catch (MalformedURLException e) {
			mUi.displayAlert(GSInsert.URL_WARNING_TITLE, GSInsert.URL_WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE);
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
			mUi.displayAlert(GSInsert.CON_ERROR_TITLE, GSInsert.CON_ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE);
			return null;
		}		
	}

	/**
	 * Parses an XML response from a GetCapabilities request and stores available Layers
	 * and options in the instance ArrayList<String>.
	 * @param xml A reader wrapped around an InputStream containing the XML response from a GetCapabilities request.
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
		private static final String ELEMENT_FEATURETYPE = "FeatureType";
		private static final String ELEMENT_NAME = "Name";
		private boolean mWithinFeatureType = false;
		private boolean mWithinName = false;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {;
			if(qName.equalsIgnoreCase(ELEMENT_FEATURETYPE))
				mWithinFeatureType = true;
			else if(qName.equalsIgnoreCase(ELEMENT_NAME) && mWithinFeatureType)
				mWithinName = true;
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			//System.out.println(new String(ch, start, length));
			if(mWithinName)
				mLayers.add(new String(ch, start, length));
			super.characters(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(qName.equalsIgnoreCase(ELEMENT_NAME) && mWithinFeatureType)
				mWithinName = false;
			else if(qName.equalsIgnoreCase(ELEMENT_FEATURETYPE))
				mWithinFeatureType = false;
			super.endElement(uri, localName, qName);
		}
	}
}
