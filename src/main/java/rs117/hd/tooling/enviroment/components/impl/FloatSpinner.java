package rs117.hd.tooling.enviroment.components.impl;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.Objects;
import javax.inject.Inject;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rs117.hd.HdPlugin;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.tooling.enviroment.components.ComponentData;

public class FloatSpinner extends ComponentData {

	JSpinner spinner;

	@Inject
	public FloatSpinner() {

	}

	public void create() {
		SpinnerModel model = new SpinnerNumberModel(
			Float.parseFloat(value),
			-900,
			Float.MAX_VALUE,
			0.1f
		); // Note the change in max value and step size
		spinner = new JSpinner(model);

		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(1); // Set maximum fraction digits to 1
		format.setMinimumFractionDigits(1); // Set minimum fraction digits to 1

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
				environmentEditor.setValue(environment, key, getFloatValue(spinnerTextField));
			}
		});
		component = spinner;
	}

	private float getFloatValue(JFormattedTextField textField) {
		// Get the value as Number
		Number value = (Number) textField.getValue();

		// Convert to float and round to one decimal place
		return Math.round(value.floatValue() * 10) / 10.0f;
	}

}
