package khanhtypo.librarycmd.commands.paramaters;

import java.util.Objects;

public final class CommandParam {
    private String prefix;
    private final String name;
    private final String description;
    private final boolean required;

    public CommandParam(String name, String description, boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.prefix = null;
    }

    @Override
    public String toString() {
        return  (required ? '<' : "[<") + (this.prefix == null ? "" : this.prefix + ' ') + name + (required ? '>' : ">]") + " - " + description;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean required() {
        return required;
    }

    public CommandParam setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommandParam) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                this.required == that.required;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, required);
    }

}
