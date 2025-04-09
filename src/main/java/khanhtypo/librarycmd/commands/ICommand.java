package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.Library;

public interface ICommand {
    String getName();

    String getDescription();

    /**
     * Called when the command is executed.
     *
     * @return false if the command should trigger exit program. e.g. a fatal error/crash occurred while running, or executing exit command
     */
    boolean onCalled(String[] parameters);

    CommandPermission getPermission();

    default DescriptiveParameters getParameters() {
        return DescriptiveParameters.NONE;
    }

    default void printCommandInfo() {
        Library.printf("%s : %s\n", getName(), getDescription());
        getParameters().printAllParameters();
        Library.println("");
    }
}
