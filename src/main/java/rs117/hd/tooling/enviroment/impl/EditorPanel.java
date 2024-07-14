package rs117.hd.tooling.enviroment.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import net.runelite.client.ui.ColorScheme;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.areas.Area;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.EnvironmentEditor;
import rs117.hd.tooling.enviroment.EnvironmentSerializer;
import rs117.hd.tooling.enviroment.NodeData;
import rs117.hd.tooling.enviroment.components.ComponentData;
import rs117.hd.tooling.enviroment.components.impl.CheckBox;

import static rs117.hd.utils.ResourcePath.path;

@Slf4j
public class EditorPanel extends JPanel {

	private EnvironmentEditor editor;

	private EnvironmentManager environmentManager;

	private Client client;

	private ClientThread clientThread;

	private Environment currentlySelectedEnvironment;

	private JTree environmentTree;

	private JPanel attributePanel = new JPanel(new GridLayout(0, 2));

	public EditorPanel(
		ClientThread clientThread,
		Client client,
		EnvironmentManager environmentManager,
		EnvironmentEditor environmentEditor
	) {
		this.clientThread = clientThread;
		this.client = client;
		this.environmentManager = environmentManager;
		this.editor = environmentEditor;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());
		environmentTree = new JTree(buildEnvironmentTree());
		environmentTree.setRootVisible(false);
		environmentTree.setShowsRootHandles(true);
		environmentTree.getSelectionModel().addTreeSelectionListener(e ->
		{
			String selectedNode = e.getPath().getLastPathComponent().toString().toUpperCase().replace(" ", "_");

			Environment matchingEnvironment = Arrays.stream(environmentManager.getEnvironments())
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
		treeScrollPane.setPreferredSize(new Dimension(220, 400));

		final JScrollPane infoScrollPane = new JScrollPane(attributePanel);
		infoScrollPane.setPreferredSize(new Dimension(400, 400));


		final JPanel bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.SOUTH);

		final JButton refreshWidgetsBtn = new JButton("Reset");
		refreshWidgetsBtn.addActionListener(e -> {
			environmentManager.startUp();
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

		editor.colorPicker = editor.colorPickerManager.create(
			SwingUtilities.windowForComponent(
				this),
			Color.BLUE, "Editor", false
		);

		updateView(Environment.OVERWORLD);
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
		if (editor.colorPicker != null) {
			editor.colorPicker.setVisible(false);
		}
		editor.properties.forEach((key, propertyData) -> {
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
			componentData.environmentEditor = editor;
			componentData.environment = environment;
			componentData.key = key;
			componentData.value = editor.getValue(environment, key);
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

	public void save() {
		try {
			Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Environment.class, new EnvironmentSerializer())
				.serializeNulls()
				.create();

			path("src/main/resources/rs117/hd/scene/environments.json")
				.writeString(gson.toJson(environmentManager.getEnvironments()));
		} catch (Exception e) {

		}
	}

	public DefaultMutableTreeNode buildEnvironmentTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Environments");
		addEnvironmentNodes(root, "Mainland", false);
		addEnvironmentNodes(root, "Underground", true);
		addEnvironmentNodesThemes(root,"Themes");
		return root;
	}

	private void addEnvironmentNodes(DefaultMutableTreeNode parent, String label, boolean underground) {
		DefaultMutableTreeNode node = (underground ? new DefaultMutableTreeNode(label) : parent);
		if (underground) {
			parent.add(node);
		}

		List<Environment> environments = Arrays.stream(environmentManager.getEnvironments())
			.filter(env -> env.area != null && (underground != Area.OVERWORLD.intersects(env.area)))
			.collect(Collectors.toList());


		environments.forEach(env -> node.add(new DefaultMutableTreeNode(new NodeData(env))));
		rearrangeNodes(node);
	}

	private void addEnvironmentNodesThemes(DefaultMutableTreeNode parent, String label) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
		parent.add(node);

		node.add(new DefaultMutableTreeNode(new NodeData(Environment.AUTUMN)));
		node.add(new DefaultMutableTreeNode(new NodeData(Environment.WINTER)));

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
