package rs117.hd.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import rs117.hd.data.environments.Area;
import rs117.hd.scene.environments.Environment;

public class EnvironmentSerializer implements JsonSerializer<Environment> {
	@Override
	public JsonElement serialize(Environment src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();


		// Serialize instance fields
		serializeFields(src, jsonObject, Environment.class.getDeclaredFields(), context);

		// Serialize static fields
		//serializeFields(Environment.class, jsonObject, Environment.class.getDeclaredFields(), context);

		return jsonObject;
	}

	private final rs117.hd.data.environments.Environment defaultEnv = rs117.hd.data.environments.Environment.DEFAULT;

	private void serializeFields(Environment env, JsonObject jsonObject, Field[] fields, JsonSerializationContext context) {
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue; // Skip static fields if you have already handled them elsewhere
			}

			field.setAccessible(true);
			try {
				var name = field.getName();
				Object value = field.get(env);
				if (value != null) {
					
					if (name.equals("area") && value == Area.NONE)
						continue;
					if (name.equals("waterCausticsColor") && value == env.directionalColor)
						continue;
					if (name.equals("waterCausticsStrength") && (float) value == env.directionalStrength)
						continue;

					final float EPS = .005f;

					if (value instanceof float[]) {
						float[] src = (float[]) value;

						// Color
						if (src.length == 3) {
							src = ColorUtils.linearToSrgb((float[]) value);
							for (int i = 0; i < 3; i++)
								src[i] *= 255;

							// See if it can fit in a hex color code
							boolean canfit = true;
							int[] c = new int[3];
							for (int i = 0; i < 3; i++) {
								float f = src[i];
								c[i] = Math.round(f);
								if (Math.abs(f - c[i]) > EPS) {
									canfit = false;
									break;
								}
							}

							if (canfit) {
								// Serialize it as a hex color code
								value = String.format("#%02x%02x%02x", c[0], c[1], c[2]);
							} else {
								value = src;
							}
						}

						// Angles
						if (src.length == 2) {
							boolean wholeNumbers = true;
							int[] rounded = new int[2];
							for (int i = 0; i < 2; i++) {
								src[i] = (float) Math.toDegrees(src[i]);
								rounded[i] = Math.round(src[i]);
								if (Math.abs(rounded[i] - src[i]) > EPS)
									wholeNumbers = false;
							}

							value = wholeNumbers ? rounded : src;
						}
					}

					if (value instanceof Float) {
						float f = (float) value;
						if (Math.abs(Math.round(f) - f) <= EPS) {
							value = (int) f;
						}
					}

					JsonElement fieldJson = context.serialize(value);
					jsonObject.add(field.getName(), fieldJson);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}