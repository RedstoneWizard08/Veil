package foundry.veil.impl.glsl.node.variable;

import foundry.veil.impl.glsl.grammar.GlslSpecifiedType;
import foundry.veil.impl.glsl.node.GlslNode;

import java.util.stream.Stream;

public class GlslStructNode implements GlslNode {

    private GlslSpecifiedType specifiedType;

    public GlslStructNode(GlslSpecifiedType specifiedType) {
        this.specifiedType = specifiedType;
    }

    @Override
    public String getSourceString() {
        if (this.specifiedType.getQualifiers().isEmpty()) {
            return "struct " + this.specifiedType.getSpecifier().getSourceString();
        }
        return this.specifiedType.getSourceString();
    }

    @Override
    public Stream<GlslNode> stream() {
        return Stream.of(this);
    }
}
