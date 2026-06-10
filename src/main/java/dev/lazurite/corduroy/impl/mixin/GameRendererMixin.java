package dev.lazurite.corduroy.impl.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.corduroy.api.ViewStack;
import dev.lazurite.corduroy.api.View;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Cancels player hand rendering.
     * 1.21.5+: renderItemInHand(float, boolean, Matrix4f).
     * @see View#shouldRenderHand
     */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    public void renderHand$HEAD(float tickDelta, boolean bl, Matrix4f matrix4f, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHand()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels view bobbing rendering during movement.
     * @see View#shouldBobView
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void bobView$HEAD(PoseStack stack, float f, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldBobView()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels view bobbing rendering during damage.
     * @see View#shouldBobView
     */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void bobHurt$HEAD(PoseStack stack, float f, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldBobView()).ifPresent(view -> ci.cancel());
    }

    /**
     * @see View#shouldPlayerControl
     */
    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void tickFov$HEAD(CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldPlayerControl()).ifPresent(view -> ci.cancel());
    }

    /**
     * 1.21.2: getFieldOfViewModifier gained (firstPerson, fovEffectScale) params.
     * @see View#shouldFOVChangeOnMovement
     */
    @Redirect(
            method = "tickFov",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getFieldOfViewModifier(ZF)F"
            )
    )
    private float tickFov$HEAD(AbstractClientPlayer player, boolean firstPerson, float fovEffectScale) {
        return ViewStack.getInstance().peek()
                .filter(view -> !view.shouldFOVChangeOnMovement())
                .map(view -> 1.0f)
                .orElse(player.getFieldOfViewModifier(firstPerson, fovEffectScale));
    }

    // The pre-1.21 PoseStack.mulPose camera-orientation redirects are gone for good: since 1.21
    // the view matrix is derived from camera.rotation(), which CorduroyCamera.setup() now sets
    // with vanilla conventions (orientation incl. roll comes entirely from the View quaternion).

}
