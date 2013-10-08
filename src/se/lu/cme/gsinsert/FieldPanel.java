package se.lu.cme.gsinsert;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

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
 * Composite components class for adding a panel, which contains a JLabel
 * and a JTextArea for entering layer attributes into the field.
 *
 * @author Mattias Sp&aring;ngmyr
 * @version 0.4, 2013-10-08
 */
public class FieldPanel extends JPanel {
	JTextArea mTextArea = new JTextArea();
	/**
	 * Constructor which does all initialization needed for the panel.
	 * @param label The text to display next to the JTextArea.
	 * @param type The type of field, determining what input types are valid and setting the JTextArea hint.
	 * @param nullable Whether or not the field is allowed to be empty.
	 * @param preset Text to display in the field.
	 */
	public FieldPanel(String label, String type, boolean nullable, String preset) {
		/* Set panel properties. */
		setLayout(new BorderLayout(5,0));
		setBorder(GSInsert.BORDER);
		
		/* Setup the JLabel. */	
		JLabel jlabel = new JLabel(label + (nullable ? "" : "*"));
		jlabel.setPreferredSize(GSInsert.DIMENSION_PREFLABEL);
		if(!nullable)
			jlabel.setFont(new Font(jlabel.getFont().getFontName(), Font.ITALIC, jlabel.getFont().getSize()));

		/* Setup the TextArea. */
		Border border = new JTextField().getBorder();
		mTextArea.setBorder(border);
		mTextArea.setWrapStyleWord(true);
		
		if(type.split(":", 2)[0].equalsIgnoreCase("gml")) // If the type is geometry, give "[lon] [lat]" hint.		
			mTextArea.setText(GSInsert.HINT_GEOMETRY);
		mTextArea.setToolTipText(type);
		mTextArea.setText(preset);

		/* Add the components to the panel. */
		add(jlabel, BorderLayout.WEST);
		add(mTextArea, BorderLayout.CENTER);	
	}

	/**
	 * Basic get method for the JTextArea in this
	 * JPanel.
	 * @return The JPanel's JTextArea.
	 */
	public JTextArea getTextArea() {
		return mTextArea;
	}
}
