package com.ryangar46.namecommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;

import java.util.Collection;
import java.util.List;

public class NameCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("name")
                .requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("name", MessageArgument.message())
                                .executes((context) -> {
                                    return nameItem(context.getSource(), EntityArgument.getEntities(context, "targets"), MessageArgument.getMessage(context, "name").getString()); }))));
    }

    private static int nameItem(CommandSource source, Collection<? extends Entity> target, String name) throws CommandSyntaxException {
        int i = 0;

        for (Entity entity : target) {
            // Checks if they are a Living Entity
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                ItemStack itemstack = livingentity.getMainHandItem();
                // Checks if slot is empty
                if (!itemstack.isEmpty()) {
                    i++;
                    itemstack.setHoverName(ITextComponent.nullToEmpty(name));
                }
            }
        }

        if (i > 0) {
            source.sendSuccess(new TranslationTextComponent("command.name.success", i, name), true);
        }
        else {
            throw new SimpleCommandExceptionType(new TranslationTextComponent("command.name.fail")).create();
        }

        return i;
    }
}
