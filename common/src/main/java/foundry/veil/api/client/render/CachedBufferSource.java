package foundry.veil.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CachedBufferSource implements MultiBufferSource, NativeResource {

    protected final Object2ObjectMap<RenderType, ByteBufferBuilder> buffers = new Object2ObjectArrayMap<>();
    protected final Map<RenderType, BufferBuilder> startedBuilders = new HashMap<>();
    @Nullable
    protected RenderType lastSharedType;

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        BufferBuilder last = this.startedBuilders.get(renderType);
        if (last != null && !renderType.canConsolidateConsecutiveGeometry()) {
            this.endBatch(renderType, last);
            last = null;
        }

        if (last != null) {
            return last;
        }

        ByteBufferBuilder bytebufferbuilder = this.buffers.computeIfAbsent(renderType, unused -> new ByteBufferBuilder(renderType.bufferSize()));
        BufferBuilder builder = new BufferBuilder(bytebufferbuilder, renderType.mode(), renderType.format());
        this.startedBuilders.put(renderType, builder);
        return builder;
    }

    @Override
    public void free() {
        this.buffers.values().forEach(ByteBufferBuilder::clear);
        this.buffers.clear();
        this.startedBuilders.clear();
        this.lastSharedType = null;
    }

    public void endLastBatch() {
        if (this.lastSharedType != null) {
            this.endBatch(this.lastSharedType);
            this.lastSharedType = null;
        }
    }

    public void endBatch() {
        this.endLastBatch();

        for (RenderType rendertype : this.buffers.keySet()) {
            this.endBatch(rendertype);
        }
    }

    public void endBatch(RenderType pRenderType) {
        BufferBuilder bufferbuilder = this.startedBuilders.remove(pRenderType);
        if (bufferbuilder != null) {
            this.endBatch(pRenderType, bufferbuilder);
        }
    }

    private void endBatch(RenderType renderType, BufferBuilder pBuilder) {
        MeshData meshdata = pBuilder.build();
        if (meshdata != null) {
            if (renderType.sortOnUpload()) {
                ByteBufferBuilder bytebufferbuilder = this.buffers.computeIfAbsent(renderType, unused -> new ByteBufferBuilder(renderType.bufferSize()));
                meshdata.sortQuads(bytebufferbuilder, RenderSystem.getVertexSorting());
            }

            renderType.draw(meshdata);
        }

        if (renderType.equals(this.lastSharedType)) {
            this.lastSharedType = null;
        }
    }
}
