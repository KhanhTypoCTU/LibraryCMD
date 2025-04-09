package khanhtypo.librarycmd.commands;

import org.jspecify.annotations.Nullable;
import khanhtypo.librarycmd.util.ActiveUser;

import java.util.function.Predicate;

public enum CommandPermission {
    ADMIN(ActiveUser::isAdmin),
    LIBRARIAN(ActiveUser::isLibrarian),
    READER(ActiveUser::isReader),
    LOG_IN_NEEDED(ActiveUser::hasLoggedIn, "You need to be logged in first."),
    LOG_OUT_NEEDED(activeUser -> !activeUser.hasLoggedIn(), "You need to log out first."),
    EVERYONE(activeUser -> true);

    private final Predicate<ActiveUser> permissionFilter;
    private final @Nullable String customNoPermissionMessage;

    CommandPermission(Predicate<ActiveUser> permissionFilter, @Nullable String customNoPermissionMessage) {
        this.permissionFilter = permissionFilter;
        this.customNoPermissionMessage = customNoPermissionMessage;
    }

    CommandPermission(Predicate<ActiveUser> permissionFilter) {
        this.permissionFilter = permissionFilter;
        this.customNoPermissionMessage = null;
    }

    public boolean hasPermission(ActiveUser user) {
        return this.permissionFilter.test(user);
    }

    public @Nullable String getCustomNoPermissionMessage() {
        return this.customNoPermissionMessage;
    }
}
