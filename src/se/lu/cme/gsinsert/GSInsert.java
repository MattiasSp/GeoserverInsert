package se.lu.cme.gsinsert;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

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
 * Light-weight java application for making WFS Transaction Insert requests
 * to Geoserver layers.
 *
 * @author Mattias Sp&aring;ngmyr
 * @version 0.3, 2013-10-08
 */
public class GSInsert {
	public static final String TITLE = "GeoserverInsert";
	public static final String VERSION = "v0.3";
	protected static final String HEADING_USER = "Username:";
	protected static final String HEADING_PASS = "Password:";
	protected static final String TOOLTIP_SERVER = "e.g. http://www.my.server/geoserver";
	protected static final String HEADING_SERVER = "Geoserver URL:";
	protected static final String BUTTONTEXT_SERVER = "Fetch Layers";
	protected static final String HEADING_LAYERS = "Select Layer:";
	protected static final String BUTTONTEXT_SUBMIT = "Submit";
	protected static final String HINT_GEOMETRY = "[lon] [lat]";
	protected static final Dimension DIMENSION_MINFRAME = new Dimension(322, 170);
	protected static final Dimension DIMENSION_AUTHPANEL = new Dimension(315, 35);
	protected static final Dimension DIMENSION_PANEL = new Dimension(Integer.MAX_VALUE, 35);
	protected static final Dimension DIMENSION_PREFLABEL = new Dimension(100, 25);
	protected static final Dimension DIMENSION_MAXAUTHFIELD = new Dimension(200, 25);
	protected static final Dimension DIMENSION_MAXFIELD = new Dimension(Integer.MAX_VALUE, 25);
	protected static final Dimension DIMENSION_MAXBUTTON = new Dimension(162, 25);
	protected static final Dimension DIMENSION_PREFAREA = new Dimension(162, 50);
	protected static final Border BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);
	
	protected static final String URL_WARNING_TITLE = "Invalid URL";
	protected static final String URL_WARNING_MESSAGE = "Check that your Geoserver URL is correct.";
	protected static final String CON_ERROR_TITLE = "Connection Failed";
	protected static final String CON_ERROR_MESSAGE = "There was an error connecting to the server.";
	protected static final String PARSE_ERROR_TITLE = "Response Error";
	protected static final String PARSE_ERROR_MESSAGE = "There was an error reading the resonse from the server.";
	protected static final String INSERT_SUCCESS_TITLE = "Upload Success";
	protected static final String INSERT_SUCCESS_MESSAGE = "The feature was uploaded successfully.";
	protected static final String INSERT_FAILURE_TITLE = "Upload Failure";
	protected static final String INSERT_FAILURE_MESSAGE = "The feature was not uploaded correctly. Contact the server administrator.";
	public static final Map<String, String> ESCAPESEQUENCES = getEscapeSequences();

	private static Config mConfig;
	private JFrame mFrame = new JFrame();
	
	private JPanel mUserPanel = new JPanel();
	private JPanel mSubUserPanel;
	private JLabel mUserLabel = new JLabel(HEADING_USER, JLabel.LEFT);
	private JTextField mUserTextField = new JTextField();
	
	private JPanel mPassPanel = new JPanel();
	private JPanel mSubPassPanel;
	private JLabel mPassLabel = new JLabel(HEADING_PASS, JLabel.LEFT);
	private JTextField mPassField = new JPasswordField();

	private JPanel mServerPanel = new JPanel();
	private JLabel mServerLabel = new JLabel(HEADING_SERVER, JLabel.LEFT);
	private JTextField mServerTextField = new JTextField();
	private JButton mServerButton = new JButton(BUTTONTEXT_SERVER);
	
	private JPanel mLayersPanel = new JPanel();
	private JLabel mLayersLabel = new JLabel(HEADING_LAYERS, JLabel.LEFT);
	private JComboBox<String> mLayersComboBox = new JComboBox<String>();
	
	private JPanel mFieldsPanel = new JPanel();
	private JScrollPane mFieldsScrollPane = new JScrollPane(mFieldsPanel);
	private JButton mSubmitButton = new JButton(BUTTONTEXT_SUBMIT);

	/** The selected layer. */
	private String mLayer;
	/** The namespace belonging to the selected layer. */
	private String mLayerNamespace;
	/** A list of references to all the JTextFields belonging to their respective field name. */
	private HashMap<String, JTextComponent> mTextFields = new HashMap<String, JTextComponent>();
	/** A list of all the fields' field types, by their name. */
	private HashMap<String, String> mFieldTypes = new HashMap<String, String>();

	/**
	 * Creates an instance of the class and starts it.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			mConfig = new Config();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to read configuration file.");
		}
		new GSInsert().createUI();
	}

	/**
	 * Produces an immutable map of Java escape sequences
	 * with their String representations as keys.
	 * @return A map of escape sequences.
	 */
	public static Map<String, String> getEscapeSequences() {
		HashMap<String, String> escapeSeq = new HashMap<String, String>();
		escapeSeq.put("\\t", "\t");
		escapeSeq.put("\\b", "\b");
		escapeSeq.put("\\n", "\n");
		escapeSeq.put("\\r", "\r");
		escapeSeq.put("\\f", "\f");
		escapeSeq.put("\\\'", "\'");
		escapeSeq.put("\\\"", "\"");
		escapeSeq.put("\\\\", "\\");
		return Collections.unmodifiableMap(escapeSeq);
	}

	/**
	 * Generates the UI, performs the logic and responds to user input.
	 */
	private void createUI() {
		final GSInsert _this = this; // Used for giving other classes a reference to this GSInsert object.

		/* Setup the main window frame. */
		mFrame.setTitle(TITLE + " " + VERSION + ((mConfig.getEdition().equalsIgnoreCase("")) ? "" : " - " + mConfig.getEdition()));
		mFrame.setMinimumSize(DIMENSION_MINFRAME);
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.setLocationByPlatform(true);
		mFrame.getContentPane().setLayout(new BoxLayout(mFrame.getContentPane(), BoxLayout.PAGE_AXIS));

		/* Add all username panel components. */
		mUserPanel.setLayout(new BorderLayout(5, 0));
		mUserPanel.setMaximumSize(DIMENSION_PANEL);
		mSubUserPanel = new JPanel();
		mSubUserPanel.setMaximumSize(DIMENSION_AUTHPANEL);
		mUserLabel.setPreferredSize(DIMENSION_PREFLABEL);
		mUserTextField.setPreferredSize(DIMENSION_MAXAUTHFIELD);
		if(!mConfig.getUser().equalsIgnoreCase(""))
			mUserTextField.setText(mConfig.getUser());
		mSubUserPanel.add(mUserLabel);
		mSubUserPanel.add(mUserTextField);
		mUserPanel.add(mSubUserPanel, BorderLayout.WEST);
		
		/* Add all password panel components. */
		mPassPanel.setLayout(new BorderLayout(5, 0));
		mPassPanel.setMaximumSize(DIMENSION_PANEL);
		mSubPassPanel = new JPanel();
		mSubPassPanel.setMaximumSize(DIMENSION_AUTHPANEL);
		mPassLabel.setPreferredSize(DIMENSION_PREFLABEL);
		mPassField.setPreferredSize(DIMENSION_MAXAUTHFIELD);
		mSubPassPanel.add(mPassLabel);
		mSubPassPanel.add(mPassField);
		mPassPanel.add(mSubPassPanel, BorderLayout.WEST);

		/* Add all server panel components. */
		mServerPanel.setBorder(BORDER);
		mServerPanel.setLayout(new BorderLayout(5, 0));
		mServerPanel.setMaximumSize(DIMENSION_PANEL);
		mServerLabel.setPreferredSize(DIMENSION_PREFLABEL);
		mServerTextField.setMaximumSize(DIMENSION_MAXFIELD);
		mServerTextField.setPreferredSize(DIMENSION_MAXAUTHFIELD);
		mServerTextField.setToolTipText(TOOLTIP_SERVER);
		if(!mConfig.getServer().equalsIgnoreCase(""))
			mServerTextField.setText(mConfig.getServer());
		mServerButton.setPreferredSize(DIMENSION_MAXBUTTON);
		mServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new GetCapabilities(_this,
					mServerTextField.getText(),
					mUserTextField.getText(),
					mPassField.getText())
				).start();
			}
		});
		mServerPanel.add(mServerLabel, BorderLayout.WEST);
		mServerPanel.add(mServerTextField, BorderLayout.CENTER);
		mServerPanel.add(mServerButton, BorderLayout.EAST);
		
		/* Add all layer panel components. */
		mLayersPanel.setBorder(BORDER);
		mLayersPanel.setLayout(new BorderLayout(5, 0));
		mLayersPanel.setMaximumSize(DIMENSION_PANEL);
		mLayersLabel.setPreferredSize(DIMENSION_PREFLABEL);
		mLayersComboBox.setMaximumSize(DIMENSION_MAXFIELD);
		mLayersComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mLayer = (String) mLayersComboBox.getSelectedItem();
				new Thread(new DescribeFeatureType(_this,
					mServerTextField.getText(),
					mUserTextField.getText(),
					mPassField.getText(),
					mLayer
				)).start();
			}
		});
		mLayersPanel.add(mLayersLabel, BorderLayout.WEST);
		mLayersPanel.add(mLayersComboBox, BorderLayout.CENTER);
		
		/* Setup the ScrollPane and the empty Fields panel. */
		mFieldsScrollPane.setBorder(null);
		mFieldsPanel.setLayout(new BoxLayout(mFieldsPanel, BoxLayout.PAGE_AXIS));
		
		/* Setup the Submit Button. */
		mSubmitButton.setPreferredSize(DIMENSION_MAXBUTTON);
		mSubmitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, String> attr = getAttributes();
				if(attr != null) {
					new Thread(new WfsInsert(_this,
						mServerTextField.getText(),
						mUserTextField.getText(),
						mPassField.getText(),
						mLayer,
						mLayerNamespace,
						attr,
						mFieldTypes
						)
					).start();
				}
			}
		});
		
		/* Add all panels to the window frame. */
		mFrame.add(mUserPanel);
		mFrame.add(mPassPanel);
		mFrame.add(mServerPanel);
		mFrame.add(mLayersPanel);
		mFrame.add(mFieldsScrollPane);	

		mFrame.pack();
		mFrame.setVisible(true);
	}

	/**
	 * Show an alert dialog to the user.
	 * @param title The title of the dialog.
	 * @param message The warning message to the user.
	 * @param type The type of message.
	 */
	public void displayAlert(String title, String message, int type) {
		JOptionPane.showMessageDialog(mFrame, message, title, type);
	}

	/**
	 * Takes an ArrayList of layer name Strings and
	 * publishes them in the Layer ComboBox.
	 * @param layers The list of layers to publish.
	 */
	public void publishLayers(ArrayList<String> layers) {
		int matchedIndex = 0;
		/* Find out the index of any layer that's been preset in the config file. */
		if(!mConfig.getLayer().equalsIgnoreCase("")) {
			for(int i=0; i < layers.size(); i++) {
				if(mConfig.getLayer().equalsIgnoreCase(layers.get(i)))
					matchedIndex = i;
			}
		}
		mLayersComboBox.setModel(new DefaultComboBoxModel<String>(layers.toArray(new String[layers.size()])));
		mLayersComboBox.setSelectedIndex(matchedIndex);
		mFieldsPanel.removeAll();
		mFrame.pack();
	}

	/**
	 * Takes an ArrayList of LayerFields and publishes
	 * them as separate panels with JLabels and JTextFields.
	 * @param fields The fields to publish.
	 */
	public void publishFields(ArrayList<LayerField> fields) {
		mFieldsPanel.removeAll(); // Clear all LayerField panels to make room for the new ones.		
		for(LayerField field : fields) {
			FieldPanel panel = new FieldPanel(field.getName(), field.getType(), field.getNullable(), mConfig.getFields().get(field.getName()));
			mTextFields.put(field.getName(), panel.getTextArea()); // Store a reference to the JTextField displaying that specific field. 
			mFieldsPanel.add(panel);
			mFieldTypes.put(field.getName(), field.getType()); // Store the field types.
		}
		mFieldsPanel.add(mSubmitButton);
		mFrame.pack();
	}

	/**
	 * Basic set method for storing the namespace
	 * related to the targeted layer.
	 * @param ns The namespace of the selected layer.
	 */
	public void setNamespace(String ns) {
		mLayerNamespace = ns;
	}

	/**
	 * Gets the attributes input by the user in a HashMap
	 * with the field names as keys.
	 * @return A HashMap<String, String> of field names to attributes.
	 */
	private HashMap<String, String> getAttributes() {
		HashMap<String, String> map = new HashMap<String, String>();
		// TODO Check if inputs are valid, otherwise alert the user. "if(fail) return null;"
		for(String field : mTextFields.keySet()) {
			String text = mTextFields.get(field).getText();
			if(!mFieldTypes.get(field).split(":", 2)[0].equalsIgnoreCase("gml")) {
				/* Replace given text with specified replacement text. */
				ArrayList<String[]> rep = mConfig.getReplace();
				for(int i=0; i < rep.size(); i++) {
					/* Make String representations of escape sequences into actual escape sequences. */
					for(String key : ESCAPESEQUENCES.keySet())
						rep.get(i)[0] = rep.get(i)[0].replace(key, ESCAPESEQUENCES.get(key));
					text = text.replace(rep.get(i)[0], rep.get(i)[1]);
				}
			}
			text = "<![CDATA[" + text + "]]>";
			map.put(field, text);
		}
		return map;
	}
}
