package rs117.hd.tooling.enviroment.components.impl;

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import java.awt.Component;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rs117.hd.HdPlugin;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.tooling.enviroment.components.ComponentData;

public class IntSpinner extends ComponentData {

	@Inject
	private EnvironmentManager environmentManager;


	JSpinner spinner;

	public void create() {
		int min = 0, max = Integer.MAX_VALUE;
		if (range != null) {
			min = range[0];
			max = range[1];
		}

		// Config may previously have been out of range
		int startingValue = Ints.constrainToRange((int) Float.parseFloat(value), min, max);

		SpinnerModel model = new SpinnerNumberModel(startingValue, min, max, 1);
		spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(6);
		spinnerTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void update() {
				environmentEditor.setValue(environment, key, spinnerTextField.getValue());
			}
		});
		component = spinner;
	}

}
