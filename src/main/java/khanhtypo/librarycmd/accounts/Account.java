package khanhtypo.librarycmd.accounts;

import khanhtypo.librarycmd.util.LibraryUtils;
import khanhtypo.librarycmd.util.PswrdObfsctr;
import khanhtypo.librarycmd.util.UserRole;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public record Account(String username, String password, UserRole role) {

    public static Account unpack(MessageUnpacker unpacker) throws IOException {
        unpacker.unpackMapHeader();
        String username = unpacker.unpackString();

        unpacker.unpackMapHeader();
        String password = PswrdObfsctr.decode(LibraryUtils.unpackValue(unpacker));
        UserRole role = UserRole.valueOf(LibraryUtils.unpackValue(unpacker));
        return new Account(username, password, role);
    }

    public void pack(MessagePacker packer) throws IOException {
        packer.packMapHeader(1);
        //map key
        packer.packString(username());
        //map values
        packer.packMapHeader(2);
        LibraryUtils.packKeyValue(packer, "password", PswrdObfsctr.encode(password()));
        LibraryUtils.packKeyValue(packer, "role", role().toString().toUpperCase());
    }
}
