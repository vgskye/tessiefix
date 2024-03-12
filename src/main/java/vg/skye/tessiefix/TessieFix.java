package vg.skye.tessiefix;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TessieFix implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("tessiefix");

	@Override
	public void onInitialize() {
		LOGGER.info("TessieFix is here to... help? probably?");
	}
}