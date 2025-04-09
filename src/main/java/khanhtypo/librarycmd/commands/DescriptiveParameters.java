package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.commands.paramaters.CommandParam;
import khanhtypo.librarycmd.Library;

import java.util.ArrayList;
import java.util.List;

public class DescriptiveParameters {
    public static final DescriptiveParameters NONE = new DescriptiveParameters(List.of());
    private final List<CommandParam> allParameters;

    private DescriptiveParameters(List<CommandParam> allParameters) {
        this.allParameters = allParameters;
    }

    public void printAllParameters() {
        if (this != NONE) {
            for (CommandParam param : this.allParameters) {
                Library.println("\t+ " + param.toString());
            }
        }
    }
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<CommandParam> allParameters = new ArrayList<>();

        public Builder add(CommandParam param) {
            this.allParameters.add(param);
            return this;
        }

        public DescriptiveParameters build() {
            if (this.allParameters.isEmpty()) return NONE;
            return new DescriptiveParameters(List.copyOf(this.allParameters));
        }
    }
}
