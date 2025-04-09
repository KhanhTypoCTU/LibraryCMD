package khanhtypo.librarycmd.commands.rolespecificcommand;

import khanhtypo.librarycmd.commands.CommandPermission;
import khanhtypo.librarycmd.commands.ICommand;

interface IReaderCommand extends ICommand {
    @Override
    default CommandPermission getPermission() {
        return CommandPermission.READER;
    }
}
