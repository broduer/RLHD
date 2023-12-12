package rs117.hd.tooling.enviroment;

import lombok.Data;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.components.ComponentData;

@Data
public class NodeData {
	private final String label;
	private final Environment data;

	public NodeData(Environment data) {
		this.label = data.name();
		this.data = data;
	}

	public String formatName(String name) {
		String[] parts = name.split("_");
		StringBuilder result = new StringBuilder();

		for (String part : parts) {
			if (part.length() > 1) {
				result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
			} else {
				result.append(part.toUpperCase());
			}
			result.append(" ");
		}

		return result.toString().trim();
	}

	@Override
	public String toString() {
		return formatName(label);
	}
}