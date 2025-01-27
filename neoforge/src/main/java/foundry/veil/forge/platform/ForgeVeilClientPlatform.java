package foundry.veil.forge.platform;

import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.forge.event.ForgeVeilPostProcessingEvent;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ForgeVeilClientPlatform implements VeilClientPlatform {

    @Override
    public void preVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Pre(name, pipeline, context));
    }

    @Override
    public void postVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Post(name, pipeline, context));
    }
}
