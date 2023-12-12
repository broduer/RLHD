package rs117.hd.tooling.enviroment.components.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import net.runelite.client.ui.ColorScheme;
import rs117.hd.HdPlugin;
import rs117.hd.tooling.enviroment.components.ComponentData;

public class CheckBox extends ComponentData {

	JCheckBox checkbox = new JCheckBox();

	public void create() {
		checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		checkbox.setSelected(Objects.equals(value, "true"));
		checkbox.addActionListener(ae -> {
			environmentEditor.setValue(environment, key, checkbox.isSelected());
		});
		component = checkbox;
	}

}
