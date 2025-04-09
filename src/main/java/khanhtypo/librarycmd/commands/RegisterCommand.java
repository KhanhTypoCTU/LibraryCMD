package khanhtypo.librarycmd.commands;

public class RegisterCommand extends AddNewUserCommand {
    @Override
    public String getName() {
        return "register";
    }

    @Override
    public String getDescription() {
        return "Register a new user as a reader.";
    }

    @Override
    protected void appendParameters(DescriptiveParameters.Builder builder) {}

    @Override
    protected int maxParameterLength() {
        return 2;
    }

    @Override
    public CommandPermission getPermission() {
        return CommandPermission.EVERYONE;
    }
}
