package rs117.hd.tooling.enviroment;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import rs117.hd.HdPlugin;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.impl.EditorPanel;

import static rs117.hd.tooling.enviroment.EditorUtils.castToBoolean;
import static rs117.hd.tooling.enviroment.EditorUtils.castToColor;
import static rs117.hd.tooling.enviroment.EditorUtils.castToFloat;
import static rs117.hd.tooling.enviroment.EditorUtils.castToInt;
import static rs117.hd.tooling.enviroment.EditorUtils.getColorValue;

@Slf4j
public class EnvironmentEditor extends JFrame {

	@Inject
	public ColorPickerManager colorPickerManager;

	@Inject
	private ClientThread clientThread;
	@Inject
	private EnvironmentManager environmentManager;
	@Inject
	private Client client;

	private JPanel editorContent = new JPanel();

	public RuneliteColorPicker colorPicker;

	public Map<String, PropertyData> properties = Map.ofEntries(
		Map.entry("isUnderwater", new PropertyData(
			"Indicates if the environment is underwater.",
			boolean.class,
			(env, val) -> env.isUnderwater = castToBoolean(val),
			env -> String.valueOf(env.isUnderwater)
		)),
		Map.entry("allowSkyOverride", new PropertyData(
			"Allows overriding the sky.",
			boolean.class,
			(env, val) -> env.allowSkyOverride = castToBoolean(val),
			env -> String.valueOf(env.allowSkyOverride)
		)),
		Map.entry("lightningEffects", new PropertyData(
			"Enables lightning effects.",
			boolean.class,
			(env, val) -> env.lightningEffects = castToBoolean(val),
			env -> String.valueOf(env.lightningEffects)
		)),

		Map.entry("ambientColor", new PropertyData(
			"Ambient light color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.ambientColor = castToColor(val),
			env -> getColorValue(env.ambientColor)
		)),
		Map.entry("ambientStrength", new PropertyData(
			"Ambient light strength multiplier. Defaults to 1.",
			float.class,
			(env, val) -> env.ambientStrength = castToFloat(val),
			env -> String.valueOf(env.ambientStrength)
		)),
		Map.entry("directionalColor", new PropertyData(
			"Directional light color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.directionalColor = castToColor(val),
			env -> getColorValue(env.directionalColor)
		)),
		Map.entry("directionalStrength", new PropertyData(
			"Directional light strength multiplier. Defaults to 0.25.",
			float.class,
			(env, val) -> env.directionalStrength = castToFloat(val),
			env -> String.valueOf(env.directionalStrength)
		)),
		Map.entry("waterColor", new PropertyData(
			"Water color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.waterColor = castToColor(val),
			env -> getColorValue(env.waterColor)
		)),
		Map.entry("waterCausticsColor", new PropertyData(
			"Water caustics color in sRGB (as hex or array). Defaults to the environment's directional light color.",
			Color.class,
			(env, val) -> env.waterCausticsColor = castToColor(val),
			env -> getColorValue(env.waterCausticsColor)
		)),
		Map.entry("waterCausticsStrength", new PropertyData(
			"Water caustics strength. Defaults to the environment's directional light strength.",
			float.class,
			(env, val) -> env.waterCausticsStrength = castToFloat(val),
			env -> String.valueOf(env.waterCausticsStrength)
		)),
		Map.entry("underglowColor", new PropertyData(
			"Underglow color in sRGB (as hex or array). Acts as light emanating from the ground.",
			Color.class,
			(env, val) -> env.underglowColor = castToColor(val),
			env -> getColorValue(env.underglowColor)
		)),
		Map.entry("underglowStrength", new PropertyData(
			"Underglow strength multiplier. Acts as light emanating from the ground.",
			float.class,
			(env, val) -> env.underglowStrength = castToFloat(val),
			env -> String.valueOf(env.underglowStrength)
		)),
		Map.entry("sunAngles", new PropertyData(
			"The sun's altitude and azimuth specified in degrees in the horizontal coordinate system.",
			boolean.class,
			(env, val) -> env.lightningEffects = castToBoolean(val),
			env -> String.valueOf(env.lightningEffects)
		)),
		Map.entry("fogColor", new PropertyData(
			"Sky/fog color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.fogColor = castToColor(val),
			env -> getColorValue(env.fogColor)
		)),
		Map.entry("fogDepth", new PropertyData(
			"Fog depth normally ranging from 0 to 100, which combined with draw distance decides the fog amount. Defaults to 25.",
			float.class,
			(env, val) -> env.fogDepth = castToFloat(val),
			env -> String.valueOf(env.fogDepth)
		)),
		Map.entry("groundFogStart", new PropertyData(
			"Only matters with groundFogOpacity > 0. Specified in local units.",
			int.class,
			(env, val) -> env.groundFogStart = castToInt(val),
			env -> String.valueOf(env.groundFogStart)
		)),
		Map.entry("groundFogEnd", new PropertyData(
			"Only matters with groundFogOpacity > 0. Specified in local units.",
			int.class,
			(env, val) -> env.groundFogEnd = castToInt(val),
			env -> String.valueOf(env.groundFogEnd)
		)),
		Map.entry("groundFogOpacity", new PropertyData(
			"Ground fog opacity ranging from 0 to 1, meaning no ground fog and full ground fog respectively. Defaults to 0.",
			int.class,
			(env, val) -> env.groundFogOpacity = castToInt(val),
			env -> String.valueOf(env.groundFogOpacity)
		))
	);

	@Inject
	public EnvironmentEditor() {
		// Call the JFrame constructor with a title
		super("Editor");
		setSize(760, 635);

		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

	}

	public void open() {
		SwingUtilities.invokeLater(() -> {
			colorPicker = colorPickerManager.create(
				SwingUtilities.windowForComponent(
					this),
				Color.BLUE, "Editor", false
			);
			editorContent = new EditorPanel(clientThread, client, environmentManager, this);

			JPanel display = new JPanel();
			MaterialTabGroup tabGroup = new MaterialTabGroup(display);
			MaterialTab editorTab = new MaterialTab("Environment Editor", tabGroup, editorContent);
			MaterialTab searchTab = new MaterialTab("Create Environment", tabGroup, new JPanel());

			tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
			tabGroup.addTab(editorTab);
			tabGroup.addTab(searchTab);
			tabGroup.select(editorTab);

			add(tabGroup, BorderLayout.NORTH);
			add(display, BorderLayout.CENTER);

			setVisible(true); // Make the frame visible
		});
	}


	public String getValue(Environment environment, String key) {
		Function<Environment, String> retriever = properties.get(key).getGetter();
		if (retriever != null) {
			return retriever.apply(environment);
		}
		return ""; // Or handle unknown keys differently
	}

	public void setValue(Environment environment, String key, Object value) {
		Environment targetEnvironment = findEnvironmentByName(environment.name());
		if (targetEnvironment == null) {
			log.info("Unable to find Environment: {}", environment.name());
			return;
		}

		BiConsumer<Environment, Object> setter = properties.get(key).getSetter();
		if (setter != null) {
			setter.accept(targetEnvironment, value);
			if (client.getGameState() == GameState.LOGGED_IN) {
				clientThread.invoke(() -> environmentManager.reset());
			}
		} else {
			log.info("Key not found: {}", key);
		}
	}

	private Environment findEnvironmentByName(String name) {
		for (int i = 0; i < environmentManager.environments.length; i++) {
			if (Objects.equals(environmentManager.environments[i].name(), name)) {
				return environmentManager.environments[i];
			}
		}
		return null;
	}

}