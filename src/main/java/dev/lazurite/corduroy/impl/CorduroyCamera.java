package dev.lazurite.corduroy.impl;

import dev.lazurite.corduroy.api.View;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

public class CorduroyCamera extends Camera {

    private final Camera parentCamera;

    public CorduroyCamera(Camera parentCamera) {
        this.parentCamera = parentCamera;
        this.entity = parentCamera.getEntity();
    }

    public Camera getParent() {
        return this.parentCamera;
    }

    public View getView() {
        // we can do this because a CorduroyCamera will never be used if there isn't a view on the stack.
        return ViewStackImpl.INSTANCE.peek().get();
    }

    /**
     * Since 1.21, the world view matrix is derived directly from {@code camera.rotation()}
     * (conjugated) in GameRenderer — so storing the View's quaternion with the exact vanilla
     * conventions is all that's needed for FPV orientation, including roll. Vanilla's basis:
     * forwards = q*(0,0,-1), up = q*(0,1,0), left = q*(-1,0,0); the xRot/yRot floats only feed
     * the sound listener and other non-matrix consumers, derived here from the look vector.
     */
    @Override
    public void setup(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float f) {
        this.initialized = true;
        this.level = blockGetter;
        this.entity = entity;

        var view = this.getView();
        this.position = view.getPosition(f);
        this.blockPosition.set(this.position.x, this.position.y, this.position.z);
        this.rotation.set(view.getRotation(f));
        this.forwards.set(0.0F, 0.0F, -1.0F);
        this.forwards.rotate(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.rotate(this.rotation);
        this.left.set(-1.0F, 0.0F, 0.0F);
        this.left.rotate(this.rotation);
        this.yRot = (float) Math.toDegrees(Math.atan2(-this.forwards.x(), this.forwards.z()));
        this.xRot = (float) Math.toDegrees(-Math.asin(Math.max(-1.0f, Math.min(1.0f, this.forwards.y()))));
        this.partialTickTime = f;

        view.onRender();
    }

    @Override
    public void tick() {
        var view = this.getView();

        /* Interpolate the view's position and rotation */
        view.updatePrevious();

        /* Temporary View */
        if (view instanceof View.Temporary temporaryView) {
            temporaryView.age();

            if (temporaryView.getAge() > temporaryView.getDuration()) {
                temporaryView.onExit();
            }
        }

        /* Ticking View */
        if (view instanceof View.Ticking tickingView) {
            tickingView.tick();
        }
    }

    @Override
    public boolean isDetached() {
        return this.getView().shouldRenderTarget();
    }

}
