package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.accounts.AccountManager;
import khanhtypo.librarycmd.books.BookManager;
import khanhtypo.librarycmd.commands.rolespecificcommand.IAdminCommand;
import khanhtypo.librarycmd.commands.rolespecificcommand.ILibrarianCommands;
import khanhtypo.librarycmd.commands.rolespecificcommand.ReaderCommand;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class CommandInputHelper {
    private static final Map<String, ICommand> allCommands = new LinkedHashMap<>();

    //regular commands
    private static final ICommand HELP = registerCommand(new HelpCommand());
    private static final ICommand LOGIN = registerCommand(new LoginCommand());
    private static final ICommand REGISTER = registerCommand(new RegisterCommand());
    private static final ICommand LOGOUT = registerCommand(new LogoutCommand());

    //admin commands
    private static final ICommand ADMIN_ADD_USER = registerCommand(new IAdminCommand.AddUserCommand());
    private static final ICommand ADMIN_REMOVE_USER = registerCommand(new IAdminCommand.RemoveUserCommand());
    private static final ICommand ADMIN_LIST_USERS = registerCommand(new IAdminCommand.ListUsersCommand());
    //librarian commands
    private static final ICommand LIBRARIAN_ADD_BOOK = registerCommand(new ILibrarianCommands.AddBookCommand());
    private static final ICommand LIBRARIAN_REMOVE_BOOK = registerCommand(new ILibrarianCommands.RemoveBookCommand());
    private static final ICommand LIBRARIAN_LIST_BOOK = registerCommand(new ILibrarianCommands.ListBookCommand());

    //reader commands
    private static final ICommand READER_BORROW_BOOK = registerCommand(new ReaderCommand.BorrowBookCommand());
    private static final ICommand READER_RETURN_BOOK = registerCommand(new ReaderCommand.ReturnBookCommand());
    private static final ICommand READER_LIST_BOOKS = registerCommand(new ReaderCommand.ListBookCommand());
    private static final ICommand EXIT = registerCommand(new ICommand() {
        @Override
        public String getName() {
            return "exit";
        }

        @Override
        public String getDescription() {
            return "End the program.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            return false;
        }

        @Override
        public CommandPermission getPermission() {
            return CommandPermission.EVERYONE;
        }
    });

    private CommandInputHelper() {
    }


    //input format : <command> [<parameters>]
    //return false if the exit command is executed
    public static boolean parseInput(final String[] inputs) {
        if (inputs.length > 0) {
            String typedCommand = inputs[0];
            ICommand command = getCommand(typedCommand);
            if (command == null)
                Library.println("Unknown command: " + typedCommand);
            else if (command.getPermission().hasPermission(Library.activeUser))
                return command.onCalled(Arrays.copyOfRange(inputs, 1, inputs.length));
            else
                Library.println(
                        command.getPermission().getCustomNoPermissionMessage() == null ?
                                "You don't have permission to use this command. Required permission: " + command.getPermission() :
                                command.getPermission().getCustomNoPermissionMessage()
                );
        }
        return true;
    }

    static Stream<ICommand> getAllCommands() {
        return allCommands.values().stream();
    }

    static ICommand getCommand(String commandName) {
        return allCommands.get(commandName);
    }

    private static ICommand registerCommand(ICommand command) {
        allCommands.put(command.getName(), command);
        return command;
    }

    public static void staticInit() {
        AccountManager.reloadCache();
        BookManager.reloadCache();
    }
}
