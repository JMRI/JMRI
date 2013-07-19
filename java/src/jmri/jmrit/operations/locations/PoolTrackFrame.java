//PoolTrackFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * 
 * Things to test with this frame:
 * 
 * - Adding a new Pool name to the available pools list
 * 
 * - What happens when a null track is passed to the frame
 * 
 * - Selecting an existing pool and saving it to the track
 * 
 * - Selecting a minimum length and saving it to the track
 * 
 * - Not sure if we want to test the status display panel, as it doesn't do
 * anything.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 * 
 * 
 */
class PoolTrackFrame extends OperationsFrame implements
		java.beans.PropertyChangeListener {

	// labels
	JLabel name = new JLabel(Bundle.getMessage("Name"));
	JLabel minimum = new JLabel(Bundle.getMessage("Minimum"));
	JLabel length = new JLabel(Bundle.getMessage("Length"));

	// text field
	JTextField trackPoolNameTextField = new JTextField(20);
	JTextField trackMinLengthTextField = new JTextField(5);

	// combo box
	JComboBox comboBoxPools = new JComboBox();

	// major buttons
	JButton addButton = new JButton(Bundle.getMessage("Add"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// pool status
	JPanel poolStatus = new JPanel();

	private TrackEditFrame _tefx;
	protected Track _track;
	protected Pool _pool;

	public PoolTrackFrame(TrackEditFrame tef) {
		super();

		// the following code sets the frame's initial state
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		_tefx = tef;
		_track = _tefx._track;

		if (_track == null) {
			log.debug("track is null, pools can not be created");
			return;
		}

		_track.addPropertyChangeListener(this);
		_track.getLocation().addPropertyChangeListener(this);

		_pool = _track.getPool();

		if (_pool != null)
			_pool.addPropertyChangeListener(this);

		// load the panel
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		JScrollPane p1Pane = new JScrollPane(p1);
		p1Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p1Pane.setBorder(BorderFactory.createTitledBorder(""));

		JPanel poolName = new JPanel();
		poolName.setLayout(new GridBagLayout());
		poolName.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("PoolName")));
		addItem(poolName, trackPoolNameTextField, 0, 0);
		addItem(poolName, addButton, 1, 0);

		JPanel selectPool = new JPanel();
		selectPool.setLayout(new GridBagLayout());
		selectPool.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("PoolSelect")));
		addItem(selectPool, comboBoxPools, 0, 0);

		JPanel minLengthTrack = new JPanel();
		minLengthTrack.setLayout(new GridBagLayout());
		minLengthTrack.setBorder(BorderFactory.createTitledBorder(MessageFormat
				.format(Bundle.getMessage("PoolTrackMinimum"),
						new Object[] { _track.getName() })));
		addItem(minLengthTrack, trackMinLengthTextField, 0, 0);

		trackMinLengthTextField.setText(Integer.toString(_track
				.getMinimumLength()));

		JPanel savePool = new JPanel();
		savePool.setLayout(new GridBagLayout());
		savePool.setBorder(BorderFactory.createTitledBorder(""));
		addItem(savePool, saveButton, 0, 0);

		p1.add(poolName);
		p1.add(selectPool);
		p1.add(minLengthTrack);
		p1.add(savePool);

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
		JScrollPane p2Pane = new JScrollPane(p2);
		p2Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p2Pane.setBorder(BorderFactory.createTitledBorder(""));

		// pool status panel
		poolStatus.setLayout(new GridBagLayout());

		p2.add(poolStatus);

		getContentPane().add(p1Pane);
		getContentPane().add(p2Pane);
		setTitle(Bundle.getMessage("MenuItemPoolTrack"));

		// load comboBox
		updatePoolsComboBox();
		updatePoolStatus();

		// button action
		addButtonAction(addButton);
		addButtonAction(saveButton);

		setVisible(true);
	}

	public PoolTrackFrame(Track t) {
		super();

		// Check for null first,
		if (t == null)
			throw new NullPointerException("Track instance can't be null");	// NOI18N
		
		// then save the Track we will work with
		_track = t;
	}

	@Override
	public void initComponents() {
		// This is an alternate constructor that only takes a Track instance.
		// the following code sets the frame's initial state
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		if (_track == null) {
			log.debug("track is null, pools can not be created");
			return;
		}

		_track.addPropertyChangeListener(this);
		_track.getLocation().addPropertyChangeListener(this);

		_pool = _track.getPool();

		if (_pool != null)
			_pool.addPropertyChangeListener(this);

		// load the panel
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		JScrollPane p1Pane = new JScrollPane(p1);
		p1Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p1Pane.setBorder(BorderFactory.createTitledBorder(""));

		JPanel poolName = new JPanel();
		poolName.setLayout(new GridBagLayout());
		poolName.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("PoolName")));
		addItem(poolName, trackPoolNameTextField, 0, 0);
		addItem(poolName, addButton, 1, 0);

		JPanel selectPool = new JPanel();
		selectPool.setLayout(new GridBagLayout());
		selectPool.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("PoolSelect")));
		addItem(selectPool, comboBoxPools, 0, 0);

		JPanel minLengthTrack = new JPanel();
		minLengthTrack.setLayout(new GridBagLayout());
		minLengthTrack.setBorder(BorderFactory.createTitledBorder(MessageFormat
				.format(Bundle.getMessage("PoolTrackMinimum"),
						new Object[] { _track.getName() })));
		addItem(minLengthTrack, trackMinLengthTextField, 0, 0);

		trackMinLengthTextField.setText(Integer.toString(_track
				.getMinimumLength()));

		JPanel savePool = new JPanel();
		savePool.setLayout(new GridBagLayout());
		savePool.setBorder(BorderFactory.createTitledBorder(""));
		addItem(savePool, saveButton, 0, 0);

		p1.add(poolName);
		p1.add(selectPool);
		p1.add(minLengthTrack);
		p1.add(savePool);

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
		JScrollPane p2Pane = new JScrollPane(p2);
		p2Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p2Pane.setBorder(BorderFactory.createTitledBorder(""));

		// pool status panel
		poolStatus.setLayout(new GridBagLayout());

		p2.add(poolStatus);

		getContentPane().add(p1Pane);
		getContentPane().add(p2Pane);
		setTitle(Bundle.getMessage("MenuItemPoolTrack"));

		// load comboBox
		updatePoolsComboBox();
		updatePoolStatus();

		// button action - These use a convention in the OperationsFrame base
		// class that requires the events to be sorted out in
		// buttonActionPerformed.
		addButtonAction(addButton);
		addButtonAction(saveButton);

		setVisible(true);

	}

	private void updatePoolsComboBox() {
		_track.getLocation().updatePoolComboBox(comboBoxPools);
		comboBoxPools.setSelectedItem(_track.getPool());
	}

	private void updatePoolStatus() {
		// This shows the details of the current member tracks in the Pool.
		poolStatus.removeAll();

		addItemLeft(poolStatus, name, 0, 0);
		addItem(poolStatus, minimum, 1, 0);
		addItem(poolStatus, length, 2, 0);

		String poolName = "";
		if (_track.getPool() != null) {
			Pool pool = _track.getPool();
			poolName = pool.getName();
			List<Track> tracks = pool.getTracks();
			int totalMinLength = 0;
			int totalLength = 0;
			for (int i = 0; i < tracks.size(); i++) {
				Track track = tracks.get(i);
				JLabel name = new JLabel();
				name.setText(track.getName());

				JLabel minimum = new JLabel();
				minimum.setText(Integer.toString(track.getMinimumLength()));
				totalMinLength = totalMinLength + track.getMinimumLength();

				JLabel length = new JLabel();
				length.setText(Integer.toString(track.getLength()));
				totalLength = totalLength + track.getLength();

				addItemLeft(poolStatus, name, 0, i + 1);
				addItem(poolStatus, minimum, 1, i + 1);
				addItem(poolStatus, length, 2, i + 1);
			}
			// Summary
			JLabel total = new JLabel(Bundle.getMessage("Totals"));
			addItem(poolStatus, total, 0, tracks.size() + 1);
			JLabel totalMin = new JLabel();
			totalMin.setText(Integer.toString(totalMinLength));
			addItem(poolStatus, totalMin, 1, tracks.size() + 1);
			JLabel totalLen = new JLabel();
			totalLen.setText(Integer.toString(totalLength));
			addItem(poolStatus, totalLen, 2, tracks.size() + 1);
		}
		poolStatus
				.setBorder(BorderFactory.createTitledBorder(MessageFormat
						.format(Bundle.getMessage("PoolTracks"),
								new Object[] { poolName })));
		poolStatus.repaint();
		poolStatus.validate();
		setPreferredSize(null); // kill JMRI window size
		pack();
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addButton) {
			Location location = _track.getLocation();
			location.addPool(trackPoolNameTextField.getText());
		}

		if (ae.getSource() == saveButton) {
			try {
				_track.setMinimumLength(Integer
						.parseInt(trackMinLengthTextField.getText()));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						Bundle.getMessage("TrackMustBeNumber"),
						Bundle.getMessage("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (comboBoxPools.getSelectedItem() != null
					&& comboBoxPools.getSelectedItem().equals("")) {
				_track.setPool(null);
				if (_pool != null)
					_pool.removePropertyChangeListener(this);
				_pool = null;
			} else if (comboBoxPools.getSelectedItem() != null
					&& !comboBoxPools.getSelectedItem().equals("")) {
				if (_pool != null)
					_pool.removePropertyChangeListener(this);
				_pool = (Pool) comboBoxPools.getSelectedItem();
				_pool.addPropertyChangeListener(this);
				_track.setPool(_pool);
			}

			// save location file
			OperationsXml.save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void dispose() {
		_track.removePropertyChangeListener(this);
		_track.getLocation().removePropertyChangeListener(this);
		if (_pool != null)
			_pool.removePropertyChangeListener(this);
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// This should move to the base class
		// Just call LogEvent(e); instead. It will figure out if logging is
		// enabled, etc.
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: "
					+ e.getOldValue() + " new: " + e.getNewValue());	// NOI18N

		if (e.getPropertyName().equals(Location.POOL_LENGTH_CHANGED_PROPERTY))
			updatePoolsComboBox();

		if (e.getPropertyName().equals(Pool.LISTCHANGE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName()
						.equals(Track.MIN_LENGTH_CHANGED_PROPERTY))
			updatePoolStatus();
	}

	static Logger log = LoggerFactory
			.getLogger(PoolTrackFrame.class.getName());
}
