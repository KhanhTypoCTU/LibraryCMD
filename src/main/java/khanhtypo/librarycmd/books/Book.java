package khanhtypo.librarycmd.books;

import com.google.common.collect.ImmutableMap;
import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.util.LibraryUtils;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public abstract class Book {
    private int id;
    private final String title;
    private final String author;
    private final String genre;
    private final String ISBN;
    private BookStatus status;
    private BorrowRecord dueData;

    protected Book(String title, String author, String genre, String ISBN, BookStatus status, BorrowRecord dueData) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ISBN = ISBN;
        this.status = status;
        this.dueData = dueData == null ? BorrowRecord.NONE : dueData;
        this.id = -1;
    }

    public void pack(MessagePacker packer) throws IOException {
        packer.packMapHeader(1)
                .packString(String.valueOf(this.id))
                .packMapHeader(this.dueData == BorrowRecord.NONE ? 7 : 9);
        LibraryUtils.packMap(packer, ImmutableMap.<String, String>builder()
                .put("bookType", this instanceof EBook ? "ebook" : "printed")
                .put("title", this.title)
                .put("author", this.author)
                .put("genre", this.genre)
                .put("ISBN", this.ISBN)
                .put("status", this.status.toString())
                .put(this instanceof EBook eBook ? new ImmutableEntry("fileFormat", eBook.getFileFormat()) : new ImmutableEntry("pages", String.valueOf(((PrintedBook) this).getPages())))
                .build());
        if (this.dueData != BorrowRecord.NONE) {
            LibraryUtils.packKeyValue(packer, "borrowed-user", this.dueData.username);
            LibraryUtils.packKeyValue(packer, "due-date", BorrowRecord.DATE_FORMATTER.format(this.dueData.dueDate));
        }
    }

    public static Book unpack(MessageUnpacker unpacker) throws IOException {
        unpacker.unpackMapHeader();
        int id = Integer.parseInt(unpacker.unpackString());
        int dataCounts = unpacker.unpackMapHeader();
        String type = LibraryUtils.unpackValue(unpacker);
        String title = LibraryUtils.unpackValue(unpacker);
        String author = LibraryUtils.unpackValue(unpacker);
        String genre = LibraryUtils.unpackValue(unpacker);
        String ISBN = LibraryUtils.unpackValue(unpacker);
        BookStatus status = BookStatus.valueOf(LibraryUtils.unpackValue(unpacker));
        String extraData = LibraryUtils.unpackValue(unpacker);

        BorrowRecord dueTime = BorrowRecord.NONE;
        if (dataCounts == 9) {
            String username = LibraryUtils.unpackValue(unpacker);
            LocalDate dueDate = LocalDate.parse(LibraryUtils.unpackValue(unpacker), BorrowRecord.DATE_FORMATTER);
            dueTime = new BorrowRecord(username, dueDate);
        }

        Book book = type.equals("ebook") ?
                new EBook(title, author, genre, ISBN, status, dueTime, extraData)
                : new PrintedBook(title, author, genre, ISBN, status, dueTime, Integer.parseInt(extraData));
        book.setId(id);
        return book;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public String genre() {
        return genre;
    }

    public String ISBN() {
        return ISBN;
    }

    public BookStatus status() {
        return status;
    }

    public String dueDataString() {
        return this.dueData.toString();
    }

    public boolean hasDueTime() {
        return this.dueData != BorrowRecord.NONE;
    }

    public boolean canBorrow() {
        return !(this.isEBook() || this.hasDueTime()) && this.status() == BookStatus.AVAILABLE && Library.activeUser.isReader();
    }

    public boolean isEBook() {
        return this instanceof EBook;
    }

    public boolean isPrintedBook() {
        return this instanceof PrintedBook;
    }

    public void borrow() {
        if (this.canBorrow()) {
            this.setDueData(new BorrowRecord(Library.activeUser.username(), LocalDate.now().plusWeeks(2)));
            this.status = BookStatus.NOT_AVAILABLE;
            Library.println("You have successfully borrowed the book.");
            BookManager.saveToDisk();
        } else {
            Library.print("You cannot borrow this book right now, because ");
            if (this.isEBook()) Library.println("this is not a physical book.");
            else if (this.hasDueTime()) Library.println("it has already been borrowed.");
            else if (!Library.activeUser.isReader()) Library.println("you have to be a reader to borrow this.");
            else Library.println("the book isn't yet available on the shelf.");
        }
    }

    public void returnBook() {
        this.dueData = BorrowRecord.NONE;
        this.status = BookStatus.AVAILABLE;
        Library.println("You have successfully returned the book.");
        BookManager.saveToDisk();
    }

    public BorrowRecord getDueData() {
        return dueData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Book) obj;
        return Objects.equals(this.title, that.title) &&
                Objects.equals(this.author, that.author) &&
                Objects.equals(this.genre, that.genre) &&
                Objects.equals(this.ISBN, that.ISBN) &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.dueData, that.dueData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, genre, ISBN, status, dueData);
    }

    @Override
    public String toString() {
        return "Book[" +
                "title=" + title + ", " +
                "author=" + author + ", " +
                "genre=" + genre + ", " +
                "ISBN=" + ISBN + ", " +
                "status=" + status + ", " +
                "dueTime=" + dueData + ']';
    }

    public void setDueData(BorrowRecord record) {
        this.dueData = record == null ? BorrowRecord.NONE : record;
    }

    public static final class BorrowRecord {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        private static final BorrowRecord NONE = new BorrowRecord(null, null);
        private final String username;
        private final LocalDate dueDate;

        public BorrowRecord(String username, LocalDate dueDate) {
            this.username = username;
            this.dueDate = dueDate;
        }

        public boolean isReaderBorrowing(String username) {
            return this != NONE && this.username.equals(username);
        }


        public String getDateString() {
            return this.dueDate.format(DATE_FORMATTER);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (BorrowRecord) obj;
            return Objects.equals(this.username, that.username) &&
                    Objects.equals(this.dueDate, that.dueDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, dueDate);
        }

        @Override
        public String toString() {
            return this == NONE ? "none" : String.format("Due : %s. By : %s", this.getDateString(), this.username);
        }
    }

    private record ImmutableEntry(String key, String value) implements Map.Entry<String, String> {

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
