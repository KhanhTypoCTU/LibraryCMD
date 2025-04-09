package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.commands.paramaters.CommandParam;

public class HelpCommand implements ICommand {

    private final DescriptiveParameters parameters;

    HelpCommand() {
        this.parameters = DescriptiveParameters.builder()
                .add(new CommandParam("command", "Specific command to learn more.", false))
                .build();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "List all available commands.";
    }

    @Override
    public DescriptiveParameters getParameters() {
        return this.parameters;
    }

    @Override
    public boolean onCalled(String[] parameters) {
        int params = parameters.length;
        if (params <= 1) {
            if (params == 0) {
                CommandInputHelper.getAllCommands().forEach(ICommand::printCommandInfo);
            } else {
                CommandInputHelper.getCommand(parameters[0]).printCommandInfo();
            }
        }
        return true;
    }

    @Override
    public CommandPermission getPermission() {
        return CommandPermission.EVERYONE;
    }
}
