package foundry.veil.api.quasar.data.module.init;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;

public record BlockParticleModuleData(boolean dynamic) implements ParticleModuleData {

    public static final Codec<BlockParticleModuleData> CODEC = Codec.BOOL.optionalFieldOf("dynamic", true).xmap(BlockParticleModuleData::new, BlockParticleModuleData::dynamic).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((InitParticleModule) particle -> {
            BlockState state = particle.getBlockStateInOrUnder();
            if (!state.isAir()) {
                particle.getRenderData().setAtlasSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
            }
        });
        if (this.dynamic) {
            builder.addModule((UpdateParticleModule) particle -> {
                BlockState state = particle.getBlockStateInOrUnder();
                if (!state.isAir()) {
                    particle.getRenderData().setAtlasSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
                }
            });
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.BLOCK_PARTICLE;
    }
}
