package always.io.kr.realtime_minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.util.Objects;

public final class RealTimeSync extends JavaPlugin {

    // 1. Prefix 상수 정의 (어디서든 RealTimeSync.PREFIX 로 접근 가능)
    // [RealTime] 형태로, 색상을 입혀서 예쁘게 만듭니다.
    public static final Component PREFIX = Component.empty()
            .append(Component.text("[", NamedTextColor.GRAY))
            .append(Component.text("RealTime", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("] ", NamedTextColor.GRAY));


    private BukkitTask syncTask; // 실행 중인 작업 핸들 (JS의 timer ID 같은 것)

    @Override
    public void onEnable() {
        // 명령어 등록 (plugin.yml의 이름과 일치해야 함)
        Objects.requireNonNull(getCommand("realtime")).setExecutor(new RealTimeCommand(this));
        getLogger().info("플러그인 로드 완료! /realtime start 로 시작하세요.");
    }

    @Override
    public void onDisable() {
        stopSyncTask(); // 서버 꺼지면 안전하게 종료
    }

    // 작업이 실행 중인지 확인 (Getter)
    public boolean isTaskRunning() {
        return syncTask != null && !syncTask.isCancelled();
    }

    public void startSyncTask() {
        if (isTaskRunning()) return;

        // 20틱(1초)마다 실행
        syncTask = new BukkitRunnable() {
            @Override
            public void run() {
                syncTimeLogic();
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    public void stopSyncTask() {
        if (isTaskRunning()) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    // ===== 핵심 로직: 시간 계산 및 적용 =====
    private void syncTimeLogic() {
        LocalTime now = LocalTime.now();
        long currentSeconds = now.toSecondOfDay(); // 0 ~ 86399

        // 오늘의 일출, 일몰 시간 계산 (초 단위)
        long sunriseSec = SunUtil.getSunTime(1);
        long sunsetSec = SunUtil.getSunTime(2);
        long noonSec = (sunriseSec + sunsetSec) / 2; // 남중 고도 시간

        long mcTick;

        /*
         * 구간별 선형 보간 (Linear Interpolation)
         * 마인크래프트 시간:
         * 23000(0) = 일출
         * 6000     = 정오 (해 가장 높음)
         * 12000    = 일몰 시작
         * 13000    = 완전히 어두워짐
         * 18000    = 자정
         */

        if (currentSeconds >= sunriseSec && currentSeconds < noonSec) {
            // 구간 1: 일출 ~ 정오 (MC 23000 ~ 30000(==6000))
            // 23000에서 시작해서 6000까지 가려면 7000틱이 흘러야 함.
            // 계산 편의상 23000을 -1000으로 취급하거나, 30000(6000+24000)으로 매핑
            mcTick = mapTime(currentSeconds, sunriseSec, noonSec, 23000, 30000);

        } else if (currentSeconds >= noonSec && currentSeconds < sunsetSec) {
            // 구간 2: 정오 ~ 일몰 (MC 6000 ~ 13000)
            mcTick = mapTime(currentSeconds, noonSec, sunsetSec, 6000, 13000);

        } else if (currentSeconds >= sunsetSec) {
            // 구간 3: 일몰 ~ 자정(24시) (MC 13000 ~ 18000+)
            // 현실 자정(86400)을 마크의 어느 시점으로 잡을지 결정해야 함 (보통 18000 근처)
            // 여기서는 단순하게 비율대로 흘러가게 둠
            mcTick = mapTime(currentSeconds, sunsetSec, 86400, 13000, 18000);

        } else {
            // 구간 4: 자정(0시) ~ 일출 (MC 18000 ~ 23000)
            mcTick = mapTime(currentSeconds, 0, sunriseSec, 18000, 23000);
        }

        // 24000 넘으면 순환
        mcTick = mcTick % 24000;

        for (World world : Bukkit.getWorlds()) {
            world.setTime(mcTick);
        }
    }

    // C의 map 함수와 동일: 값 x를 입력범위(inMin~inMax)에서 출력범위(outMin~outMax)로 변환
    private long mapTime(long x, long inMin, long inMax, long outMin, long outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}