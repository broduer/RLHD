package rs117.hd.tooling.enviroment;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.GroundMaterialManager;
import rs117.hd.scene.TextureManager;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.impl.EditorPanel;
import rs117.hd.tooling.enviroment.impl.GroundMaterialsPanel;
import rs117.hd.utils.HDUtils;

import static rs117.hd.tooling.enviroment.EditorUtils.castToBoolean;
import static rs117.hd.tooling.enviroment.EditorUtils.castToColor;
import static rs117.hd.tooling.enviroment.EditorUtils.castToFloat;
import static rs117.hd.tooling.enviroment.EditorUtils.castToFloatArray;
import static rs117.hd.tooling.enviroment.EditorUtils.castToInt;
import static rs117.hd.tooling.enviroment.EditorUtils.getColorValue;
import static rs117.hd.utils.HDUtils.reverseSunAngles;

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

	@Inject
	private TextureManager textureManager;

	@Inject
	private GroundMaterialManager groundMaterialManager;

	private JPanel editorContent = new JPanel();

	private JPanel groundMaterialPanel = new JPanel();

	public RuneliteColorPicker colorPicker;

	public Map<String, PropertyData> properties = new LinkedHashMap<>() {{
		// Environmental properties
		put("isUnderwater", new PropertyData(
			"Indicates if the environment is underwater.",
			boolean.class,
			(env, val) -> env.isUnderwater = castToBoolean(val),
			env -> String.valueOf(env.isUnderwater)
		));
		put("allowSkyOverride", new PropertyData(
			"Allows overriding the sky.",
			boolean.class,
			(env, val) -> env.allowSkyOverride = castToBoolean(val),
			env -> String.valueOf(env.allowSkyOverride)
		));
		put("lightningEffects", new PropertyData(
			"Enables lightning effects.",
			boolean.class,
			(env, val) -> env.lightningEffects = castToBoolean(val),
			env -> String.valueOf(env.lightningEffects)
		));

		put("instantTransition", new PropertyData(
			"Enables instant Transitions.",
			boolean.class,
			(env, val) -> env.instantTransition = castToBoolean(val),
			env -> String.valueOf(env.instantTransition)
		));

		// Light properties
		put("ambientColor", new PropertyData(
			"Ambient light color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.ambientColor = castToColor(val),
			env -> getColorValue(env.ambientColor)
		));
		put("ambientStrength", new PropertyData(
			"Ambient light strength multiplier. Defaults to 1.",
			float.class,
			(env, val) -> env.ambientStrength = castToFloat(val),
			env -> String.valueOf(env.ambientStrength)
		));
		put("directionalColor", new PropertyData(
			"Directional light color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.directionalColor = castToColor(val),
			env -> getColorValue(env.directionalColor)
		));
		put("directionalStrength", new PropertyData(
			"Directional light strength multiplier. Defaults to 0.25.",
			float.class,
			(env, val) -> env.directionalStrength = castToFloat(val),
			env -> String.valueOf(env.directionalStrength)
		));

		// Water properties
		put("waterColor", new PropertyData(
			"Water color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.waterColor = castToColor(val),
			env -> getColorValue(env.waterColor)
		));
		put("waterCausticsColor", new PropertyData(
			"Water caustics color in sRGB (as hex or array). Defaults to the environment's directional light color.",
			Color.class,
			(env, val) -> env.waterCausticsColor = castToColor(val),
			env -> getColorValue(env.waterCausticsColor)
		));
		put("waterCausticsStrength", new PropertyData(
			"Water caustics strength. Defaults to the environment's directional light strength.",
			float.class,
			(env, val) -> env.waterCausticsStrength = castToFloat(val),
			env -> String.valueOf(env.waterCausticsStrength)
		));

		// Underglow properties
		put("underglowColor", new PropertyData(
			"Underglow color in sRGB (as hex or array). Acts as light emanating from the ground.",
			Color.class,
			(env, val) -> env.underglowColor = castToColor(val),
			env -> getColorValue(env.underglowColor)
		));
		put("underglowStrength", new PropertyData(
			"Underglow strength multiplier. Acts as light emanating from the ground.",
			float.class,
			(env, val) -> env.underglowStrength = castToFloat(val),
			env -> String.valueOf(env.underglowStrength)
		));

		// Other environmental effects
		put("sunAngles", new PropertyData(
			"The sun's altitude and azimuth specified in degrees in the horizontal coordinate system.",
			int[].class,
			(env, val) -> env.sunAngles = HDUtils.sunAngles(castToFloatArray(val)[0],castToFloatArray(val)[1]),
			env -> Arrays.toString(reverseSunAngles(env.sunAngles ==  null ? HDUtils.sunAngles(52, 235) : env.sunAngles))
		));
		put("fogColor", new PropertyData(
			"Sky/fog color in sRGB, specified as a hex color code or an array.",
			Color.class,
			(env, val) -> env.fogColor = castToColor(val),
			env -> getColorValue(env.fogColor)
		));
		put("fogDepth", new PropertyData(
			"Fog depth normally ranging from 0 to 100, which combined with draw distance decides the fog amount. Defaults to 25.",
			float.class,
			(env, val) -> env.fogDepth = castToFloat(val),
			env -> String.valueOf(env.fogDepth)
		));

		// Ground fog properties
		put("groundFogStart", new PropertyData(
			"Only matters with groundFogOpacity > 0. Specified in local units.",
			int.class,
			(env, val) -> env.groundFogStart = castToInt(val),
			env -> String.valueOf(env.groundFogStart)
		));
		put("groundFogEnd", new PropertyData(
			"Only matters with groundFogOpacity > 0. Specified in local units.",
			int.class,
			(env, val) -> env.groundFogEnd = castToInt(val),
			env -> String.valueOf(env.groundFogEnd)
		));
		put("groundFogOpacity", new PropertyData(
			"Ground fog opacity ranging from 0 to 1, meaning no ground fog and full ground fog respectively. Defaults to 0.",
			int.class,
			(env, val) -> env.groundFogOpacity = castToInt(val),
			env -> String.valueOf(env.groundFogOpacity)
		));
	}};


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
			groundMaterialPanel = new GroundMaterialsPanel(clientThread, client, environmentManager, this,textureManager,groundMaterialManager);

			JPanel display = new JPanel();
			MaterialTabGroup tabGroup = new MaterialTabGroup(display);
			MaterialTab editorTab = new MaterialTab("Environment Editor", tabGroup, editorContent);
			MaterialTab searchTab = new MaterialTab("Ground Materials Editor", tabGroup, groundMaterialPanel);

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
		for (int i = 0; i < environmentManager.getEnvironments().length; i++) {
			if (Objects.equals(environmentManager.getEnvironments()[i].name(), name)) {
				return environmentManager.getEnvironments()[i];
			}
		}
		return null;
	}

}