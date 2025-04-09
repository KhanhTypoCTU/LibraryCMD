package khanhtypo.librarycmd.commands;

import khanhtypo.librarycmd.Library;
import khanhtypo.librarycmd.accounts.Account;
import khanhtypo.librarycmd.accounts.AccountManager;
import khanhtypo.librarycmd.books.Book;
import khanhtypo.librarycmd.books.BookManager;
import khanhtypo.librarycmd.util.ModifyOperation;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public abstract class ModifyUserListCommand implements ICommand {
    private final Path accountsFile;

    public ModifyUserListCommand() {
        this.accountsFile = AccountManager.getAccountsFile();
    }

    protected boolean removeUser(String username) throws IOException {
        return this.modifyUserList(AccountManager.getAccount(username), ModifyOperation.REMOVE);
    }

    /**
     * @return false if the add operation adding an account with existing name, or if the remove operation removes a non-existing account name; true otherwise.
     */
    protected boolean modifyUserList(Account account, ModifyOperation operation) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(this.accountsFile.toFile());
        MessagePacker packer = MessagePack.newDefaultPacker(fileOutputStream);

        Map<String, Account> mappings = AccountManager.getAllAccountsMap();
        if (operation == ModifyOperation.ADD) {
            if (mappings.containsKey(account.username())) return false;
            mappings.put(account.username(), account);
        } else {
            if (mappings.remove(account.username()) == null) return false;
            BookManager.getAllBooks().stream().filter(b -> b.getDueData().isReaderBorrowing(account.username())).forEach(b -> b.setDueData(null));
            BookManager.saveToDisk();
        }

        int mapSize = mappings.size();
        packer.packArrayHeader(mapSize);
        Iterable<Account> accounts = mappings.values();
        for (Account acc : accounts) {
            try {
                acc.pack(packer);
            } catch (Exception e) {
                Library.println("Error packing account: " + acc.username());
                Library.println("Error: " + e.getMessage());
                return false;
            }
        }
        packer.close();
        fileOutputStream.close();
        AccountManager.reloadCache();
        return true;
    }
}
