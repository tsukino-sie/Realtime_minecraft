package always.io.kr.realtime_minecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RealTimeCommand implements CommandExecutor {

    private final RealTimeSync plugin;

    public RealTimeCommand(RealTimeSync plugin) {
        this.plugin = plugin; // 메인 플러그인 인스턴스를 받아옴 (Dependency Injection)
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false; // 사용법 출력 유도

        String action = args[0].toLowerCase();

        switch (action) {
            case "start":
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(ChatColor.YELLOW + "이미 동기화가 실행 중입니다.");
                } else {
                    plugin.startSyncTask();
                    sender.sendMessage(ChatColor.GREEN + "현실 시간 동기화를 시작합니다.");
                }
                break;

            case "stop":
                if (!plugin.isTaskRunning()) {
                    sender.sendMessage(ChatColor.YELLOW + "실행 중인 동기화 작업이 없습니다.");
                } else {
                    plugin.stopSyncTask();
                    sender.sendMessage(ChatColor.RED + "현실 시간 동기화를 중지했습니다.");
                }
                break;

            case "status":
                if (plugin.isTaskRunning()) {
                    sender.sendMessage("현재 상태: " + ChatColor.GREEN + "작동 중 (Running)");
                } else {
                    sender.sendMessage("현재 상태: " + ChatColor.RED + "중지됨 (Stopped)");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다.");
                return false;
        }
        return true;
    }
}