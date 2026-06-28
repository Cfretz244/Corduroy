package dev.lazurite.corduroy.impl;

import dev.lazurite.corduroy.api.View;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorduroyCamera extends Camera {

    // TEMP DIAGNOSTIC: remove once the FPV black-screen render bug is solved.
    private static final Logger DEBUG_LOG = LoggerFactory.getLogger("CorduroyCamera-DEBUG");
    private static int debugFrame = 0;

    private final Camera parentCamera;

    public CorduroyCamera(Camera parentCamera) {
        this.parentCamera = parentCamera;
        this.entity = parentCamera.entity; // 26.1: getEntity() removed; field is access-widened
        this.level = parentCamera.level;   // 26.1: getCameraEntityPartialTicks() derefs this.level
                                           // (tickRateManager()); vanilla sets it via setLevel(), which
                                           // never runs on this wrapper, so copy it from the parent.
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
     * (conjugated) — so storing the View's quaternion with the exact vanilla conventions is all
     * that's needed for FPV orientation, including roll. Vanilla's basis: forwards = q*(0,0,-1),
     * up = q*(0,1,0), left = q*(-1,0,0); the xRot/yRot floats only feed the sound listener and
     * other non-matrix consumers, derived here from the look vector.
     *
     * 26.1: Camera.setup() became update(DeltaTracker) -> alignWithEntity(partialTicks) (made
     * overridable via access widener); the rest of update (fov/frustum/perspective) runs in super.
     */
    @Override
    public void alignWithEntity(float partialTicks) {
        var view = this.getView();
        this.position = view.getPosition(partialTicks);
        this.blockPosition.set(this.position.x, this.position.y, this.position.z);
        this.rotation.set(view.getRotation(partialTicks));
        this.forwards.set(0.0F, 0.0F, -1.0F);
        this.forwards.rotate(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.rotate(this.rotation);
        this.left.set(-1.0F, 0.0F, 0.0F);
        this.left.rotate(this.rotation);
        this.yRot = (float) Math.toDegrees(Math.atan2(-this.forwards.x(), this.forwards.z()));
        this.xRot = (float) Math.toDegrees(-Math.asin(Math.max(-1.0f, Math.min(1.0f, this.forwards.y()))));

        view.onRender();

        // TEMP DIAGNOSTIC: log camera state ~once/sec while a Corduroy view is active, to find
        // the FPV black-screen cause (degenerate position/rotation, uninitialized camera, etc.).
        if (debugFrame++ % 40 == 0) {
            final var mc = Minecraft.getInstance();
            final var ppos = mc.player == null ? null : mc.player.position();
            DEBUG_LOG.info(
                "view={} firstPerson={} initialized={} levelNull={} entity={} camEntity={} | pos=({}, {}, {}) | rot=({}, {}, {}, {}) | fwd=({}, {}, {}) up=({}, {}, {}) | xRot={} yRot={} | playerPos={}",
                view.getClass().getSimpleName(),
                mc.options.getCameraType().isFirstPerson(),
                this.isInitialized(),
                this.level == null,
                this.entity == null ? "null" : this.entity.getClass().getSimpleName(),
                mc.getCameraEntity() == null ? "null" : mc.getCameraEntity().getClass().getSimpleName(),
                this.position.x, this.position.y, this.position.z,
                this.rotation.x, this.rotation.y, this.rotation.z, this.rotation.w,
                this.forwards.x(), this.forwards.y(), this.forwards.z(),
                this.up.x(), this.up.y(), this.up.z(),
                this.xRot, this.yRot,
                ppos
            );
        }
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
