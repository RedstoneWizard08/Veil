package foundry.veil.api.client.render.shader.processor;

import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Modifies the source code of a shader before compilation.
 *
 * @author Ocelot
 */
public interface ShaderPreProcessor {

    ShaderPreProcessor NOOP = Context::sourceCode;
    Pattern UNIFORM_PATTERN = Pattern.compile(".*uniform\\s+(?<type>\\w+)\\W(?<name>\\w*)");

    /**
     * Called once when a shader is first run through the pre-processor.
     */
    default void prepare() {
    }

    /**
     * Modifies the specified shader source input.
     *
     * @param context Context for modifying shaders
     * @return The modified source or the input if nothing changed
     * @throws IOException If any error occurs while editing the source
     */
    String modify(Context context) throws IOException;

    static ShaderPreProcessor allOf(ShaderPreProcessor... processors) {
        return allOf(Arrays.asList(processors));
    }

    static ShaderPreProcessor allOf(Collection<ShaderPreProcessor> processors) {
        List<ShaderPreProcessor> list = new ArrayList<>(processors.size());
        for (ShaderPreProcessor processor : processors) {
            if (processor instanceof ShaderMultiProcessor multiProcessor) {
                list.addAll(Arrays.asList(multiProcessor.processors()));
            } else if (processor != NOOP) {
                list.add(processor);
            }
        }

        if (list.isEmpty()) {
            return NOOP;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return new ShaderMultiProcessor(list.toArray(new ShaderPreProcessor[0]));
    }

    /**
     * Context for modifying source code and shader behavior.
     */
    interface Context {

        /**
         * Runs the specified source through the entire processing list.
         *
         * @param name   The name of the shader file to modify or <code>null</code> if the source is a raw string
         * @param source The shader source code to modify
         * @return The modified source
         * @throws IOException If any error occurs while editing the source
         */
        String modify(@Nullable ResourceLocation name, String source) throws IOException;

        /**
         * Sets the uniform binding for a shader.
         *
         * @param name    The name of the uniform
         * @param binding The binding to set it to
         */
        void addUniformBinding(String name, int binding);

        /**
         * Marks this shader as dependent on the specified pre-definition.
         * When definitions change, only shaders marked as dependent on that definition will be recompiled.
         *
         * @param name The name of the definition to depend on
         */
        void addDefinitionDependency(String name);

        /**
         * @param name The name of the definition to depend on
         */
        void addInclude(ResourceLocation name);

        /**
         * @return The id of the shader being compiled or <code>null</code> if the shader is compiled from a raw string
         */
        @Nullable
        ResourceLocation name();

        /**
         * @return The input source code. This is GLSL
         */
        String sourceCode();

        /**
         * @return The OpenGL type of the shader being compiled
         */
        int type();

        /**
         * @return The file to id converter for the loading shader file type
         */
        FileToIdConverter idConverter();

        /**
         * @return The readable name of the loading shader file type
         */
        default String getTypeName() {
            return ShaderManager.getTypeName(this.type());
        }

        /**
         * @return Whether the processor is being run for a source file and not a #include file
         */
        boolean isSourceFile();

        /**
         * @return The definition of the program this is being compiled for or <code>null</code> if the shader is standalone
         */
        @Nullable
        ProgramDefinition definitions();

        /**
         * @return The set of pre-definitions for shaders
         */
        ShaderPreDefinitions preDefinitions();

        Context withSource(@Nullable ResourceLocation name, String source);
    }
}
