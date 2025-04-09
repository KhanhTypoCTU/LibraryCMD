package khanhtypo.librarycmd.commands.rolespecificcommand;

import de.vandermeer.asciitable.AsciiTable;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.books.*;
import khanhtypo.librarycmd.commands.CommandPermission;
import khanhtypo.librarycmd.commands.ICommand;
import khanhtypo.librarycmd.util.ModifyOperation;
import org.jspecify.annotations.Nullable;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.FileOutputStream;
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
                int pages = Integer.parseInt(readInput("Page Count", s -> "Pages count can not be 0 or lower.", s -> Integer.parseInt(s) > 0));
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

        public RemoveBookCommand() {
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
            ListBookCommand.listAllBooks();

            int id = ReaderCommand.readIntInput("ID of the book to be removed: ",
                    i -> BookManager.getBookById(i) != null,
                    i -> "Invalid book ID " + i,
                    str -> "Your input is not a valid book ID format, should be a number.");

            try {
                if (this.modifyBookList(BookManager.getBookById(id), ModifyOperation.REMOVE))
                    Library.println("\nBook successfully removed from the database.\n");
            } catch (Exception e) {
                Library.printError(e);
                return false;
            }
            return true;
        }
    }

    final class ListBookCommand implements ILibrarianCommands {
        public ListBookCommand() {
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
        public boolean onCalled(String[] ignored) {
            listAllBooks();
            return true;
        }

        private static void listAllBooks() {
            List<Book> allBooks = BookManager.getAllBooks();

            //List all printed book
            Library.println("All Printed Books: ");
            AsciiTable printed = Library.createTableRenderer();
            printed.addRule();
            printed.addRow("ID", "Title", "Author", "Genre", "ISBN", "Pages", "Status", "Due Date");
            printed.addRule();
            allBooks.stream().filter(Book::isPrintedBook).map(b -> (PrintedBook) b).forEach(
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
            allBooks.stream().filter(Book::isEBook).map(b -> (EBook) b).forEach(
                    book -> {
                        ebook.addRow(book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), book.status(), book.getFileFormat());
                        ebook.addRule();
                    }
            );
            Library.println(ebook.render());
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