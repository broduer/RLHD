package rs117.hd.tooling.enviroment;

import rs117.hd.utils.ColorUtils;

import static rs117.hd.utils.ColorUtils.rgb;

public class EditorUtils {

	public static String getColorValue(float[] color) {
		return ColorUtils.linearToSrgbHex(color != null ? color : ColorUtils.rgb("#FFFFFF"));
	}


	public static boolean castToBoolean(Object value) {
		return (boolean) value;
	}

	public static float[] castToFloatArray(Object value) {
		String floatString = (String) value;

		// Split the string at the comma
		String[] floatStrings = floatString.split(",");

		return new float[] { Integer.valueOf(floatStrings[0]), Integer.valueOf(floatStrings[1]) };
	}
	public static float castToFloat(Object value) {
		return (float) value;
	}

	public static int castToInt(Object value) {
		return (int) value;
	}

	public static float[] castToColor(Object value) {
		return rgb(value.toString());
	}

}
