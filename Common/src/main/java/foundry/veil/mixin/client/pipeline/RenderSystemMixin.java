package foundry.veil.mixin.client.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.pipeline.VeilRenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;replayQueue()V", shift = At.Shift.BEFORE), remap = false)
    private static void veil$flipFrame(long window, CallbackInfo ci) {
        VeilRenderSystem.flipFrame(window);
    }

    @Inject(method = "setShader", at = @At("TAIL"), remap = false)
    private static void setShader(Supplier<ShaderInstance> shader, CallbackInfo ci) {
        VeilRenderSystem.shaderUpdate();
    }
}