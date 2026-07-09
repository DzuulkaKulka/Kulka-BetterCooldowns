package xyz.kulka.mixin.client;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kulka.client.CooldownCache;
import xyz.kulka.client.KulkaBettercooldownsClient;

@Mixin(ItemCooldownManager.class)
public class MixinItemCooldownManager {

    @Inject(method = "set(Lnet/minecraft/item/ItemStack;I)V", at = @At("HEAD"))
    private void kulka_captureSet(ItemStack stack, int duration, CallbackInfo ci) {
        ItemCooldownManager self = (ItemCooldownManager) (Object) this;
        Identifier group = self.getGroup(stack);

        CooldownCache.set(group, duration);
        CooldownCache.setUsedName(group, KulkaBettercooldownsClient.resolveItemName(stack));
        CooldownCache.setUsedStack(group, stack.copy());
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void kulka_captureRemove(Identifier group, CallbackInfo ci) {
        CooldownCache.remove(group);
    }
}
