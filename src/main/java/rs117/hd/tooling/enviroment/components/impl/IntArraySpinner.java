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
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.tooling.enviroment.components.ComponentData;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class IntArraySpinner extends ComponentData {

	@Inject
	private EnvironmentManager environmentManager;

	JSpinner spinner1;
	JSpinner spinner2;

	int value1;
	int value2;

	public void create() {
		int min = 0, max = Integer.MAX_VALUE;
		if (range != null) {
			min = range[0];
			max = range[1];
		}

		System.out.println("Value@ " + value);
		String floatString = value;
		floatString = floatString.substring(1, floatString.length() - 1);

		// Split the string at the comma
		String[] floatStrings = floatString.split(", ");

		// Convert the resulting substrings to float
		value1 = Integer.valueOf(floatStrings[0]);
		value2 = Integer.valueOf(floatStrings[1]);

		// Config may previously have been out of range
		int startingValue1 = Ints.constrainToRange((int) Float.parseFloat(String.valueOf(value1)), min, max);
		int startingValue2 = Ints.constrainToRange((int) Float.parseFloat(String.valueOf(value2)), min, max);

		SpinnerModel model1 = new SpinnerNumberModel(startingValue1, min, max, 1);
		SpinnerModel model2 = new SpinnerNumberModel(startingValue2, min, max, 1);

		spinner1 = new JSpinner(model1);
		spinner2 = new JSpinner(model2);

		configureSpinner(spinner1);
		configureSpinner2(spinner2);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label1 = new JLabel("Altitude: ");
		JLabel label2 = new JLabel("Azimuth: ");
		panel.add(label1);
		panel.add(spinner1);
		panel.add(label2);
		panel.add(spinner2);

		component = panel;
	}

	private void configureSpinner(JSpinner spinner) {
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(6);
		spinnerTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void removeUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void insertUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void update(JFormattedTextField textField) {
				environmentEditor.setValue(environment, key, textField.getValue() + "," + value2);
			}
		});
	}

	private void configureSpinner2(JSpinner spinner) {
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(6);
		spinnerTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void removeUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void insertUpdate(DocumentEvent e) {
				update(spinnerTextField);
			}

			public void update(JFormattedTextField textField) {
				environmentEditor.setValue(environment, key, value1 + "," + textField.getValue());
			}
		});
	}

}