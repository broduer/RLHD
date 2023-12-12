package rs117.hd.tooling.enviroment;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import rs117.hd.scene.environments.Environment;
import rs117.hd.tooling.enviroment.components.ComponentData;
import rs117.hd.tooling.enviroment.components.impl.ColorPicker;
import rs117.hd.tooling.enviroment.components.impl.FloatSpinner;
import rs117.hd.tooling.enviroment.components.impl.IntSpinner;

@Data
public class PropertyData {

	private String propertyName;
	private String description;
	private Object object;
	private BiConsumer<Environment, Object> setter;
	private Function<Environment, String> getter;
	private ComponentData componentData = null;

	public PropertyData(String description, Object object, BiConsumer<Environment, Object> setter, Function<Environment, String> getter) {
		this.description = description;
		this.object = object;
		this.getter = getter;
		this.setter = setter;
		if (getObject() == float.class) {
			componentData = new FloatSpinner();
		} else if (getObject() == int.class) {
			componentData = new IntSpinner();
		} else if (getObject() == Color.class) {
			componentData = new ColorPicker();
		}
	}

	public PropertyData(String description, BiConsumer<Environment, Object> setter, Function<Environment, String> getter) {
		this.description = description;
		this.object = null;
		this.getter = getter;
		this.setter = setter;
	}


	@Override
	public String toString() {
		return "PropertyDescription{" +
			   "propertyName='" + propertyName + '\'' +
			   ", description='" + description + '\'' +
			   '}';
	}
}