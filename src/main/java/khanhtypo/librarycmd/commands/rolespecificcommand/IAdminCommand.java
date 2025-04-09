package khanhtypo.librarycmd.commands.rolespecificcommand;

import de.vandermeer.asciitable.AsciiTable;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.accounts.AccountManager;
import khanhtypo.librarycmd.commands.*;
import khanhtypo.librarycmd.commands.paramaters.CommandParam;
import khanhtypo.librarycmd.util.PswrdObfsctr;

public interface IAdminCommand extends ICommand {
    @Override
    default CommandPermission getPermission() {
        return CommandPermission.ADMIN;
    }

    final class AddUserCommand extends AddNewUserCommand implements IAdminCommand {
        @Override
        public String getName() {
            return "add-user";
        }

        @Override
        public String getDescription() {
            return "[ADMIN ONLY] Add a new account with a role.";
        }

        @Override
        protected void appendParameters(DescriptiveParameters.Builder builder) {
            builder.add(new CommandParam("user-role", "Role of the new user account (non-case sensitive). Can either be ADMIN, LIBRARIAN, or USER", false));
        }

        @Override
        protected int maxParameterLength() {
            return 3;
        }
    }

    final class RemoveUserCommand extends ModifyUserListCommand implements IAdminCommand {
        private final DescriptiveParameters parameters;

        public RemoveUserCommand() {
            this.parameters = DescriptiveParameters.builder()
                    .add(new CommandParam("username", "Username of the account to be removed.", true))
                    .build();
        }


        @Override
        public DescriptiveParameters getParameters() {
            return this.parameters;
        }

        @Override
        public String getName() {
            return "remove-user";
        }

        @Override
        public String getDescription() {
            return "[ADMIN ONLY] Remove an account.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            if (parameters.length != 1) {
                Library.println("Invalid number of parameters: " + parameters.length);
            }

            try {
                if (!super.removeUser(parameters[0])) {
                    Library.println("Removing an account that is not existed.");
                } else Library.println("Account successfully removed.");
            } catch (Exception e) {
                Library.printError(e);
                return false;
            }
            return true;
        }
    }

    final class ListUsersCommand implements IAdminCommand {

        @Override
        public String getName() {
            return "list-users";
        }

        @Override
        public String getDescription() {
            return "[ADMIN ONLY] List all available account in a table.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            AsciiTable table = Library.createTableRenderer();
            table.addRule();
            table.addRow("Username", "User Role", "Password", "Password (Obfuscated)" );
            table.addRule();
            AccountManager.getAllAccounts().forEach(account ->
                    table.addRow(account.username(), account.role(), account.password(), PswrdObfsctr.encode(account.password()))
            );
            table.addRule();

            Library.println(table.render());

            return true;
        }
    }
}