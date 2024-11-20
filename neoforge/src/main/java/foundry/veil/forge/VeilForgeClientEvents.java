package foundry.veil.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.api.quasar.particle.ParticleSystemManager;
import foundry.veil.impl.client.imgui.VeilImGuiImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@ApiStatus.Internal
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        VeilClient.tickClient(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Pre event) {
        if (event.getLevel().isClientSide()) {
            VeilRenderSystem.renderer().getParticleManager().tick();
        }
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matches(event.getKey(), event.getScanCode())) {
            VeilImGuiImpl.get().toggle();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("quasar");
        builder.then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(QuasarParticles.emitterSuggestionProvider()).then(Commands.argument("position", Vec3Argument.vec3()).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "emitter");

            CommandSourceStack source = ctx.getSource();
            ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
            ParticleEmitter instance = particleManager.createEmitter(id);
            if (instance == null) {
                source.sendFailure(Component.literal("Unknown emitter: " + id));
                return 0;
            }

            WorldCoordinates coordinates = ctx.getArgument("position", WorldCoordinates.class);
            Vec3 pos = coordinates.getPosition(source);
            instance.setPosition(pos.x, pos.y, pos.z);
            particleManager.addParticleSystem(instance);
            source.sendSuccess(() -> Component.literal("Spawned " + id), true);
            return 1;
        })));
        event.getDispatcher().register(builder);
    }

    @SubscribeEvent
    public static void mousePressed(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matchesMouse(event.getButton())) {
            VeilImGuiImpl.get().toggle();
        }
    }

    @SubscribeEvent
    public static void leaveGame(ClientPlayerNetworkEvent.LoggingOut event) {
        VeilRenderSystem.renderer().getDeferredRenderer().reset();
    }
}
