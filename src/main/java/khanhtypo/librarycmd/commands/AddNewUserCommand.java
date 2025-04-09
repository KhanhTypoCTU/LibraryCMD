package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.accounts.Account;
import khanhtypo.librarycmd.commands.paramaters.CommandParam;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.util.ModifyOperation;
import khanhtypo.librarycmd.util.UserRole;

import java.util.Locale;

//only admin can choose to add a new user with a role.
public abstract class AddNewUserCommand extends ModifyUserListCommand implements ICommand {
    private final DescriptiveParameters parameters;

    public AddNewUserCommand() {
        DescriptiveParameters.Builder builder = DescriptiveParameters.builder();
        this.appendParameters(builder);
        builder.add(new CommandParam("username", "Username of the account.", true));
        builder.add(new CommandParam("password", "Password of the account.", true));
        this.parameters = builder.build();
    }

    protected abstract void appendParameters(DescriptiveParameters.Builder builder);

    @Override
    public DescriptiveParameters getParameters() {
        return parameters;
    }

    @Override
    public boolean onCalled(String[] parameters) {
        int len = parameters.length;
        if (len == 3 || len == 2) {
            UserRole userRole = UserRole.READER;
            String username;
            String password;

            if (len == 2) {
                username = parameters[0];
                password = parameters[1];
            } else if (this.maxParameterLength() == 3) {
                userRole = UserRole.valueOf(parameters[0].toUpperCase(Locale.ROOT));
                username = parameters[1];
                password = parameters[2];
            } else {
                Library.println("Invalid number of parameters. Must be 2, but was " + len);
                return true;
            }

            try {
                if (!super.modifyUserList(new Account(username, password, userRole), ModifyOperation.ADD)) {
                    Library.println("Account already exists.");
                } else Library.println("Account successfully registered.");
            } catch (Exception exception) {
                Library.printError(exception);
                return false;
            }

        } else Library.println("Invalid parameter inputs for command. Expected 3 or 2, but got " + len);
        return true;
    }

    protected abstract int maxParameterLength();
}