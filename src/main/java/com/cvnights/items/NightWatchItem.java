package com.cvnights.items;

import com.cvnights.NightState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class NightWatchItem extends Item {

    public NightWatchItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack, true);
        }

        Optional<Identifier> tonight = NightState.currentSpecialNight();
        Text tonightText = tonight.map(NightWatchItem::nightDisplayName)
                .orElse(Text.translatable("cvnights.night_watch.normal"));
        user.sendMessage(Text.translatable("cvnights.night_watch.tonight", tonightText), false);

        NightState.nextSpecialNight().ifPresentOrElse(next -> {
            Text nextText = nightDisplayName(next.nightId());
            user.sendMessage(Text.translatable("cvnights.night_watch.tomorrow", nextText, next.daysUntil()), false);
        }, () -> user.sendMessage(Text.translatable("cvnights.night_watch.no_forecast"), false));

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.5f, 1.2f);

        return TypedActionResult.success(stack, false);
    }

    private static Text nightDisplayName(Identifier nightId) {
        // Les clés de traduction "enhancedcelestials.name.<nom>" sont les mêmes que celles
        // déclarées dans data/cvnights/enhancedcelestials/lunar/event/*.json (text_components.name.key)
        return Text.translatable("enhancedcelestials.name." + nightId.getPath());
    }
}
