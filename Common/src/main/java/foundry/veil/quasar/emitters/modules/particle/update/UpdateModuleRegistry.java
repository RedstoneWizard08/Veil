package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.Veil;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

public class UpdateModuleRegistry {

    private static final BiMap<String, ModuleType<?>> MODULES = HashBiMap.create();

    public static final Codec<ModuleType<?>> MODULE_MAP_CODEC = Codec.STRING.comapFlatMap(name -> {
        if(!MODULES.containsKey(name)) {
            return DataResult.error(() -> "Update module %s does not exist!".formatted(name));
        }
        return DataResult.success(MODULES.get(name));
    }, MODULES.inverse()::get);
    public static void register(String name, ModuleType<?> type) {
        MODULES.put(name, type);
    }

    public static void bootstrap() {}

    private static final BiMap<ResourceLocation, UpdateParticleModule> MODULES_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, UpdateParticleModule module) {
        MODULES_BY_ID.put(id, module);
    }

    public static UpdateParticleModule getModule(ResourceLocation id) {
        if(!MODULES_BY_ID.containsKey(id)) {
            Veil.LOGGER.error("Update module %s does not exist!".formatted(id));
            return null;
        } else return MODULES_BY_ID.get(id);
    }

    public static ResourceLocation getModuleId(UpdateParticleModule module) {
        return MODULES_BY_ID.inverse().get(module);
    }

    public static void clearRegisteredModules() {
        MODULES_BY_ID.clear();
    }
}
