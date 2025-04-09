package khanhtypo.librarycmd.commands.rolespecificcommand;

import de.vandermeer.asciitable.AsciiTable;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.books.Book;
import khanhtypo.librarycmd.books.BookManager;
import khanhtypo.librarycmd.books.PrintedBook;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

public abstract class ReaderCommand implements IReaderCommand {

    /**
     * @param username username of the borrower to let the function list and print a table of book that the reader is borrowing. Otherwise, list all borrowable books
     */
    protected final List<PrintedBook> listBooks(@Nullable String username) {
        List<PrintedBook> borrowableBooks =
                username == null ? BookManager.getAllPrintedBooks().filter(Book::canBorrow).toList()
                        : BookManager.getAllPrintedBooks().filter(b -> b.getDueData().isReaderBorrowing(username)).toList();
        if (!borrowableBooks.isEmpty()) {
            if (username == null) {
                Library.println("Available books to borrow: ");
            } else Library.println("Books that you are borrowing: ");
            AsciiTable table = Library.createTableRenderer();
            table.addRule();
            String[] headerRow = new String[]{"ID", "Title", "Author", "Genre", "ISBN", "Pages"};
            if (username != null) headerRow = ArrayUtils.add(headerRow, "Due Date");
            table.addRow(List.of(headerRow));
            table.addRule();
            borrowableBooks.forEach(book -> {
                Object[] row = new Object[]{book.getId(), book.title(), book.author(), book.genre(), book.ISBN(), book.getPages()};
                if (username != null) row = ArrayUtils.add(row, book.getDueData().getDateString());
                table.addRow(row);
                table.addRule();
            });
            Library.println(table.render());
            return borrowableBooks;
        } else {
            if (username == null) {
                Library.println("There is no available book to be borrowed.");
            } else Library.println("There is no book you are borrowing.");
            return List.of();
        }
    }

    protected final int readIntInput(String field, IntPredicate validator, IntFunction<String> invalidMessage, UnaryOperator<String> formatErrorMessage) {
        while (true) {
            Library.print(field);
            String input = Library.getInput();
            try {
                int value = Integer.parseInt(input);
                if (validator.test(value)) {
                    return value;
                } else {
                    Library.println(invalidMessage.apply(value));
                }
            } catch (NumberFormatException e) {
                Library.println(formatErrorMessage.apply(input));
            }
        }
    }

    public static final class BorrowBookCommand extends ReaderCommand {

        @Override
        public String getName() {
            return "borrow-book";
        }

        @Override
        public String getDescription() {
            return "[Readers Only] Borrow A Book, if available.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            List<PrintedBook> borrowableBooks = super.listBooks(null);
            if (!borrowableBooks.isEmpty()) {
                int input = readIntInput("Book ID to borrow: ",
                        i -> borrowableBooks.stream().map(Book::getId).anyMatch(id -> id == i),
                        i -> "Book with ID" + i + " does not exists.",
                        str -> "Format error. " + str + " is not a valid book ID.");
                borrowableBooks.stream().filter(b -> b.getId() == input).findFirst().ifPresent(Book::borrow);
            }

            return true;
        }
    }

    public static final class ListBookCommand extends ReaderCommand {

        @Override
        public String getName() {
            return "list-readers-books";
        }

        @Override
        public String getDescription() {
            return "[Readers Only] List all book the reader can borrow and is borrowing.";
        }

        @Override
        public boolean onCalled(String[] parameters) {
            Library.println("");
            super.listBooks(null);
            Library.println("");
            super.listBooks(Library.activeUser.username());
            Library.println("");
            return true;
        }
    }


    public static final class ReturnBookCommand extends ReaderCommand {

        @Override
        public String getName() {
            return "return-book";
        }

        @Override
        public String getDescription() {
            return "[Readers Only] Return a borrowed book.";
        }


        //list books that readers are borrowing, then ask for reader to type ID to return.
        @Override
        public boolean onCalled(String[] parameters) {
            List<PrintedBook> borrowingBooks = super.listBooks(Library.activeUser.username());
            if (!borrowingBooks.isEmpty()) {
                int idToReturn = super.readIntInput("ID of book to return: ",
                        i -> borrowingBooks.stream().anyMatch(b -> b.getId() == i),
                        i -> "You haven't borrowed book with ID :" + i,
                        str -> "Format error. " + str + " is not a valid book ID.\n"
                );
                borrowingBooks.stream().filter(b -> b.getId() == idToReturn).findFirst().ifPresent(Book::returnBook);
            }
            return true;
        }
    }
}