package rs117.hd.scene;

import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import rs117.hd.HdPlugin;
import rs117.hd.data.materials.GroundMaterial;
import rs117.hd.utils.FileWatcher;
import rs117.hd.utils.Props;
import rs117.hd.utils.ResourcePath;

import static rs117.hd.utils.ResourcePath.path;

@Slf4j
public class GroundMaterialManager {
	private static final ResourcePath GROUND_MATERIALS_PATH = Props.getPathOrDefault(
		"rlhd.ground-materials-path",
		() -> path(AreaManager.class, "ground_materials.json")
	);

	@Inject
	private HdPlugin plugin;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TileOverrideManager tileOverrideManager;

	private FileWatcher.UnregisterCallback fileWatcher;

	public static GroundMaterial[] GROUND_MATERIALS = new GroundMaterial[0];

	public void startUp() {
		fileWatcher = GROUND_MATERIALS_PATH.watch((path, first) -> {
			try {
				GroundMaterial[] groundMaterials = path.loadJson(plugin.getGson(), GroundMaterial[].class);
				if (groundMaterials == null)
					throw new IOException("Empty or invalid: " + path);

				GROUND_MATERIALS = new GroundMaterial[groundMaterials.length + 2];
				GROUND_MATERIALS[0] = GroundMaterial.NONE;
				GROUND_MATERIALS[1] = GroundMaterial.DIRT;
				System.arraycopy(groundMaterials, 0, GROUND_MATERIALS, 2, groundMaterials.length);

				if (!first) {
					clientThread.invoke(this::reload);
				}
			} catch (IOException ex) {
				log.error("Failed to load ground materials:", ex);
			}
		});
	}

	public void reload() {
		clientThread.invoke(() -> {
			// Reload everything which depends on ground materials
			tileOverrideManager.shutDown();
			tileOverrideManager.startUp();
			plugin.reuploadScene();
		});
	}

	public static int lookupIndex(String name) {
		for (int i = 0; i < GROUND_MATERIALS.length; i++) {
			if (GROUND_MATERIALS[i].getName().equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}

	public static GroundMaterial lookup(String name) {
		for (GroundMaterial groundMaterial : GROUND_MATERIALS) {
			if (groundMaterial.getName().equalsIgnoreCase(name)) {
				return groundMaterial;
			}
		}
		return GroundMaterial.NONE;
	}

	public void shutDown() {
		if (fileWatcher != null)
			fileWatcher.unregister();
		fileWatcher = null;
		GROUND_MATERIALS = new GroundMaterial[0];
	}
}
