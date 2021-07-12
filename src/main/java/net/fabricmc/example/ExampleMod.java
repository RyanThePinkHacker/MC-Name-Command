package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import ryangar46.namecommand.commands.NameCommand;

public class ExampleMod implements ModInitializer {
	@Override
	public void onInitialize() {
		// Fabric will only use ExampleMod and not Main. I don't know how to fix this, but this works.
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			NameCommand.register(dispatcher);
		});
	}
}
