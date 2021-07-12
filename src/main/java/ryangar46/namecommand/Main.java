package ryangar46.namecommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import ryangar46.namecommand.commands.NameCommand;

public class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			NameCommand.register(dispatcher);
		});
	}
}
