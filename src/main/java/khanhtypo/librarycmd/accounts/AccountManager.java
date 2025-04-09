package khanhtypo.librarycmd.accounts;

import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.util.LibraryUtils;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class AccountManager {
    private static final AccountManager INSTANCE = new AccountManager();
    private final Map<String, Account> allAccounts;
    private final Path accountsFile;

    private AccountManager() {
        this.allAccounts = new HashMap<>();
        this.accountsFile = LibraryUtils.getOrCreateFile("all-users.msg");
    }

    private void reload() {
        this.allAccounts.clear();
        try {
            FileInputStream fileInputStream = new FileInputStream(getAccountsFile().toFile());
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(fileInputStream);
            if (unpacker.hasNext()) {
                int size = unpacker.unpackArrayHeader();
                for (int i = 0; i < size; i++) {
                    Account account = Account.unpack(unpacker);
                    this.allAccounts.put(account.username(), account);
                }
            }
            fileInputStream.close();
        } catch (Exception e) {
            Library.printError(e);
            System.exit(1);
        }
    }

    public static AccountManager getInstance() {
        return INSTANCE;
    }

    public static Path getAccountsFile() {
        return getInstance().accountsFile;
    }

    public static Stream<Account> getAllAccounts() {
        return getInstance().allAccounts.values().stream();
    }

    public static Map<String, Account> getAllAccountsMap() {
        return new HashMap<>(getInstance().allAccounts);
    }

    public static void reloadCache() {
        getInstance().reload();
    }

    public static Account getAccount(String username) {
        return getInstance().allAccounts.get(username);
    }
}
