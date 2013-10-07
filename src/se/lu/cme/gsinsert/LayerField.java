package se.lu.cme.gsinsert;

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
 * Simple field class for keeping the name, nullability and type data of a field.
 *
 * @author Mattias Sp&aring;ngmyr
 * @version 0.01, 2012-09-17
 */
public class LayerField {
	/** The error tag for this class. */
	public static final String TAG = "LayerField";

	/** The name of the field. */
	private final String mName;
	/** Whether or not the field's values can be null. */
	private final boolean mNullable;
	/** The field type, as it is specified on the geospatial server. */
	private final String mType;
	
	/**
	 * Default constructor setting the field data.
	 * @param name The field name.
	 * @param nullable Whether or not null values are allowed.
	 * @param type The data type of the field on the geospatial server.
	 */
	public LayerField(String name, boolean nullable, String type) {
		mName = name;
		mNullable = nullable;
		mType = type;
	}
	
	/**
	 * Gets the field name.
	 * @return The field name.
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Gets the nullable value.
	 * @return The nullable value.
	 */
	public boolean getNullable() {
		return mNullable;
	}
	
	/**
	 * Gets the field type.
	 * @return The data type of the field on the geospatial server.
	 */
	public String getType() {
		return mType;
	}
}