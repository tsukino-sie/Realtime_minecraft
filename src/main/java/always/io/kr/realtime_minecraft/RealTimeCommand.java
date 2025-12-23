package always.io.kr.realtime_minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TabCompleter 인터페이스 추가 구현
public class RealTimeCommand implements CommandExecutor, TabCompleter {

    private final RealTimeSync plugin;
    // 자동완성 후보 리스트 미리 정의
    private static final List<String> COMMANDS = Arrays.asList("start", "stop", "status");

    public RealTimeCommand(RealTimeSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 0) {
            // 사용법 안내에도 Prefix 적용
            sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("사용법: /realtime <start|stop|status>", NamedTextColor.RED)));
            return false;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "start":
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("이미 동기화가 실행 중입니다.", NamedTextColor.YELLOW)));
                } else {
                    plugin.startSyncTask();
                    sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("현실 시간 동기화를 시작합니다.", NamedTextColor.GREEN)));
                }
                break;

            case "stop":
                if (!plugin.isTaskRunning()) {
                    sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("실행 중인 동기화 작업이 없습니다.", NamedTextColor.YELLOW)));
                } else {
                    plugin.stopSyncTask();
                    sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("현실 시간 동기화를 중지했습니다.", NamedTextColor.RED)));
                }
                break;

            case "status":
                Component statusText = plugin.isTaskRunning()
                        ? Component.text("작동 중 (Running)", NamedTextColor.GREEN)
                        : Component.text("중지됨 (Stopped)", NamedTextColor.RED);

                sender.sendMessage(RealTimeSync.PREFIX
                        .append(Component.text("현재 상태: ", NamedTextColor.WHITE))
                        .append(statusText));
                break;

            default:
                sender.sendMessage(RealTimeSync.PREFIX.append(Component.text("알 수 없는 명령어입니다.", NamedTextColor.RED)));
                return false;
        }
        return true;
    }

    /**
     * 탭 자동완성 로직 (JS의 filter와 비슷)
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        // args[0] (첫 번째 인자)를 입력 중일 때
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            // 사용자가 입력한 값(args[0])과 일치하는 후보만 필터링해서 completions에 담음
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
            // 알파벳 순 정렬
            Collections.sort(completions);
            return completions;
        }
        // 인자가 더 없으면 빈 리스트 반환
        return Collections.emptyList();
    }
}