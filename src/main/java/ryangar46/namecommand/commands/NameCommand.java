package ryangar46.namecommand.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

public class NameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("name")
                .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                        .then(CommandManager.argument("name", MessageArgumentType.message())
                                .executes((p_198493_0_) -> {
                                    return nameItem(EntityArgumentType.getEntities(p_198493_0_, "targets"), MessageArgumentType.getMessage(p_198493_0_, "name").getString()); }))));
    }

    private static int nameItem(Collection<? extends Entity> target, String name) {
        int i = 0;

        for(Entity entity : target) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                ItemStack itemstack = livingentity.getMainHandStack();
                if (!itemstack.isEmpty()) {
                    itemstack.setCustomName(Text.of(name));
                    i = 1;
                }
            }
        }

        return i;
    }
}
