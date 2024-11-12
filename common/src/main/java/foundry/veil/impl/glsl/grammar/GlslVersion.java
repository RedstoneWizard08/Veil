package foundry.veil.impl.glsl.grammar;

/**
 * Represents the version statement for a GLSL shader source.
 *
 * @author Ocelot
 */
public class GlslVersion {

    private int version;
    private boolean core;

    public GlslVersion(int version, boolean core) {
        this.version = version;
        this.core = core;
    }

    /**
     * @return The GLSL version number
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * @return Whether to use the core or compatibility profile
     */
    public boolean isCore() {
        return this.core;
    }

    public String getVersionStatement() {
        return this.version + (this.core ? " core" : "");
    }

    /**
     * Sets the GLSL version integer.
     *
     * @param version The new version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Sets whether to use the core or compatibility profile.
     *
     * @param core Whether to use "core"
     */
    public void setCore(boolean core) {
        this.core = core;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GlslVersion that = (GlslVersion) o;
        return version == that.version && core == that.core;
    }

    @Override
    public int hashCode() {
        return 31 * this.version + Boolean.hashCode(this.core);
    }

    @Override
    public String toString() {
        return "GlslVersion{" + this.getVersionStatement() + '}';
    }
}