package khanhtypo.librarycmd.commands.rolespecificcommand;

import de.vandermeer.asciitable.AsciiTable;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.books.*;
import khanhtypo.librarycmd.commands.CommandPermission;
import khanhtypo.librarycmd.commands.DescriptiveParameters;
import khanhtypo.librarycmd.commands.ICommand;
import khanhtypo.librarycmd.commands.paramaters.CommandParam;
import khanhtypo.librarycmd.util.ModifyOperation;
import org.jspecify.annotations.Nullable;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface ILibrarianCommands extends ICommand {
    @Override
    default CommandPermission getPermission() {
        return CommandPermission.LIBRARIAN;
    }

    default boolean modifyBookList(Book book, ModifyOperation operation) throws Exception {
        List<Book> allBooks = BookManager.getAllBooks();
        FileOutputStream fileOutputStream = new FileOutputStream(BookManager.getSavedFile().toFile());
        MessagePacker packer = MessagePack.newDefaultPacker(fileOutputStream);
        if (operation == ModifyOperation.ADD) {
            String duplicationMessage = this.ensureDuplication(allBooks, book);
            if (duplicationMessage != null) {
                Library.println(duplicationMessage);
                return false;
            }
            allBooks.add(book);
        } else {
            if (!allBooks.remove(book)) {
                Library.println("Removing a non-existing book.");
                return false;
            }
        }
        int size = allBooks.size();
        packer.packArrayHeader(size);
        for (int i = 0; i < size; i++) {
            Book book1 = allBooks.get(i);
            book1.setId(i);
            book1.pack(packer);
        }
        packer.close();
        fileOutputStream.close();
        BookManager.reloadCache();
        return true;
    }

    @Nullable
    default String ensureDuplication(List<Book> list, Book toCheck) {
        for (Book book : list) {
            if (book.getId() == toCheck.getId()) return "Duplicate Book ID: " + book.getId();
            else if (book.title().equals(toCheck.title())) return "Duplicate Title: " + book.title();
            else if (book.ISBN().equals(toCheck.ISBN())) return "Duplicate ISBN: " + book.ISBN();
        }
        return null;
    }

    final class AddBookCommand implements ILibrarianCommands {

        @Override
        public String getName() {
            return "add-book";
        }

        @Override
        public String getDescription() {
            return "[Librarian Only] Start adding a new book to database.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            String bookType = readInput("Book Type (printed/ebook)"
                    , (input) -> "Invalid input, accepted input: printed or ebook.",
                    input -> input.equalsIgnoreCase("printed") || input.equalsIgnoreCase("ebook"));
            String title = readInput("Title", s -> "", s -> true);
            String author = readInput("Author(s)", s -> "", s -> true);
            String genre = readInput("Genre", s -> "", s -> true);
            String ISBN = readInput("ISBN", s -> "ISBN should be an ISBN10 or ISBN13, no dashes."
                    , input -> {
                        int len = input.length();
                        return len == 10 || len == 13;
                    }
            );

            Book book;
            if (bookType.equalsIgnoreCase("ebook")) {
                String fileFormat = readInput("File Format", s -> "Invalid file format.", s -> true);
                book = new EBook(title, author, genre, ISBN, BookStatus.AVAILABLE, null, fileFormat.replace('.', '\0'));
            } else {
                int pages = Integer.parseInt(readInput("Page Count",  s -> "Pages count can not be 0 or lower.", s -> Integer.parseInt(s) > 0));
                book = new PrintedBook(title, author, genre, ISBN, BookStatus.AVAILABLE, null, pages);
            }


            try {
                if (this.modifyBookList(book, ModifyOperation.ADD))
                    Library.println("\nBook successfully added to the database.\n");
            } catch (Exception e) {
                Library.printError(e);
                return false;
            }

            return true;
        }

        static String readInput(String field, UnaryOperator<String> invalidMessage, Predicate<String> checker) {
            do {
                Library.print(field + ':');
                String input = Library.getInput();
                if (!checker.test(input)) {
                    Library.println(invalidMessage.apply(input));
                } else return input;
            } while (true);
        }
    }

    final class RemoveBookCommand implements ILibrarianCommands {

        private final DescriptiveParameters parameters;

        public RemoveBookCommand() {
            this.parameters = DescriptiveParameters.builder()
                    .add(new CommandParam("id-number", "Remove the book by its id.", false).setPrefix("id"))
                    .add(new CommandParam("book-title", "Remove the book information by its title.", false).setPrefix("title"))
                    .add(new CommandParam("book-isbn", "Remove the book information by its ISBN.", false).setPrefix("isbn"))
                    .build();
        }

        @Override
        public DescriptiveParameters getParameters() {
            return parameters;
        }

        @Override
        public String getName() {
            return "remove-book";
        }

        @Override
        public String getDescription() {
            return "[Librarian Only] Remove a book from database.";
        }

        @Override
        public boolean onCalled(String[] parameters) {

            if (parameters.length == 0) {
                Library.println("Don't know what to remove ?");
            } else if (parameters.length == 1) {
                Library.println("Missing parameter value.");
            } else {
                try {
                    String parameterKey = parameters[0];
                    switch (parameterKey) {
                        case "id" -> {
                            if (parameters.length == 2) {
                                int bookId = Integer.parseInt(parameters[1]);
                                Book book = BookManager.getBookById(bookId);
                                if (book != null) {
                                    this.modifyBookList(book, ModifyOperation.REMOVE);
                                } else {
                                    Library.println("Book with id " + bookId + " not found.");
                                    return true;
                                }
                            } else Library.println("Expected a numeric id.");
                        }
                        case "title" -> {
                            String title = String.join(" ", Arrays.copyOfRange(parameters, 1, parameters.length));
                            Book book = BookManager.getBookByTitle(title);
                            if (book != null) {
                                this.modifyBookList(book, ModifyOperation.REMOVE);
                            } else Library.println("Book with title \"" + title + "\" not found.");
                        }
                        case "isbn" -> {
                            if (parameters.length == 2) {
                                Book book = BookManager.getBookByISBN(parameters[1]);
                                if (book != null) {
                                    this.modifyBookList(book, ModifyOperation.REMOVE);
                                } else Library.println("Book with ISBN " + parameters[1] + " not found.");
                            } else Library.println("Expecting a numeric ISBN value.");
                        }
                        default -> Library.println("Unknown parameter key: " + parameterKey);
                    }
                } catch (Exception e) {
                    Library.printError(e);
                    return false;
                }
            }

            return true;
        }
    }

    final class ListBookCommand implements ILibrarianCommands {

        private final DescriptiveParameters parameters;

        public ListBookCommand() {
            this.parameters = DescriptiveParameters.builder()
                    .add(new CommandParam("id-number", "Search and list the book information by its id.", false).setPrefix("id"))
                    .add(new CommandParam("book-title", "Search and list the book information by its title.", false).setPrefix("title"))
                    .add(new CommandParam("book-isbn", "Search and list the book information by its ISBN.", false).setPrefix("isbn"))
                    .build();
        }

        @Override
        public DescriptiveParameters getParameters() {
            return parameters;
        }

        @Override
        public String getName() {
            return "list-books";
        }

        @Override
        public String getDescription() {
            return "[Librarian Only] List all books.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            if (parameters.length == 0) {
                List<Book> allBooks = BookManager.getAllBooks();

                //List all printed book
                Library.println("All Printed Books: ");
                AsciiTable printed = Library.createTableRenderer();
                printed.addRule();
                printed.addRow("ID", "Title", "Author", "Genre", "ISBN", "Pages", "Status", "Due Date");
                printed.addRule();
                allBooks.stream().filter(b -> b instanceof PrintedBook).map(b -> (PrintedBook) b).forEach(
                        book -> {
                            printed.addRow(book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), book.getPages(), book.status(), book.dueDataString());
                            printed.addRule();
                        }
                );
                //printed.addRule();
                Library.println(printed.render());
                Library.println("\n All Digital Books: ");
                //List all ebooks
                AsciiTable ebook = Library.createTableRenderer();
                ebook.addRule();
                ebook.addRow("ID", "Title", "Author", "Genre", "ISBN", "Status", "File Format");
                ebook.addRule();
                allBooks.stream().filter(b -> b instanceof EBook).map(b -> (EBook) b).forEach(
                        book -> {
                            ebook.addRow(book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), book.status(), book.getFileFormat());
                            ebook.addRule();
                        }
                );
                Library.println(ebook.render());
            } else if (parameters.length == 1) {
                Library.println("Unknown parameter: " + parameters[0]);
            } else {
                try {
                    AsciiTable table = Library.createTableRenderer();
                    String parameterKey = parameters[0];
                    switch (parameterKey) {
                        case "id" -> {
                            if (parameters.length == 2) {
                                int id = Integer.parseInt(parameters[1]);
                                Book book = BookManager.getBookById(id);
                                if (book == null) {
                                    Library.println("Book ID not existed: " + id);
                                    return true;
                                }
                                this.printSingletonTable(table, book);

                            } else Library.println("Expected an numeric id input.");
                        }
                        case "title" -> {
                            String title = String.join(" ", Arrays.copyOfRange(parameters, 1, parameters.length));
                            Book foundBook = BookManager.getBookByTitle(title);
                            if (foundBook != null) {
                                this.printSingletonTable(table, foundBook);
                            } else Library.println("No book with title " + title + " was found");
                        }
                        case "isbn" -> {
                            if (parameters.length == 2) {
                                Book book = BookManager.getBookByISBN(parameters[1]);
                                if (book == null) {
                                    Library.println("Book ISBN " + parameters[1] + " not found.");
                                } else this.printSingletonTable(table, book);
                            } else Library.println("Expected an numeric ISBN value.");
                        }
                        default -> Library.println("Unknown parameter key: " + parameterKey);
                    }
                } catch (Exception e) {
                    Library.printError(e);
                    return false;
                }
            }

            return true;
        }

        private void printSingletonTable(AsciiTable table, Book book) {
            table.addRule();
            if (book instanceof EBook eBook) {
                table.addRow("ID", "Title", "Author", "Genre", "ISBN", "Status", "File Format");
                table.addRow(book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), book.status(), eBook.getFileFormat());
                table.addRule();
            } else {
                table.addRow("ID", "Title", "Author", "Genre", "ISBN", "Pages", "Status", "Due Date");
                table.addRow(book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), ((PrintedBook) book).getPages(), book.status(), book.dueDataString());
                table.addRule();
            }
        }
    }

}