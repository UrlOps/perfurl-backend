package be.url_backend.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentAnalyzerTest {

    @Test
    @DisplayName("Windows 환경의 Chrome 브라우저 분석 검증")
    void testWindowsChrome() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(ua);

        assertThat(insight.browser()).isEqualTo("Chrome");
        assertThat(insight.os()).isEqualTo("Windows");
        assertThat(insight.deviceType()).isEqualTo("Desktop");
    }

    @Test
    @DisplayName("iPhone 환경의 Safari 브라우저 분석 검증")
    void testIphoneSafari() {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1";
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(ua);

        assertThat(insight.browser()).isEqualTo("Safari");
        assertThat(insight.os()).isEqualTo("iOS");
        assertThat(insight.deviceType()).isEqualTo("Mobile");
    }

    @Test
    @DisplayName("Android 환경의 Samsung Internet 브라우저 분석 검증")
    void testAndroidSamsungInternet() {
        String ua = "Mozilla/5.0 (Linux; Android 14; SAMSUNG SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/23.0 Chrome/115.0.5790.166 Mobile Safari/537.36";
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(ua);

        assertThat(insight.browser()).isEqualTo("Samsung Internet");
        assertThat(insight.os()).isEqualTo("Android");
        assertThat(insight.deviceType()).isEqualTo("Mobile");
    }

    @Test
    @DisplayName("Mac 환경의 Edge 브라우저 분석 검증")
    void testMacEdge() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(ua);

        assertThat(insight.browser()).isEqualTo("Edge");
        assertThat(insight.os()).isEqualTo("macOS");
        assertThat(insight.deviceType()).isEqualTo("Desktop");
    }

    @Test
    @DisplayName("iPad 환경의 태블릿 분석 검증")
    void testIpadTablet() {
        String ua = "Mozilla/5.0 (iPad; CPU OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1";
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(ua);

        assertThat(insight.deviceType()).isEqualTo("Tablet");
        assertThat(insight.os()).isEqualTo("iOS");
    }

    @Test
    @DisplayName("null 또는 빈 문자열 처리 검증")
    void testEmptyUserAgent() {
        UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(null);
        assertThat(insight.browser()).isEqualTo("Unknown");
        assertThat(insight.os()).isEqualTo("Unknown");
        assertThat(insight.deviceType()).isEqualTo("Unknown");

        UserAgentAnalyzer.ClientInsight blankInsight = UserAgentAnalyzer.analyze("   ");
        assertThat(blankInsight.browser()).isEqualTo("Unknown");
    }
}
