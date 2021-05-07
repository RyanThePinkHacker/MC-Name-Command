package com.ryangar46.itemcommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public class ItemCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((p_208839_0_) -> {
        return new TranslationTextComponent("commands.enchant.failed.entity", p_208839_0_);
    });
    private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType((p_208835_0_) -> {
        return new TranslationTextComponent("commands.enchant.failed.itemless", p_208835_0_);
    });
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType((p_208837_0_) -> {
        return new TranslationTextComponent("commands.enchant.failed.incompatible", p_208837_0_);
    });
    private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((p_208840_0_, p_208840_1_) -> {
        return new TranslationTextComponent("commands.enchant.failed.level", p_208840_0_, p_208840_1_);
    });
    private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.enchant.failed"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> itemCommand
                = Commands.literal("item")
                .requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.literal("enchant")
                    .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("enchantment", EnchantmentArgument.enchantment())
                            .executes((args) -> {
                                return enchant(args.getSource(), EntityArgument.getEntities(args, "targets"), EnchantmentArgument.getEnchantment(args, "enchantment"), 1);
                            })
                            .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes((args) -> {
                                    return enchant(args.getSource(), EntityArgument.getEntities(args, "targets"), EnchantmentArgument.getEnchantment(args, "enchantment"), IntegerArgumentType.getInteger(args, "level"));
                                })
                            )
                        )
                    )
                )
                .then(Commands.literal("give")
                    .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("item", ItemArgument.item())
                            .executes((args) -> {
                                return giveItem(args.getSource(), ItemArgument.getItem(args, "item"), EntityArgument.getPlayers(args, "targets"), 1);
                            })
                            .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .executes((args) -> {
                                    return giveItem(args.getSource(), ItemArgument.getItem(args, "item"), EntityArgument.getPlayers(args, "targets"), IntegerArgumentType.getInteger(args, "count"));
                                })
                            )
                        )
                    )
                )
                .then(Commands.literal("name")
                    .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("name", MessageArgument.message())
                            .executes((args) -> {
                                return nameItem(args.getSource(), EntityArgument.getEntities(args, "targets"), MessageArgument.getMessage(args, "name").getString());
                            })
                        )
                    )
                );

        dispatcher.register(itemCommand);
    }

    private static int nameItem(CommandSource source, Collection<? extends Entity> target, String name) throws CommandSyntaxException {
        int i = 0;

        for(Entity entity : target) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                ItemStack itemstack = livingentity.getMainHandItem();
                if (!itemstack.isEmpty()) {
                    itemstack.setHoverName(ITextComponent.nullToEmpty(name));
                }
            }
        }

        return i;
    }

    private static int enchant(CommandSource source, Collection<? extends Entity> target, Enchantment enchantment, int level) throws CommandSyntaxException {
        if (level > enchantment.getMaxLevel()) {
            throw ERROR_LEVEL_TOO_HIGH.create(level, enchantment.getMaxLevel());
        } else {
            int i = 0;

            for(Entity entity : target) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingentity = (LivingEntity)entity;
                    ItemStack itemstack = livingentity.getMainHandItem();
                    if (!itemstack.isEmpty()) {
                        if (enchantment.canEnchant(itemstack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemstack).keySet(), enchantment)) {
                            itemstack.enchant(enchantment, level);
                            ++i;
                        } else if (target.size() == 1) {
                            throw ERROR_INCOMPATIBLE.create(itemstack.getItem().getName(itemstack).getString());
                        }
                    } else if (target.size() == 1) {
                        throw ERROR_NO_ITEM.create(livingentity.getName().getString());
                    }
                } else if (target.size() == 1) {
                    throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
                }
            }

            if (i == 0) {
                throw ERROR_NOTHING_HAPPENED.create();
            } else {
                if (target.size() == 1) {
                    source.sendSuccess(new TranslationTextComponent("commands.enchant.success.single", enchantment.getFullname(level), target.iterator().next().getDisplayName()), true);
                } else {
                    source.sendSuccess(new TranslationTextComponent("commands.enchant.success.multiple", enchantment.getFullname(level), target.size()), true);
                }

                return i;
            }
        }
    }

    private static int giveItem(CommandSource source, ItemInput item, Collection<ServerPlayerEntity> target, int count) throws CommandSyntaxException {
        for(ServerPlayerEntity serverplayerentity : target) {
            int i = count;

            while(i > 0) {
                int j = Math.min(item.getItem().getMaxStackSize(), i);
                i -= j;
                ItemStack itemstack = item.createItemStack(j, false);
                boolean flag = serverplayerentity.inventory.add(itemstack);
                if (flag && itemstack.isEmpty()) {
                    itemstack.setCount(1);
                    ItemEntity itementity1 = serverplayerentity.drop(itemstack, false);
                    if (itementity1 != null) {
                        itementity1.makeFakeItem();
                    }

                    serverplayerentity.level.playSound((PlayerEntity)null, serverplayerentity.getX(), serverplayerentity.getY(), serverplayerentity.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverplayerentity.getRandom().nextFloat() - serverplayerentity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    serverplayerentity.inventoryMenu.broadcastChanges();
                } else {
                    ItemEntity itementity = serverplayerentity.drop(itemstack, false);
                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setOwner(serverplayerentity.getUUID());
                    }
                }
            }
        }

        if (target.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.give.success.single", count, item.createItemStack(count, false).getDisplayName(), target.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.give.success.single", count, item.createItemStack(count, false).getDisplayName(), target.size()), true);
        }

        return target.size();
    }
}
