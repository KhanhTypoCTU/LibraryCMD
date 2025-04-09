package khanhtypo.librarycmd.books;

import org.jspecify.annotations.Nullable;

public final class EBook extends Book {
    private final String fileFormat;

    public EBook(String title, String author, String genre, String ISBN, BookStatus status, BorrowRecord dueTime, String fileFormat) {
        super(title, author, genre, ISBN, status, dueTime);
        this.fileFormat = fileFormat;
    }

    public String getFileFormat() {
        return fileFormat;
    }
}