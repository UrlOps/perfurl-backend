package be.url_backend.common.util;

public class UserAgentAnalyzer {

    public record ClientInsight(String browser, String os, String deviceType) {}

    public static ClientInsight analyze(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new ClientInsight("Unknown", "Unknown", "Unknown");
        }

        String ua = userAgent.toLowerCase();

        // 1. Device Type
        String deviceType;
        if (ua.contains("tablet") || ua.contains("ipad")) {
            deviceType = "Tablet";
        } else if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android")) {
            deviceType = "Mobile";
        } else if (ua.contains("bot") || ua.contains("crawl") || ua.contains("spider")) {
            deviceType = "Bot/Crawler";
        } else {
            deviceType = "Desktop";
        }

        // 2. OS
        String os;
        if (ua.contains("windows")) {
            os = "Windows";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            os = "iOS";
        } else if (ua.contains("mac os") || ua.contains("macintosh")) {
            os = "macOS";
        } else if (ua.contains("android")) {
            os = "Android";
        } else if (ua.contains("linux")) {
            os = "Linux";
        } else {
            os = "Other";
        }

        // 3. Browser
        String browser;
        if (ua.contains("whale")) {
            browser = "Naver Whale";
        } else if (ua.contains("samsungbrowser")) {
            browser = "Samsung Internet";
        } else if (ua.contains("edg/") || ua.contains("edge/")) {
            browser = "Edge";
        } else if (ua.contains("opr/") || ua.contains("opera")) {
            browser = "Opera";
        } else if (ua.contains("chrome") || ua.contains("crios")) {
            browser = "Chrome";
        } else if (ua.contains("firefox") || ua.contains("fxios")) {
            browser = "Firefox";
        } else if (ua.contains("safari")) {
            browser = "Safari";
        } else if (ua.contains("trident") || ua.contains("msie")) {
            browser = "Internet Explorer";
        } else {
            browser = "Other";
        }

        return new ClientInsight(browser, os, deviceType);
    }
}
