package rs117.hd.tooling.enviroment.impl;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import rs117.hd.HdPlugin;
import rs117.hd.data.materials.GroundMaterial;
import rs117.hd.data.materials.Material;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.GroundMaterialManager;
import rs117.hd.scene.TextureManager;
import rs117.hd.tooling.enviroment.EnvironmentEditor;

import static rs117.hd.utils.ResourcePath.path;

@Slf4j
public class GroundMaterialsPanel extends JPanel {

	private EnvironmentEditor editor;
	private EnvironmentManager environmentManager;
	private Client client;
	private ClientThread clientThread;
	private JPanel attributePanel = new JPanel(new GridLayout(0, 2));
	private JTree environmentTree;

	private TextureManager textureManager;
	private GroundMaterialManager groundMaterialManager;

	private JScrollPane infoScrollPane;
	private JSplitPane split;

	public GroundMaterialsPanel(ClientThread clientThread, Client client,
		EnvironmentManager environmentManager, EnvironmentEditor environmentEditor,
		TextureManager textureManager, GroundMaterialManager groundMaterialManager) {
		this.clientThread = clientThread;
		this.client = client;
		this.environmentManager = environmentManager;
		this.editor = environmentEditor;
		this.textureManager = textureManager;
		this.groundMaterialManager = groundMaterialManager;

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		createAttrPanel();
		createEnvironmentTree();

		JScrollPane treeScrollPane = new JScrollPane(environmentTree);
		treeScrollPane.setPreferredSize(new Dimension(220, 400));

		infoScrollPane = new JScrollPane(createCustomPanel(GroundMaterial.DIRT));
		infoScrollPane.setPreferredSize(new Dimension(400, 400));

		JPanel bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.SOUTH);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, infoScrollPane);
		add(split, BorderLayout.CENTER);

		editor.colorPicker = editor.colorPickerManager.create(
			SwingUtilities.windowForComponent(this),
			Color.BLUE, "Editor", false
		);
	}

	public JPanel createCustomPanel(GroundMaterial groundMaterial) {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Top panel with label and text field
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel label = new JLabel("Edit Name:");
		JTextField textField = new JTextField(20);
		textField.setText(capitalizeWords(groundMaterial.name));
		topPanel.add(label);
		topPanel.add(textField);
		mainPanel.add(topPanel, BorderLayout.NORTH);

		// Separator
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		mainPanel.add(separator, BorderLayout.NORTH);

		// Panel to hold the boxes
		JPanel gridPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		int boxesPerRow = 3;
		int index = 0;

		// Add material boxes to the grid
		for (Material material : groundMaterial.materials) {
			JPanel box = createMaterialContainer(groundMaterial, material);
			gbc.gridx = index % boxesPerRow;
			gbc.gridy = index / boxesPerRow;
			gridPanel.add(box, gbc);
			index++;
		}

		// Add the new box panel to the grid
		JPanel newBoxPanel = createAddMaterialContainer(groundMaterial);
		gbc.gridx = index % boxesPerRow;
		gbc.gridy = index / boxesPerRow;
		gridPanel.add(newBoxPanel, gbc);

		mainPanel.add(gridPanel, BorderLayout.CENTER);

		return mainPanel;
	}
	public JPanel createMaterialContainer(GroundMaterial groundMaterial, Material material) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(145, 145));
		panel.setLayout(new BorderLayout());

		// Load the image from the texture manager
		Image image = getImageForMaterial(material);
		ImageIcon backgroundIcon = new ImageIcon(image);

		JLabel backgroundLabel = new JLabel(backgroundIcon);
		backgroundLabel.setLayout(new BorderLayout());

		JLabel topLabel = new JLabel(capitalizeWords(material.name()));
		topLabel.setForeground(Color.black);
		topLabel.setHorizontalAlignment(SwingConstants.CENTER);
		topLabel.setVerticalAlignment(SwingConstants.CENTER);

		// Create a button and position it at the bottom
		JButton button = new JButton("Remove");
		button.addActionListener(e -> {
			String groundMaterialName = groundMaterial.name.replace(" ", "");
			int index = GroundMaterialManager.lookupIndex(groundMaterialName);

			// Find the index of the material to remove
			int materialIndexToRemove = -1;
			for (int i = 0; i < groundMaterial.materials.length; i++) {
				if (groundMaterial.materials[i].equals(material)) {
					materialIndexToRemove = i;
					break;
				}
			}

			if (materialIndexToRemove != -1) {
				// Create a new array with one less element
				Material[] newMaterials = new Material[groundMaterial.materials.length - 1];

				// Copy elements from original array to new array, excluding the one to be removed
				for (int i = 0, j = 0; i < groundMaterial.materials.length; i++) {
					if (i != materialIndexToRemove) {
						newMaterials[j++] = groundMaterial.materials[i];
					}
				}

				GroundMaterialManager.GROUND_MATERIALS[index].materials = newMaterials;
			}
			groundMaterialManager.reload();
			infoScrollPane.setViewportView(createCustomPanel(groundMaterial));
		});

		backgroundLabel.add(topLabel, BorderLayout.NORTH);
		backgroundLabel.add(button, BorderLayout.SOUTH);
		panel.add(backgroundLabel, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		return panel;
	}

	public JPanel createAddMaterialContainer(GroundMaterial groundMaterial) {
		AtomicReference<Material> selectedMaterial = new AtomicReference<>(Material.NONE);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(145, 145));
		panel.setLayout(new BorderLayout());

		ImageIcon backgroundIcon = new ImageIcon(getImageForMaterial(selectedMaterial.get()));

		JLabel backgroundLabel = new JLabel(backgroundIcon);
		backgroundLabel.setLayout(new BorderLayout());

		JLabel topLabel = new JLabel(capitalizeWords(selectedMaterial.get().name()));
		topLabel.setForeground(Color.black);
		topLabel.setHorizontalAlignment(SwingConstants.CENTER);
		topLabel.setVerticalAlignment(SwingConstants.CENTER);

		JButton button = new JButton("Add");
		button.addActionListener(e -> {
			String groundMaterialName = groundMaterial.name.replace(" ", "");
			int index = GroundMaterialManager.lookupIndex(groundMaterialName);
			Material[] materials = Arrays.copyOf(groundMaterial.materials, groundMaterial.materials.length + 1);
			materials[materials.length - 1] = selectedMaterial.get();
			GroundMaterialManager.GROUND_MATERIALS[index].materials = materials;
			groundMaterialManager.reload();
			infoScrollPane.setViewportView(createCustomPanel(groundMaterial));
		});

		JComboBox<String> comboBox = new JComboBox<>();
		for (Material value : Material.values()) {
			comboBox.addItem(capitalizeWords(value.name()));
		}
		comboBox.setPreferredSize(new Dimension(135, 25));

		comboBox.addActionListener(e -> {
			String selectedItem = (String) comboBox.getSelectedItem();
			Material material = Material.valueOf(selectedItem.replace(" ", "_").toUpperCase());
			selectedMaterial.set(material);
			backgroundIcon.setImage(getImageForMaterial(selectedMaterial.get()));
			backgroundLabel.setIcon(backgroundIcon);
			topLabel.setText(capitalizeWords(selectedMaterial.get().name()));
		});

		JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		middlePanel.setOpaque(false);
		middlePanel.add(comboBox);

		backgroundLabel.add(topLabel, BorderLayout.NORTH);
		backgroundLabel.add(middlePanel, BorderLayout.CENTER);
		backgroundLabel.add(button, BorderLayout.SOUTH);

		panel.add(backgroundLabel, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		return panel;
	}

	private Image getImageForMaterial(Material material) {
		try {
			Image image = textureManager.loadTextureImage(material);

			if (image == null) {
				image = path(HdPlugin.class, "empty.png").loadImage();
			}
			return image.getScaledInstance(145, 145, Image.SCALE_SMOOTH);
		} catch (Exception e) {
			return null;
		}
	}

	private void createAttrPanel() {
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

	private void createEnvironmentTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Ground Materials");
		environmentTree = new JTree(root);

		for (GroundMaterial groundMaterial : GroundMaterialManager.GROUND_MATERIALS) {
			String materialName = capitalizeWords(groundMaterial.name);
			DefaultMutableTreeNode environmentNode = new DefaultMutableTreeNode(materialName);
			root.add(environmentNode);
		}

		environmentTree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) environmentTree.getLastSelectedPathComponent();

			if (selectedNode != null) {
				String selectedMaterialName = (String) selectedNode.getUserObject();
				infoScrollPane.setViewportView(createCustomPanel(GroundMaterialManager.lookup(selectedMaterialName.replace(" ", "_").toUpperCase())));
			}
		});

		environmentTree.expandRow(0);
	}

	public static String capitalizeWords(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		String[] words = input.replace("_", " ").split("\\s+");
		StringBuilder capitalizedString = new StringBuilder();

		for (int i = 0; i < words.length; i++) {
			if (i > 0) {
				capitalizedString.append(" ");
			}
			capitalizedString.append(Character.toUpperCase(words[i].charAt(0)));
			if (words[i].length() > 1) {
				capitalizedString.append(words[i].substring(1).toLowerCase());
			}
		}

		return capitalizedString.toString();
	}
}
