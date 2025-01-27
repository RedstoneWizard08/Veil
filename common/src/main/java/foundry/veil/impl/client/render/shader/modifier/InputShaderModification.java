package foundry.veil.impl.client.render.shader.modifier;

import foundry.veil.impl.client.render.shader.transformer.VeilJobParameters;
import foundry.veil.impl.glsl.GlslParser;
import foundry.veil.impl.glsl.GlslSyntaxException;
import foundry.veil.impl.glsl.node.GlslTree;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InputShaderModification implements ShaderModification {

    private final int priority;
    private final String input;

    public InputShaderModification(int priority, String input) {
        this.priority = priority;
        this.input = input;
    }

    @Override
    public void inject(GlslTree tree, VeilJobParameters parameters) throws GlslSyntaxException {
        tree.getBody().addAll(0, GlslParser.parse(this.input).getBody());
//        tree.parseAndInjectNodes(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, this.input.split("\n"));
    }

    @Override
    public int priority() {
        return this.priority;
    }
}
