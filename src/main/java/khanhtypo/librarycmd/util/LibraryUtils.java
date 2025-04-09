package khanhtypo.librarycmd.util;

import khanhtypo.librarycmd.Library;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class LibraryUtils {
    public static void packMap(MessagePacker packer, Map<String, String> map) {
        map.forEach((k, v) -> {
            try {
                packKeyValue(packer, k, v);
            } catch (IOException e) {
                Library.printError(e);
            }
        });
    }

    /**
     * Should call {@link MessagePacker#packMapHeader(int)} before calling this
     */
    public static void packKeyValue(MessagePacker packer, String key, String value) throws IOException {
        packer.packString(key);
        packer.packString(value);
    }

    /**
     * Unpack a packed map of key-value, providing only the value is needed.
     * Should call {@link MessageUnpacker#unpackMapHeader()} before this
     */
    public static String unpackValue(MessageUnpacker unpacker) throws IOException {
        unpacker.skipValue();
        return unpacker.unpackString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Path getOrCreateFile(String fileName) {
        Path path = Library.LIBRARY_DATA.resolve(fileName);
        File file = path.toFile();
        if (!file.exists()) {
            try  {file.createNewFile();} catch (IOException e) {Library.printError(e);}
        }
        return path;
    }
}
