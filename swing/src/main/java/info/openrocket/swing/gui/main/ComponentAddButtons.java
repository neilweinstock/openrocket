package info.openrocket.swing.gui.main;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import info.openrocket.core.preferences.ApplicationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Pair;
import info.openrocket.core.util.Reflection;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.configdialog.ComponentConfigDialog;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;

/**
 * A component that contains addition buttons to add different types of rocket components
 * to a rocket.  It enables and disables buttons according to the current selection of a 
 * TreeSelectionModel. 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentAddButtons extends JPanel implements Scrollable {
	private static final long serialVersionUID = 4315680855765544950L;
	
	private static final Logger log = LoggerFactory.getLogger(ComponentAddButtons.class);
	private static final Translator trans = Application.getTranslator();
	
	private static final int ROWS = 4;
	private static final int MAXCOLS = 6;
	private static final String BUTTONPARAM = "grow, sizegroup buttons";
	
	private static final int GAP = 5;
	private static final int EXTRASPACE = 0;
	
	private final ComponentButton[][] buttons;
	
	private final OpenRocketDocument document;
	private final TreeSelectionModel selectionModel;
	private final JViewport viewport;
	private final MigLayout layout;
	
	private final int width, height;
	
	
	public ComponentAddButtons(OpenRocketDocument document, TreeSelectionModel model,
			JViewport viewport) {
		
		super();
		String constaint = "[min!]";
		for (int i = 1; i < MAXCOLS; i++)
			constaint = constaint + GAP + "[min!]";
		
		layout = new MigLayout("fill", constaint);
		setLayout(layout);
		this.document = document;
		this.selectionModel = model;
		this.viewport = viewport;

		buttons = new ComponentButton[ROWS][];
		for( int rowCur = 0; rowCur < ROWS; rowCur++){
			buttons[rowCur]=null;
		}
		int row = 0;
		int col = 0;

		////////////////////////////////////////////
		add(new JLabel(trans.get("compaddbuttons.ComponentAssembly")), "span, gaptop 0, wrap");

		//// Component Assembly Components:
		addButtonGroup(row,
				new StageButton(AxialStage.class, trans.get("compaddbuttons.AxialStage")),
				new ComponentButton(ParallelStage.class, trans.get("compaddbuttons.ParallelStage")),
				new ComponentButton(PodSet.class, trans.get("compaddbuttons.Pods")));
		row++;

		////////////////////////////////////////////
		
		//// Body components and fin sets
		add(new JLabel(trans.get("compaddbuttons.Bodycompandfinsets")), "span, gaptop unrel, wrap");
		addButtonGroup(row, 
				//// Nose cone
				new BodyComponentButton(NoseCone.class, trans.get("compaddbuttons.Nosecone")),
				//// Body tube
				new BodyComponentButton(BodyTube.class, trans.get("compaddbuttons.Bodytube")),
				//// Transition
				new BodyComponentButton(Transition.class, trans.get("compaddbuttons.Transition")),
				//// Trapezoidal
				new ComponentButton(TrapezoidFinSet.class, trans.get("compaddbuttons.Trapezoidal")), // TODO: MEDIUM: freer fin placing
				//// Elliptical
				new ComponentButton(EllipticalFinSet.class, trans.get("compaddbuttons.Elliptical")),
				//// Freeform
				new ComponentButton(FreeformFinSet.class, trans.get("compaddbuttons.Freeform")),
				//// Freeform
				new ComponentButton(TubeFinSet.class, trans.get("compaddbuttons.Tubefin")),
				//// Rail Button
				new ComponentButton( RailButton.class, trans.get("compaddbuttons.RailButton")),
				//// Launch lug
				new ComponentButton(LaunchLug.class, trans.get("compaddbuttons.Launchlug")));
		row++;
		
		/////////////////////////////////////////////
		
		//// Inner component
		add(new JLabel(trans.get("compaddbuttons.InnerComponent")), "span, gaptop unrel, wrap");
		addButtonGroup(row, 
				//// Inner tube
				new ComponentButton(InnerTube.class, trans.get("compaddbuttons.Innertube")),
				//// Coupler
				new ComponentButton(TubeCoupler.class, trans.get("compaddbuttons.Coupler")),
				//// Centering\nring
				new ComponentButton(CenteringRing.class, trans.get("compaddbuttons.Centeringring")),
				//// Bulkhead
				new ComponentButton(Bulkhead.class, trans.get("compaddbuttons.Bulkhead")),
				//// Engine\nblock
				new ComponentButton(EngineBlock.class, trans.get("compaddbuttons.Engineblock")));
		
		row++;

		////////////////////////////////////////////
		add(new JLabel(trans.get("compaddbuttons.MassComponents")), "span, gaptop unrel, wrap");

		//// Mass objects
		// NOTE: These are on the same line as the assemblies above
		addButtonGroup(row, 
				//// Parachute
				new ComponentButton(Parachute.class, trans.get("compaddbuttons.Parachute")),
				//// Streamer
				new ComponentButton(Streamer.class, trans.get("compaddbuttons.Streamer")),
				//// Shock cord
				new ComponentButton(ShockCord.class, trans.get("compaddbuttons.Shockcord")),
				//				new ComponentButton("Motor clip"),
				//				new ComponentButton("Payload"),
				//// Mass component
				new ComponentButton(MassComponent.class, trans.get("compaddbuttons.MassComponent")));
		

		// Get maximum button size
		int w = 0, h = 0;
		
		for (row = 0; row < buttons.length; row++) {
			for (col = 0; col < buttons[row].length; col++) {
				Dimension d = buttons[row][col].getPreferredSize();
				if (d.width > w)
					w = d.width;
				if (d.height > h)
					h = d.height;
			}
		}
		
		// Set all buttons to maximum size
		width = w;
		height = h;
		Dimension d = new Dimension(width, height);
		for (row = 0; row < buttons.length; row++) {
			for (col = 0; col < buttons[row].length; col++) {
				buttons[row][col].setMinimumSize(d);
				buttons[row][col].setPreferredSize(d);
				buttons[row][col].validate();
			}
		}
		
		// Add viewport listener if viewport provided
		if (viewport != null) {
			viewport.addChangeListener(new ChangeListener() {
				private int oldWidth = -1;
				
				@Override
				public void stateChanged(ChangeEvent e) {
					Dimension d1 = ComponentAddButtons.this.viewport.getExtentSize();
					if (d1.width != oldWidth) {
						oldWidth = d1.width;
						flowButtons();
					}
				}
			});
		}
		
		add(new JPanel(), "grow");
	}
	
	
	/**
	 * Adds a buttons to the panel in a row.  Assumes.
	 *
	 * @param row    Row number
	 * @param b      List of ComponentButtons to place on the row
	 */
	private void addButtonGroup(int row, ComponentButton... b) {

		int oldLen=0;
		if( null == buttons[row] ){
			buttons[row] = new ComponentButton[b.length];
		}else{
			ComponentButton[] oldArr = buttons[row];
			oldLen = oldArr.length;
			ComponentButton[] newArr = new ComponentButton[oldLen + b.length];
			System.arraycopy(oldArr, 0, newArr, 0, oldLen);
			buttons[row] = newArr;
		}
		
		int dstCol = oldLen;
		int srcCol=0;
		while( srcCol < b.length) {
			buttons[row][dstCol] = b[srcCol];
			add(b[srcCol], BUTTONPARAM);
			dstCol++;
			srcCol++;
		}
	}
	
	
	/**
	 * Flows the buttons in all rows of the panel.  If a button would come too close
	 * to the right edge of the viewport, "newline" is added to its constraints flowing 
	 * it to the next line.
	 */
	private void flowButtons() {
		if (viewport == null)
			return;
		
		int w;
		
		Dimension d = viewport.getExtentSize();

		for (ComponentButton[] button : buttons) {
			w = 0;
			for (int col = 0; col < button.length; col++) {
				w += GAP + width;
				String param = BUTTONPARAM + ",width " + width + "!,height " + height + "!";

				if (w + EXTRASPACE > d.width) {
					param = param + ",newline";
					w = GAP + width;
				}
				if (col == button.length - 1)
					param = param + ",wrap";
				layout.setComponentConstraints(button[col], param);
			}
		}
		revalidate();
	}
	
	

	/**
	 * Class for a component button.
	 */
	private class ComponentButton extends JButton implements TreeSelectionListener {
		private static final long serialVersionUID = 4510127994205259083L;
		protected Class<? extends RocketComponent> componentClass = null;
		private Constructor<? extends RocketComponent> constructor = null;
		
		/** Only label, no icon. */
		public ComponentButton(String text) {
			this(text, null, null);
		}
		
		/**
		 * Constructor with icon and label.  The icon and label are placed into the button.
		 * The label may contain "\n" as a newline.
		 */
		public ComponentButton(String text, Icon enabled, Icon disabled) {
			super(text, enabled);

			setVerticalTextPosition(SwingConstants.BOTTOM); 		// Put the text below the icon
			setHorizontalTextPosition(SwingConstants.CENTER); 		// Center the text horizontally
			//setIconTextGap(0); // Optional; sets the gap between the icon and the text

			// set the disabled icon if it is not null
			if (disabled != null) {
				setDisabledIcon(disabled);
			}

			setHorizontalAlignment(SwingConstants.CENTER); 			// Center the button in its parent component

			// if you have multiline text, you could use html to format it
			if (text != null && text.contains("\n")) {
				text = "<html><center>" + text.replace("\n", "<br>") + "</center></html>";
				setText(text);
			}

			// Initialize enabled status
			valueChanged(null);

			// Attach a tree selection listener if selection model is not null
			if (selectionModel != null) {
				selectionModel.addTreeSelectionListener(this);
			}
		}
		
		
		/**
		 * Main constructor that should be used.  The generated component type is specified
		 * and the text.  The icons are fetched based on the component type.
		 */
		public ComponentButton(Class<? extends RocketComponent> c, String text) {
			this(text, ComponentIcons.getLargeIcon(c), ComponentIcons.getLargeDisabledIcon(c));
			
			if (c == null)
				return;
			
			componentClass = c;
			
			try {
				constructor = c.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Unable to get default " +
						"constructor for class " + c, e);
			}
		}
		
		
		/**
		 * Return whether the current component is addable when the component c is selected.
		 * c is null if there is no selection.  The default is to use c.isCompatible(class).
		 */
		public boolean isAddable(RocketComponent c) {
			if (c == null)
				return false;
			if (componentClass == null)
				return false;
			return c.isCompatible(componentClass);
		}
		
		/**
		 * Return the position to add the component if component c is selected currently.
		 * The first element of the returned array is the RocketComponent to add the component
		 * to, and the second (if non-null) an Integer telling the position of the component.
		 * A return value of null means that the user cancelled addition of the component.
		 * If the Integer is null, the component is added at the end of the sibling 
		 * list.  By default returns the end of the currently selected component.
		 * 
		 * @param c  The component currently selected
		 * @return   The position to add the new component to, or null if should not add.
		 */
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			return new Pair<>(c, null);
		}
		
		/**
		 * Updates the enabled status of the button.
		 * TODO: LOW: What about updates to the rocket tree?
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			updateEnabled();
		}
		
		/**
		 * Sets the enabled status of the button and all subcomponents.
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			Component[] c = getComponents();
			for (Component component : c) component.setEnabled(enabled);
		}
		
		
		/**
		 * Update the enabled status of the button.
		 */
		private void updateEnabled() {
			RocketComponent c = null;
			TreePath p = selectionModel.getSelectionPath();
			if (p != null)
				c = (RocketComponent) p.getLastPathComponent();
			setEnabled(isAddable(c));
		}
		
		
		@Override
		protected void fireActionPerformed(ActionEvent event) {
			super.fireActionPerformed(event);
			log.info(Markers.USER_MARKER, "Adding component of type " + componentClass.getSimpleName());
			RocketComponent c = null;
			Integer position = null;
			
			TreePath p = selectionModel.getSelectionPath();
			if (p != null)
				c = (RocketComponent) p.getLastPathComponent();
			
			Pair<RocketComponent, Integer> pos = getAdditionPosition(c);
			if (pos == null) {
				// Cancel addition
				log.info("No position to add component");
				return;
			}
			c = pos.getU();
			position = pos.getV();
			

			if (c == null) {
				// Should not occur
				Application.getExceptionHandler().handleErrorCondition("ERROR:  Could not place new component.");
				updateEnabled();
				return;
			}
			
			if (constructor == null) {
				Application.getExceptionHandler().handleErrorCondition("ERROR:  Construction of type not supported yet.");
				return;
			}
			
			RocketComponent component;
			try {
				component = (RocketComponent) constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new BugException("Could not construct new instance of class " + constructor, e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
			
			// Next undo position is set by opening the configuration dialog
			document.addUndoPosition("Add " + component.getComponentName());
			
			log.info("Adding component " + component.getComponentName() + " to component " + c.getComponentName() +
					" position=" + position);
			
			if (position == null)
				c.addChild(component);
			else
				c.addChild(component, position);
			
			// Select new component and open config dialog
			selectionModel.setSelectionPath(ComponentTreeModel.makeTreePath(component));
			
			JFrame parent = null;
			for (Component comp = ComponentAddButtons.this; comp != null; comp = comp.getParent()) {
				if (comp instanceof JFrame) {
					parent = (JFrame) comp;
					break;
				}
			}
			
			ComponentConfigDialog.showDialog(parent, document, component, false, true);
		}
	}
	
	/**
	 * A class suitable for BodyComponents.  Addition is allowed ...  
	 */
	private class BodyComponentButton extends ComponentButton {
		private static final long serialVersionUID = 1574998068156786363L;

		public BodyComponentButton(Class<? extends RocketComponent> c, String text) {
			super(c, text);
		}
		
		public BodyComponentButton(String text, Icon enabled, Icon disabled) {
			super(text, enabled, disabled);
		}
		
		public BodyComponentButton(String text) {
			super(text);
		}
		
		@Override
		public boolean isAddable(RocketComponent selectedComponent) {
			if (super.isAddable(selectedComponent)) {
				return true;
			}else if (selectedComponent instanceof BodyComponent) {
	            // Handled separately:
				return true;
		    }else if (selectedComponent == null) {
			    return false;
		    }else if( selectedComponent instanceof Rocket) {
			    return false;
			}
			return false;
		}
		
		@Override
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			if (super.isAddable(c)) // Handled automatically
				return super.getAdditionPosition(c);
			

			if (c == null || c instanceof Rocket) {
				// Add as last body component of the last stage
				Rocket rocket = document.getRocket();
				return new Pair<>(rocket.getChild(rocket.getStageCount() - 1),
						null);
			}
			
			if (!(c instanceof BodyComponent))
				return null;
			RocketComponent parent = c.getParent();
			if (parent == null) {
				throw new BugException("Component " + c.getComponentName() + " is the root component, " +
						"componentClass=" + componentClass);
			}
			
			// Check whether to insert between or at the end.
			// 0 = ask, 1 = in between, 2 = at the end
			int pos = Application.getPreferences().getChoice(ApplicationPreferences.BODY_COMPONENT_INSERT_POSITION_KEY, 2, 0);
			if (pos == 0) {
				if (parent.getChildPosition(c) == parent.getChildCount() - 1)
					pos = 2; // Selected component is the last component
				else
					pos = askPosition();
			}

			return switch (pos) {
				case 0 ->
					// Cancel
						null;
				case 1 ->
					// Insert after current position
						new Pair<>(parent, parent.getChildPosition(c) + 1);
				case 2 ->
					// Insert at the end of the parent
						new Pair<>(parent, null);
				default -> {
					Application.getExceptionHandler().handleErrorCondition("ERROR:  Bad position type: " + pos);
					yield null;
				}
			};
		}
		
		private int askPosition() {
			//// Insert here 
			//// Add to the end
			//// Cancel
			Object[] options = { trans.get("compaddbuttons.askPosition.Inserthere"), 
					trans.get("compaddbuttons.askPosition.Addtotheend"), 
					trans.get("compaddbuttons.askPosition.Cancel") };
			
			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox check = new JCheckBox(trans.get("compaddbuttons.Donotaskmeagain"));
			panel.add(check, "wrap");
			//// You can change the default operation in the preferences.
			panel.add(new StyledLabel(trans.get("compaddbuttons.lbl.Youcanchange"), -2));
			
			int sel = JOptionPane.showOptionDialog(null, // parent component 
					//// Insert the component after the current component or as the last component?
					new Object[] {
					trans.get("compaddbuttons.lbl.insertcomp"),
							panel },
							//// Select component position
							trans.get("compaddbuttons.Selectcomppos"), // title
					JOptionPane.DEFAULT_OPTION, // default selections
					JOptionPane.QUESTION_MESSAGE, // dialog type
					null, // icon
					options, // options
					options[0]); // initial value
			
			switch (sel) {
			case JOptionPane.CLOSED_OPTION:
			case 2:
				// Cancel
				return 0;
			case 0:
				// Insert
				sel = 1;
				break;
			case 1:
				// Add
				sel = 2;
				break;
			default:
				Application.getExceptionHandler().handleErrorCondition("ERROR:  JOptionPane returned " + sel);
				return 0;
			}
			
			if (check.isSelected()) {
				// Save the preference
				Application.getPreferences().putInt(ApplicationPreferences.BODY_COMPONENT_INSERT_POSITION_KEY, sel);
			}
			return sel;
		}
		
	}

	/**
	 * A class suitable for the Stage component.
	 * If a stage component or any of its subcomponents is selected and there is already a stage after the
	 * selected stage or parent stage, then a popup window will ask whether the user wants to insert the stage
	 * at the end or between the two stages.
	 * In any other case, the new stage will be added to the end of the component tree
	 */
	private class StageButton extends ComponentButton {

		public StageButton(String text) {
			super(text);
		}

		public StageButton(String text, Icon enabled, Icon disabled) {
			super(text, enabled, disabled);
		}

		public StageButton(Class<? extends RocketComponent> c, String text) {
			super(c, text);
		}

		@Override
		public boolean isAddable(RocketComponent c) {
			return true;
		}

		@Override
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			if (c == null || c instanceof Rocket) {
				// Add to the end
				return new Pair<>(document.getRocket(), null);
			}

			RocketComponent parentStage = null;
			if (c instanceof AxialStage)
				parentStage = c;
			else {
				parentStage = c.getStage();
			}
			if (parentStage == null) {
				throw new BugException("Component " + c.getComponentName() + " has no parent stage");
			}

			// Check whether to insert between or at the end.
			// 0 = ask, 1 = in between, 2 = at the end
			int pos = Application.getPreferences().getChoice(ApplicationPreferences.STAGE_INSERT_POSITION_KEY, 2, 0);
			if (pos == 0) {
				if (document.getRocket().getChildPosition(parentStage) == document.getRocket().getChildCount() - 1)
					pos = 2; // Selected component is the last component
				else
					pos = askPosition();
			}

			return switch (pos) {
				case 0 ->
					// Cancel
						null;
				case 1 ->
					// Insert after current stage
						new Pair<>(document.getRocket(), document.getRocket().getChildPosition(parentStage) + 1);
				case 2 ->
					// Insert at the end
						new Pair<>(document.getRocket(), null);
				default -> {
					Application.getExceptionHandler().handleErrorCondition("ERROR:  Bad position type: " + pos);
					yield null;
				}
			};
		}

		private int askPosition() {
			//// Insert here
			//// Add to the end
			//// Cancel
				Object[] options = { trans.get("compaddbuttons.askPosition.Inserthere"),
					trans.get("compaddbuttons.askPosition.Addtotheend"),
					trans.get("compaddbuttons.askPosition.Cancel") };

			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox check = new JCheckBox(trans.get("compaddbuttons.Donotaskmeagain"));
			panel.add(check, "wrap");
			//// You can change the default operation in the preferences.
			panel.add(new StyledLabel(trans.get("compaddbuttons.lbl.Youcanchange"), -2));

			int sel = JOptionPane.showOptionDialog(null, // parent component
					//// Insert the component after the current component or as the last component?
					new Object[] {
							trans.get("compaddbuttons.lbl.insertstage"),
							panel },
					//// Select component position
					trans.get("compaddbuttons.Selectstagepos"), // title
					JOptionPane.DEFAULT_OPTION, // default selections
					JOptionPane.QUESTION_MESSAGE, // dialog type
					null, // icon
					options, // options
					options[0]); // initial value

			switch (sel) {
				case JOptionPane.CLOSED_OPTION:
				case 2:
					// Cancel
					return 0;
				case 0:
					// Insert
					sel = 1;
					break;
				case 1:
					// Add
					sel = 2;
					break;
				default:
					Application.getExceptionHandler().handleErrorCondition("ERROR:  JOptionPane returned " + sel);
					return 0;
			}

			if (check.isSelected()) {
				// Save the preference
				Application.getPreferences().putInt(ApplicationPreferences.STAGE_INSERT_POSITION_KEY, sel);
			}
			return sel;
		}
	}

	/////////  Scrolling functionality
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL)
			return visibleRect.height * 8 / 10;
		return 10;
	}
	
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}
	
}
