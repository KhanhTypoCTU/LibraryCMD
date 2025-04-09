package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.accounts.AccountManager;
import khanhtypo.librarycmd.commands.paramaters.CommandParam;
import khanhtypo.librarycmd.util.ActiveUser;
import khanhtypo.librarycmd.Library;

public class LoginCommand implements ICommand {
    static LoginCommand INSTANCE;
    private final DescriptiveParameters parameters;

    LoginCommand() {
        INSTANCE = this;
        this.parameters = DescriptiveParameters.builder()
                .add(new CommandParam("username", "Username of the account", true))
                .add(new CommandParam("password", "Password of the account", true))
                .build();
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getDescription() {
        return "Login as an admin, librarian, or an user";
    }

    @Override
    public DescriptiveParameters getParameters() {
        return this.parameters;
    }

    @Override
    public boolean onCalled(String[] parameters) {
        if (Library.activeUser.hasLoggedIn()) {
            Library.println("You are already logged in.");
        } else if (parameters.length == 2) {
            String username = parameters[0];
            String password = parameters[1];
            AccountManager.getAllAccounts()
                    .filter(account -> account.username().equals(username))
                    .findFirst()
                    .ifPresentOrElse(
                            account -> {
                                if (account.password().equals(password)) {
                                    Library.printf("Welcome back ! (%s) %s\n", account.role(), account.username());
                                    Library.activeUser = new ActiveUser(account.username(), account.role());
                                } else Library.println("Wrong password!");
                            },
                            () -> Library.println("Oops, account " + username + " not found, please register.")
                    );
        } else Library.println("Password is not typed in.");
        return true;
    }

    @Override
    public CommandPermission getPermission() {
        return CommandPermission.LOG_OUT_NEEDED;
    }
}
