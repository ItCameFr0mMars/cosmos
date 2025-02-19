package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

public class Font extends Module {
	public static Font INSTANCE;
	
	public Font() {
		super("Font", Category.CLIENT, "Allows you to customize the client font.");
		setDrawn(false);
		setExempt(true);
		INSTANCE = this;
	}

	public static Setting<Boolean> vanilla = new Setting<>("Vanilla", "Overrides the minecraft vanilla font", false);
}
