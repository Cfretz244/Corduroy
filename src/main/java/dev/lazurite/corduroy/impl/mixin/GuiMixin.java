package dev.lazurite.corduroy.impl.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
     * 1.21: {@code renderHotbar(float, GuiGraphicsExtractor)} is now {@code renderHotbarAndDecorations(GuiGraphicsExtractor, DeltaTracker)}.
     * @see View#shouldRenderHud
     */
    // 26.1: HUD elements are extraction-based (extract* methods take a GuiGraphicsExtractor).
    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    protected void renderHotbar$HEAD(GuiGraphicsExtractor matrices, DeltaTracker deltaTracker, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHud()).ifPresent(view -> ci.cancel());
    }

    /**
     * Cancels crosshair rendering.
     * 1.21: {@code renderCrosshair} gained a {@code DeltaTracker} parameter.
     * @see View#shouldRenderHud
     */
    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair$HEAD(GuiGraphicsExtractor matrices, DeltaTracker deltaTracker, CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldRenderHud()).ifPresent(view -> ci.cancel());
    }

}
