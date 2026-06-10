package dev.lazurite.corduroy.impl.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import dev.lazurite.corduroy.api.ViewStack;
import dev.lazurite.corduroy.api.View;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Cancels player hand rendering.
     * 26.1: renderItemInHand(CameraRenderState, float, Matrix4fc).
     * @see View#shouldRenderHand
     */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    public void renderHand$HEAD(CameraRenderState cameraRenderState, float tickDelta, Matrix4fc matrix4f, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHand()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels view bobbing rendering during movement.
     * @see View#shouldBobView
     */
    // 26.1: bobView/bobHurt take (CameraRenderState, PoseStack).
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void bobView$HEAD(CameraRenderState cameraRenderState, PoseStack stack, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldBobView()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels view bobbing rendering during damage.
     * @see View#shouldBobView
     */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void bobHurt$HEAD(CameraRenderState cameraRenderState, PoseStack stack, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldBobView()).ifPresent(view -> ci.cancel());
    }

    // The pre-1.21 PoseStack.mulPose camera-orientation redirects are gone for good: since 1.21
    // the view matrix is derived from camera.rotation(), which CorduroyCamera.setup() now sets
    // with vanilla conventions (orientation incl. roll comes entirely from the View quaternion).

}
