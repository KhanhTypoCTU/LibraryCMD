package khanhtypo.librarycmd.books;

import org.jspecify.annotations.Nullable;

public final class PrintedBook extends Book {
    private final int pages;

    public PrintedBook(String title, String author, String genre, String ISBN, BookStatus status, BorrowRecord dueTime, int pages) {
        super(title, author, genre, ISBN, status, dueTime);
        this.pages = pages;
    }

    public int getPages() {
        return this.pages;
    }
}
