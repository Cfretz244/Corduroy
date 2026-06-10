package dev.lazurite.corduroy.impl.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import dev.lazurite.corduroy.api.ViewStack;
import dev.lazurite.corduroy.api.View;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    /**
     * Cancels hotbar (and decorations) rendering.
     * 1.21: {@code renderHotbar(float, GuiGraphics)} is now {@code renderHotbarAndDecorations(GuiGraphics, DeltaTracker)}.
     * @see View#shouldRenderHud
     */
    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    protected void renderHotbar$HEAD(GuiGraphics matrices, DeltaTracker deltaTracker, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHud()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels crosshair rendering.
     * 1.21: {@code renderCrosshair} gained a {@code DeltaTracker} parameter.
     * @see View#shouldRenderHud
     */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair$HEAD(GuiGraphics matrices, DeltaTracker deltaTracker, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHud()).ifPresent(view -> ci.cancel());
    }

}
