package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.PointLight;
import foundry.veil.api.client.render.light.renderer.InstancedLightRenderer;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import foundry.veil.api.client.render.light.renderer.LightTypeRenderer;
import foundry.veil.api.client.render.shader.VeilShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class InstancedPointLightRenderer extends InstancedLightRenderer<PointLight> {

    public InstancedPointLightRenderer() {
        super(Float.BYTES * 7);
    }

    @Override
    protected MeshData createMesh() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        LightTypeRenderer.createInvertedCube(bufferBuilder);
        return bufferBuilder.buildOrThrow();
    }

    @Override
    protected void setupBufferState() {
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 6);

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_POINT);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
    }

    @Override
    protected boolean isVisible(PointLight light, CullFrustum frustum) {
        return frustum.testSphere(light.getPosition(), light.getRadius() * 1.4F);
    }
}