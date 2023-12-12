package rs117.hd.tooling.enviroment.components.impl;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import rs117.hd.HdPlugin;
import rs117.hd.tooling.enviroment.components.ComponentData;

public class ColorPicker extends ComponentData {

	ColorJButton colorPickerBtn;

	public void create() {
		Color existing = Color.decode(value);

		boolean alphaHidden = true;

		if (existing == null) {
			colorPickerBtn = new ColorJButton("Pick a color", Color.BLACK);
		} else {
			String colorHex =
				"#" + (alphaHidden ? ColorUtil.colorToHexCode(existing) : ColorUtil.colorToAlphaHexCode(existing)).toUpperCase();
			colorPickerBtn = new ColorJButton(colorHex, existing);
		}

		colorPickerBtn.setFocusable(false);
		colorPickerBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				environmentEditor.colorPicker = environmentEditor.colorPickerManager.create(SwingUtilities.windowForComponent(
						environmentEditor),
					Color.decode(value), environment.name() + " : " + key, false
				);
				environmentEditor.colorPicker.setOnColorChange(c -> {
					value = c.toString();
					colorPickerBtn.setColor(c);
					String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
					environmentEditor.setValue(environment, key, hex);
				});
				environmentEditor.colorPicker.setVisible(true);
			}
		});
		component = colorPickerBtn;
	}

}
