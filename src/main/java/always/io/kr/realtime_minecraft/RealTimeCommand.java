package always.io.kr.realtime_minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class RealTimeCommand implements CommandExecutor {

    private final RealTimeSync plugin;

    public RealTimeCommand(RealTimeSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 0) return false;

        String action = args[0].toLowerCase();

        switch (action) {
            case "start":
                if (plugin.isTaskRunning()) {
                    // ChatColor.YELLOW 대신 NamedTextColor.YELLOW 사용
                    sender.sendMessage(Component.text("이미 동기화가 실행 중입니다.", NamedTextColor.YELLOW));
                } else {
                    plugin.startSyncTask();
                    sender.sendMessage(Component.text("현실 시간 동기화를 시작합니다.", NamedTextColor.GREEN));
                }
                break;

            case "stop":
                if (!plugin.isTaskRunning()) {
                    sender.sendMessage(Component.text("실행 중인 동기화 작업이 없습니다.", NamedTextColor.YELLOW));
                } else {
                    plugin.stopSyncTask();
                    sender.sendMessage(Component.text("현실 시간 동기화를 중지했습니다.", NamedTextColor.RED));
                }
                break;

            case "status":
                if (plugin.isTaskRunning()) {
                    // 텍스트를 이어 붙일 때는 append() 사용
                    sender.sendMessage(
                            Component.text("현재 상태: ")
                                    .append(Component.text("작동 중 (Running)", NamedTextColor.GREEN))
                    );
                } else {
                    sender.sendMessage(
                            Component.text("현재 상태: ")
                                    .append(Component.text("중지됨 (Stopped)", NamedTextColor.RED))
                    );
                }
                break;

            default:
                sender.sendMessage(Component.text("알 수 없는 명령어입니다.", NamedTextColor.RED));
                return false;
        }
        return true;
    }
}