package se.lu.cme.gsinsert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
 * Class for loading and storing configuration data from a text file.
 * 
 * @author Mattias Sp&aring;ngmyr
 * @version 0.3, 2013-10-08
 */
public class Config {
	public static final String KEY_EDITION = "EDITION";
	public static final String KEY_USER = "USER";
	public static final String KEY_SERVER = "SERVER";
	public static final String KEY_LAYER = "LAYER";
	public static final String KEY_FIELD = "FIELD";
	public static final String KEY_REPLACE = "REPLACE";
	private String mEdition = "";
	private String mUser = "";
	private String mServer = "";
	private String mLayer = "";
	private HashMap<String, String> mFields = new HashMap<String, String>();
	private ArrayList<String[]> mReplace = new ArrayList<String[]>();

	/**
	 * Creates a Config object by parsing a configuration file
	 * and storing the result in its instance variables.
	 */
	public Config() throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("config.ini"));
		} catch (FileNotFoundException e) {
			System.out.println("No config.ini file found.");
			return;
		}

		String line = reader.readLine();
		
		while (line != null) {
			String[] keyValuePair = line.split(" ", 2);
			if(keyValuePair[0].equalsIgnoreCase(KEY_EDITION))
				mEdition = keyValuePair[1];
			else if(keyValuePair[0].equalsIgnoreCase(KEY_USER))
				mUser = keyValuePair[1];
			else if(keyValuePair[0].equalsIgnoreCase(KEY_SERVER))
				mServer = keyValuePair[1];
			else if(keyValuePair[0].equalsIgnoreCase(KEY_LAYER))
				mLayer = keyValuePair[1];
			else if(keyValuePair[0].equalsIgnoreCase(KEY_FIELD)) {
				String[] fieldPreset = line.split(" ", 3);
				mFields.put(fieldPreset[1], fieldPreset[2]);
			}
			else if(keyValuePair[0].equalsIgnoreCase(KEY_REPLACE)) {
				String[] fieldPreset = line.split(" ", 3);
				mReplace.add(new String[] {fieldPreset[1], fieldPreset[2]});
			}
			else
				System.out.println("Invalid line in config.ini.");
				
			line = reader.readLine();
		}
		reader.close();
	}

	/**
	 * Basic get method for the Edition String.
	 * @return The text to use as title addition.
	 */
	protected String getEdition() {
		return mEdition;
	}

	/**
	 * Basic get method for the user preset String.
	 * @return The text to fill the user field with during start-up.
	 */
	protected String getUser() {
		return mUser;
	}

	/**
	 * Basic get method for the server preset String.
	 * @return The text to fill the server field with during start-up.
	 */
	protected String getServer() {
		return mServer;
	}

	/**
	 * Basic get method for the layer preset String.
	 * @return The name of the layer to auto-select upon retrieving layers.
	 */
	protected String getLayer() {
		return mLayer;
	}

	/**
	 * Basic get method for the field preset map.
	 * @return The map of preset strings to fill the correspondingly named fields with upon retrieving layer fields.
	 */
	protected HashMap<String, String> getFields() {
		return mFields;
	}

	/**
	 * Basic get method for the String replacement list.
	 * The list holds String arrays which should have two elements,
	 * the String to replace and its replacement String.
	 * @return The list of Strings to replace corresponding Strings with in non-geometry fields.
	 */
	protected ArrayList<String[]> getReplace() {
		return mReplace;
	}

	
}
