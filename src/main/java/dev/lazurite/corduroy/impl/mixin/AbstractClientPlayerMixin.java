package dev.lazurite.corduroy.impl.mixin;

import dev.lazurite.corduroy.api.View;
import dev.lazurite.corduroy.api.ViewStack;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Freezes the FOV modifier at 1.0 while a view that suppresses player control or movement-FOV is
 * active (replaces the pre-26.1 GameRenderer.tickFov hooks).
 *
 * @see View#shouldFOVChangeOnMovement
 * @see View#shouldPlayerControl
 */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void getFieldOfViewModifier$HEAD(boolean firstPerson, float fovEffectScale, CallbackInfoReturnable<Float> cir) {
        ViewStack.getInstance().peek()
                .filter(view -> !view.shouldFOVChangeOnMovement() || !view.shouldPlayerControl())
                .ifPresent(view -> cir.setReturnValue(1.0f));
    }
}
