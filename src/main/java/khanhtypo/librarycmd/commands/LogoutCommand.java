package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.util.ActiveUser;
import khanhtypo.librarycmd.Library;

public class LogoutCommand implements ICommand {

    LogoutCommand() {
    }

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public String getDescription() {
        return "Logout the current user.";
    }

    @Override
    public boolean onCalled(String[] parameters) {
        Library.println(
                Library.activeUser.hasLoggedIn() ? "You have logged out." : "You have already logged out."
        );

        Library.activeUser = ActiveUser.NO_ACTIVE_USER;
        return true;
    }

    @Override
    public CommandPermission getPermission() {
        return CommandPermission.EVERYONE;
    }
}
