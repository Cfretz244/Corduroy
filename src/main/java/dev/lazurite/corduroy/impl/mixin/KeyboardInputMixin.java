package dev.lazurite.corduroy.impl.mixin;

import dev.lazurite.corduroy.api.ViewStack;
import dev.lazurite.corduroy.api.View;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

    /**
     * Prevents keyboard controls from being used.
     * 1.21.2: the client class is ClientInput; key states live in the immutable
     * keyPresses {@link Input} record.
     * @see View#shouldPlayerControl
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick$HEAD(CallbackInfo ci) {
        ViewStack.getInstance().peek().filter(view -> !view.shouldPlayerControl()).ifPresent(view -> {
            this.forwardImpulse = 0.0f;
            this.leftImpulse = 0.0f;
            this.keyPresses = Input.EMPTY;
            ci.cancel();
        });
    }

}
