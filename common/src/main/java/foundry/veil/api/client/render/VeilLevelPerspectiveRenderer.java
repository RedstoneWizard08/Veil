package foundry.veil.api.client.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.ext.RenderTargetExtension;
import foundry.veil.impl.client.render.LevelPerspectiveCamera;
import foundry.veil.mixin.accessor.GameRendererAccessor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

/**
 * Renders the level from different perspectives.
 *
 * @author Ocelot
 */
public final class VeilLevelPerspectiveRenderer {

    private static final LevelPerspectiveCamera CAMERA = new LevelPerspectiveCamera();
    private static final Matrix4f TRANSFORM = new Matrix4f();

    private static final Matrix4f BACKUP_PROJECTION = new Matrix4f();
    private static final Vector3f BACKUP_LIGHT0_POSITION = new Vector3f();
    private static final Vector3f BACKUP_LIGHT1_POSITION = new Vector3f();

    private static boolean renderingPerspective = false;

    private VeilLevelPerspectiveRenderer() {
    }

    /**
     * Renders the level from another POV. Automatically prevents circular render references.
     *
     * @param framebuffer       The framebuffer to draw into
     * @param modelView         The base modelview matrix
     * @param projection        The projection matrix
     * @param cameraPosition    The position of the camera
     * @param cameraOrientation The orientation of the camera
     * @param renderDistance    The chunk render distance
     * @param deltaTracker      The delta tracker instance
     */
    public static void render(AdvancedFbo framebuffer, Matrix4fc modelView, Matrix4fc projection, Vector3dc cameraPosition, Quaternionfc cameraOrientation, float renderDistance, DeltaTracker deltaTracker) {
        render(framebuffer, Minecraft.getInstance().cameraEntity, modelView, projection, cameraPosition, cameraOrientation, renderDistance, deltaTracker);
    }

    /**
     * Renders the level from another POV. Automatically prevents circular render references.
     *
     * @param framebuffer       The framebuffer to draw into
     * @param cameraEntity      The entity to draw the camera in relation to. If unsure use {@link #render(AdvancedFbo, Matrix4fc, Matrix4fc, Vector3dc, Quaternionfc, float, DeltaTracker)}
     * @param modelView         The base modelview matrix
     * @param projection        The projection matrix
     * @param cameraPosition    The position of the camera
     * @param cameraOrientation The orientation of the camera
     * @param renderDistance    The chunk render distance
     * @param deltaTracker      The delta tracker instance
     */
    public static void render(AdvancedFbo framebuffer, @Nullable Entity cameraEntity, Matrix4fc modelView, Matrix4fc projection, Vector3dc cameraPosition, Quaternionfc cameraOrientation, float renderDistance, DeltaTracker deltaTracker) {
        if (renderingPerspective) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer gameRenderer = minecraft.gameRenderer;
        LevelRenderer levelRenderer = minecraft.levelRenderer;
        Window window = minecraft.getWindow();
        GameRendererAccessor accessor = (GameRendererAccessor) gameRenderer;
        RenderTargetExtension renderTargetExtension = (RenderTargetExtension) minecraft.getMainRenderTarget();

        CAMERA.setup(cameraPosition, cameraEntity, minecraft.level, cameraOrientation);

        PoseStack poseStack = new PoseStack();

        poseStack.mulPose(TRANSFORM.set(modelView));
        poseStack.mulPose(CAMERA.rotation());

        float backupRenderDistance = gameRenderer.getRenderDistance();
        accessor.setRenderDistance(renderDistance);

        int backupWidth = window.getWidth();
        int backupHeight = window.getHeight();
        window.setWidth(framebuffer.getWidth());
        window.setHeight(framebuffer.getHeight());

        BACKUP_PROJECTION.set(RenderSystem.getProjectionMatrix());
        gameRenderer.resetProjectionMatrix(TRANSFORM.set(projection));
        BACKUP_LIGHT0_POSITION.set(VeilRenderSystem.getLight0Position());
        BACKUP_LIGHT1_POSITION.set(VeilRenderSystem.getLight1Position());

        HitResult backupHitResult = minecraft.hitResult;
        Entity backupCrosshairPickEntity = minecraft.crosshairPickEntity;

        renderingPerspective = true;
        framebuffer.bindDraw(true);
        renderTargetExtension.veil$setWrapper(framebuffer);
        levelRenderer.prepareCullFrustum(new Vec3(cameraPosition.x(), cameraPosition.y(), cameraPosition.z()), TRANSFORM, poseStack.last().pose());
        levelRenderer.renderLevel(deltaTracker, false, CAMERA, gameRenderer, gameRenderer.lightTexture(), poseStack.last().pose(), TRANSFORM);
        levelRenderer.doEntityOutline();
        renderTargetExtension.veil$setWrapper(null);
        AdvancedFbo.unbind();
        renderingPerspective = false;

        minecraft.crosshairPickEntity = backupCrosshairPickEntity;
        minecraft.hitResult = backupHitResult;

        RenderSystem.setShaderLights(BACKUP_LIGHT0_POSITION, BACKUP_LIGHT1_POSITION);
        gameRenderer.resetProjectionMatrix(BACKUP_PROJECTION);

        window.setWidth(backupWidth);
        window.setHeight(backupHeight);

        accessor.setRenderDistance(backupRenderDistance);
    }

    /**
     * @return Whether a perspective is being rendered
     */
    public static boolean isRenderingPerspective() {
        return renderingPerspective;
    }
}
