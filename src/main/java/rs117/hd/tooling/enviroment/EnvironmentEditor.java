package rs117.hd.tooling.enviroment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.devtools.DevToolsFrame;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import rs117.hd.HdPlugin;
import rs117.hd.data.environments.Area;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.components.ComponentData;
import rs117.hd.tooling.enviroment.components.impl.CheckBox;
import rs117.hd.utils.ColorUtils;
import rs117.hd.utils.EnvironmentSerializer;

import static rs117.hd.utils.ColorUtils.rgb;
import static rs117.hd.utils.ResourcePath.path;

@Slf4j
public class EnvironmentEditor extends DevToolsFrame {

	private JTree environmentTree;

	private JPanel attributePanel = new JPanel(new GridLayout(0, 2));

	@Inject
	public ColorPickerManager colorPickerManager;

	@Inject
	private ClientThread clientThread;
	@Inject
	private EnvironmentManager environmentManager;
	@Inject
	private Client client;

	private List<String> editedEnvironments = new ArrayList<String>();

	public RuneliteColorPicker colorPicker;

	@Inject
	private HdPlugin hdPlugin;

	private Environment currentlySelectedEnvironment;

	Map<String, PropertyData> properties = Map.ofEntries(
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

	}

	public String getColorValue(float[] color) {
		return ColorUtils.linearToSrgbHex(color != null ? color : ColorUtils.rgb("#FFFFFF"));
	}

	public void open() {
		setTitle("Environment Editor");

		setLayout(new BorderLayout());

		environmentTree = new JTree(buildEnvironmentTree());
		environmentTree.setRootVisible(false);
		environmentTree.setShowsRootHandles(true);
		environmentTree.getSelectionModel().addTreeSelectionListener(e ->
		{
			String selectedNode = e.getPath().getLastPathComponent().toString().toUpperCase().replace(" ", "_");

			Environment matchingEnvironment = Arrays.stream(environmentManager.environments)
				.filter(environment -> environment.toString().equals(selectedNode))
				.findFirst()
				.orElse(Environment.DEFAULT);

			environmentManager.forcedEnvironment = false;
			currentlySelectedEnvironment = matchingEnvironment;
			updateView(currentlySelectedEnvironment);
		});

		createAttrPanel();

		updateView(Environment.DEFAULT);

		final JScrollPane treeScrollPane = new JScrollPane(environmentTree);
		treeScrollPane.setPreferredSize(new Dimension(200, 400));

		final JScrollPane infoScrollPane = new JScrollPane(attributePanel);
		infoScrollPane.setPreferredSize(new Dimension(400, 400));


		final JPanel bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.SOUTH);

		final JButton refreshWidgetsBtn = new JButton("Reset");
		refreshWidgetsBtn.addActionListener(e -> {
			environmentManager.startUp();
			editedEnvironments.clear();
			updateView(Environment.DEFAULT);
			environmentManager.forcedEnvironment = false;
		});
		bottomPanel.add(refreshWidgetsBtn);

		final JButton forceEnvironment = new JButton("Force Environment");
		forceEnvironment.setToolTipText("Force Selected Environment");
		forceEnvironment.addActionListener(e -> {
			System.out.println(currentlySelectedEnvironment.name());
			environmentManager.forceEnvironment(currentlySelectedEnvironment, true);
		});
		bottomPanel.add(forceEnvironment);

		final JButton revalidateWidget = new JButton("Save");
		revalidateWidget.addActionListener(e -> {
			save();
		});
		bottomPanel.add(revalidateWidget);

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, infoScrollPane);
		add(split, BorderLayout.CENTER);

		colorPicker = colorPickerManager.create(
			SwingUtilities.windowForComponent(
				this),
			Color.BLUE, "Editor", false
		);

		updateView(Environment.OVERWORLD);

		pack();
		super.open();
	}

	public void save() {
		editedEnvironments.forEach(name -> System.out.println("EDITED: " + name));
		try {
			Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Environment.class, new EnvironmentSerializer())
				.serializeNulls()
				.create();

			path("src/main/resources/rs117/hd/scene/environments.json")
				.writeString(gson.toJson(environmentManager.environments));
		} catch (Exception e) {

		}
	}

	public void createAttrPanel() {

		JLabel header1 = new JLabel("Type", SwingConstants.CENTER);
		header1.setForeground(Color.WHITE);
		header1.setOpaque(true);
		header1.setBackground(Color.decode("#232323"));
		header1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#1B1B1B")));

		JLabel header2 = new JLabel("Value", SwingConstants.CENTER);
		header2.setForeground(Color.WHITE);
		header2.setOpaque(true);
		header2.setBackground(Color.decode("#232323"));
		header2.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.decode("#1B1B1B")));

		attributePanel.add(header1);
		attributePanel.add(header2);
	}

	public void updateView(Environment environment) {
		for (int i = attributePanel.getComponentCount() - 1; i >= 2; i--) {
			attributePanel.remove(i);
		}
		if (colorPicker != null) {
			colorPicker.setVisible(false);
		}
		properties.forEach((key, propertyData) -> {
			JLabel propName = new JLabel(key);
			propName.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.decode("#1B1B1B")));
			propName.setToolTipText("<html><p style='width:200px;'>" + propertyData.getDescription() + "</p></html>");
			propName.setOpaque(true);
			attributePanel.add(propName);

			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.decode("#1B1B1B")));


			JPanel cardLayoutPanel = new JPanel(new CardLayout());
			panel.add(cardLayoutPanel, BorderLayout.CENTER);

			panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.decode("#1B1B1B")));
			ComponentData componentData = propertyData.getComponentData();
			String layout = "";
			if (propertyData.getComponentData() == null) {
				componentData = new CheckBox();
				layout = BorderLayout.EAST;
			}
			componentData.environmentEditor = this;
			componentData.environment = environment;
			componentData.key = key;
			componentData.value = getValue(environment, key);
			componentData.create();
			if (layout.isEmpty()) {
				cardLayoutPanel.add(componentData.component);
			} else {
				panel.add(componentData.component, layout);
			}
			attributePanel.add(panel);
		});

		attributePanel.revalidate();
		attributePanel.repaint();
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
			if (!editedEnvironments.contains(targetEnvironment.name())) {
				editedEnvironments.add(targetEnvironment.name());
			}
			if (client.getGameState() == GameState.LOGGED_IN) {
				clientThread.invoke(() -> environmentManager.reset());
			}
		} else {
			log.info("Key not found: {}", key);
		}
	}

	// Utility methods for casting values
	private boolean castToBoolean(Object value) {
		return (boolean) value;
	}

	private float castToFloat(Object value) {
		return (float) value;
	}

	private int castToInt(Object value) {
		return (int) value;
	}

	private float[] castToColor(Object value) {
		return rgb(value.toString());
	}

	// Method to find environment by name
	private Environment findEnvironmentByName(String name) {
		for (int i = 0; i < environmentManager.environments.length; i++) {
			if (Objects.equals(environmentManager.environments[i].name(), name)) {
				return environmentManager.environments[i];
			}
		}
		return null;
	}

	@Override
	public void close() {
		super.close();
	}

	public DefaultMutableTreeNode buildEnvironmentTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Environments");
		addEnvironmentNodes(root, "Mainland", false);
		addEnvironmentNodes(root, "Underground", true);
		return root;
	}

	private void addEnvironmentNodes(DefaultMutableTreeNode parent, String label, boolean underground) {
		DefaultMutableTreeNode node = (underground ? new DefaultMutableTreeNode(label) : parent);
		if (underground) {
			parent.add(node);
		}

		List<Environment> environments = Arrays.stream(environmentManager.environments)
			.filter(env -> env.area != null && (underground != Area.OVERWORLD.intersects(env.area)))
			.collect(Collectors.toList());

		environments.forEach(env -> node.add(new DefaultMutableTreeNode(new NodeData(env))));
		rearrangeNodes(node);
	}

	private void rearrangeNodes(DefaultMutableTreeNode parent) {
		List<DefaultMutableTreeNode> nodesToRearrange = new ArrayList<>();

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
			nodesToRearrange.addAll(findNodesToReparent(childNode, parent));
		}

		for (DefaultMutableTreeNode nodeToMove : nodesToRearrange) {
			reparentNode(nodeToMove, parent);
		}
	}

	private List<DefaultMutableTreeNode> findNodesToReparent(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
		List<DefaultMutableTreeNode> nodesToReparent = new ArrayList<>();
		NodeData nodeData = (NodeData) node.getUserObject();

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode potentialParentNode = (DefaultMutableTreeNode) parent.getChildAt(i);
			if (node != potentialParentNode) {
				NodeData potentialParentData = (NodeData) potentialParentNode.getUserObject();
				if (shouldReparent(nodeData.getData(), potentialParentData.getData())) {
					nodesToReparent.add(node);
					break;
				}
			}
		}

		return nodesToReparent;
	}

	private void reparentNode(DefaultMutableTreeNode nodeToMove, DefaultMutableTreeNode currentParent) {
		NodeData dataToMove = (NodeData) nodeToMove.getUserObject();

		for (int i = 0; i < currentParent.getChildCount(); i++) {
			DefaultMutableTreeNode potentialParentNode = (DefaultMutableTreeNode) currentParent.getChildAt(i);
			if (!potentialParentNode.isNodeDescendant(nodeToMove)) {
				NodeData potentialParentData = (NodeData) potentialParentNode.getUserObject();
				if (shouldReparent(dataToMove.getData(), potentialParentData.getData())) {
					currentParent.remove(nodeToMove);
					potentialParentNode.add(nodeToMove);
					break;
				}
			}
		}
	}

	private static boolean shouldReparent(Environment child, Environment parent) {
		// Replace this logic with your own criteria
		return parent.area.intersects(child.area);
	}

}