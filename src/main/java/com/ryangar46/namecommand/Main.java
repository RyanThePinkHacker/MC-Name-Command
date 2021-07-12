package com.ryangar46.namecommand;

import com.ryangar46.namecommand.commands.NameCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			NameCommand.register(dispatcher);
		});
	}
}
