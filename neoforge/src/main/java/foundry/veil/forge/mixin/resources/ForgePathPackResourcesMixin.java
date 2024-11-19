package foundry.veil.forge.mixin.resources;

import foundry.veil.Veil;
import foundry.veil.ext.PackResourcesExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Mixin(PathPackResources.class)
public abstract class ForgePathPackResourcesMixin implements PackResources, PackResourcesExtension {

    @Shadow
    public abstract @Nullable IoSupplier<InputStream> getRootResource(String... paths);

    @Shadow
    public abstract Path getSource();

    @Override
    public void veil$listResources(PackResourceConsumer consumer) {
        String packId = this.packId();
        Path root = this.getSource();
        String separator = root.getFileSystem().getSeparator();

        for (PackType type : PackType.values()) {
            Path assetPath = root.resolve(type.getDirectory());
            if (!Files.exists(assetPath)) {
                continue;
            }

            try {
                Files.walkFileTree(assetPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String[] parts = assetPath.relativize(file).toString().replace(separator, "/").split("/", 2);
                        String namespace = parts.length == 1 ? "root" : parts[0];
                        String path = parts.length == 1 ? parts[0] : parts[1];
                        ResourceLocation name = ResourceLocation.tryBuild(namespace, path);

                        if (name != null) {
                            consumer.accept(type, name, assetPath, file, PackResourcesExtension.findDevPath(root, file));
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Veil.LOGGER.warn("Failed to list resources in {} failed!", packId, e);
            }
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> veil$getIcon() {
        return this.getRootResource("pack.png");
    }
}
