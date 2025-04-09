package khanhtypo.librarycmd;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridConfig;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import khanhtypo.librarycmd.commands.CommandInputHelper;
import khanhtypo.librarycmd.util.ActiveUser;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class Library {
    public static final Path LIBRARY_DATA = Path.of("library-data/");
    private static final Scanner scanner = new Scanner(System.in);
    public static ActiveUser activeUser = ActiveUser.NO_ACTIVE_USER;

    public static void main(String[] args) {
        LIBRARY_DATA.toFile().mkdir();
        CommandInputHelper.staticInit();
        println("Welcome to Library Management System.");
        println("Type 'help' to list all commands. Type 'exit' to end the session.");

        do {
            printf("%s> ", activeUser == ActiveUser.NO_ACTIVE_USER ? "" : "(%c) %s ".formatted(activeUser.userRole().toString().charAt(0), activeUser.username()));
        } while (CommandInputHelper.parseInput(getInputs()));
    }

    private static String[] getInputs() {
        return Arrays.stream(scanner.nextLine().split(" ")).filter(string -> !string.isEmpty()).toArray(String[]::new);
    }
    public static AsciiTable createTableRenderer() {
        AsciiTable table = new AsciiTable().setTextAlignment(TextAlignment.CENTER);
        table.getContext().getGrid().addCharacterMap(TA_GridConfig.RULESET_NORMAL, '\0', '\u2500', '\u2502', '\u250C', '\u2510', '\u2514', '\u2518', '\u251C', '\u2524', '\u253C', '\u252C', '\u2534');
        return table;
    }
    public static String getInput() {
        return scanner.nextLine();
    }

    public static <T extends Exception> void printError(T exception) {
        exception.printStackTrace(System.err);
    }

    public static void println(String message) {
        System.out.println(message);
    }

    public static void print(String message) {
        System.out.print(message);
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }
}