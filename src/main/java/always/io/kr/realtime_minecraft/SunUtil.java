package always.io.kr.realtime_minecraft;

import java.util.Calendar;
import java.util.TimeZone;

public class SunUtil {
    // 서울 좌표 (원하는 지역으로 변경 가능)
    private static final double LATITUDE = 37.5665;
    private static final double LONGITUDE = 126.9780;

    // 간단한 일출/일몰 계산 알고리즘 (NOAA 알고리즘 간소화 버전)
    // type: 1 = 일출, 2 = 일몰
    public static long getSunTime(int type) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        // 대략적인 태양 적위 계산
        double declination = -23.45 * Math.cos(Math.toRadians(360.0 / 365.0 * (dayOfYear + 10)));

        // 시간차 방정식 (Equation of Time) - 정오가 12시 정각에서 얼마나 벗어나는지
        double b = Math.toRadians(360.0 / 364.0 * (dayOfYear - 81));
        double equationOfTime = 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b);

        // 시간각 계산 (일출/일몰 기준 고도 -0.833도)
        double hourAngle = Math.toDegrees(Math.acos(
                (Math.sin(Math.toRadians(-0.833)) - Math.sin(Math.toRadians(LATITUDE)) * Math.sin(Math.toRadians(declination))) /
                        (Math.cos(Math.toRadians(LATITUDE)) * Math.cos(Math.toRadians(declination)))
        ));

        double timeOffset = equationOfTime + (4 * LONGITUDE) - (60 * 9); // GMT+9 보정

        double minutes;
        if (type == 1) { // 일출
            minutes = 720 - (4 * hourAngle) - timeOffset;
        } else { // 일몰
            minutes = 720 + (4 * hourAngle) - timeOffset;
        }

        // 00:00부터 흐른 '초'로 변환
        return (long) (minutes * 60);
    }
}