package khanhtypo.librarycmd.books;

import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.util.LibraryUtils;
import org.jspecify.annotations.Nullable;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class BookManager {
    private static final BookManager INSTANCE = new BookManager();
    private final List<Book> allBooks;
    private final Path savedFile;

    public BookManager() {
        this.allBooks = new ArrayList<>();
        this.savedFile = LibraryUtils.getOrCreateFile("all-books.msg");
    }

    public static Path getSavedFile() {
        return getInstance().savedFile;
    }

    public static List<Book> getAllBooks() {
        return new ArrayList<>(INSTANCE.allBooks);
    }

    public static Stream<PrintedBook> getAllPrintedBooks() {
        return getAllBooks().stream().filter(Book::isPrintedBook).map(b -> (PrintedBook) b);
    }

    private void save() {
        try {
            FileOutputStream fos = new FileOutputStream(getInstance().savedFile.toFile());
            MessagePacker packer = MessagePack.newDefaultPacker(fos);
            packer.packArrayHeader(this.allBooks.size());
            this.allBooks.forEach(book -> {
                try {
                    book.pack(packer);
                } catch (IOException e) {
                    Library.printError(e);
                    System.exit(1);
                }
            });
            packer.close();
            fos.close();
        } catch (Exception e) {
            Library.printError(e);
            System.exit(1);
        }
    }

    private void reload() {
        this.allBooks.clear();
        try {
            FileInputStream fileInputStream = new FileInputStream(savedFile.toFile());
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(fileInputStream);

            if (unpacker.hasNext()) {
                int size = unpacker.unpackArrayHeader();
                for (int i = 0; i < size; i++) {
                    this.allBooks.add(Book.unpack(unpacker));
                }
            }
            unpacker.close();
            fileInputStream.close();
        } catch (Exception e) {
            Library.printError(e);
        }
    }

    public static BookManager getInstance() {
        return INSTANCE;
    }

    public static void reloadCache() {
        getInstance().reload();
    }

    public static int getBooksCount() {
        return getInstance().allBooks.size();
    }

    @Nullable
    public static Book getBookById(int id) {
        return getBooksCount() >= id ? null : getAllBooks().get(id);
    }

    @Nullable
    public static Book getBookByTitle(String title) {
        return getAllBooks().stream().filter(b -> b.title().equals(title)).findFirst().orElse(null);
    }

    @Nullable
    public static Book getBookByISBN(String isbn) {
        return getAllBooks().stream().filter(b -> b.title().equals(isbn)).findFirst().orElse(null);
    }

    public static void saveToDisk() {
        getInstance().save();
    }
}