package com.cvnights.items;

import com.cvnights.XpBoostManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FocusCandyItem extends Item {

    public FocusCandyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            boolean applied = XpBoostManager.tryApplyCandy(serverPlayer);
            if (applied) {
                stack.decrement(1);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.4f);
                return TypedActionResult.success(stack, false);
            } else {
                // déjà actif ou en cooldown : le message a déjà été envoyé, on ne consomme pas l'objet
                return TypedActionResult.fail(stack);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}
