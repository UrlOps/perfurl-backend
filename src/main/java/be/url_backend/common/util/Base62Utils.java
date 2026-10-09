package be.url_backend.common.util;

import java.security.SecureRandom;
import java.util.Random;

public class Base62Utils {

    private static final char[] BASE62_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    public static final int SHORT_KEY_LENGTH = 7;
    private static final int BASE = 62;
    private static final Random RANDOM = new SecureRandom();

    /**
     * 지정된 길이의 무작위 Base62 문자열을 생성합니다.
     * @return Base62로 인코딩된 무작위 문자열
     */
    public static String generateShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(BASE62_CHARS[RANDOM.nextInt(BASE62_CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * long 타입의 숫자를 Base62 문자열로 인코딩합니다.
     * @param number 인코딩할 숫자 (e.g., 데이터베이스 ID)
     * @return Base62로 인코딩된 문자열
     */
    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62_CHARS[(int) (number % BASE)]);
            number /= BASE;
        }

        return sb.reverse().toString();
    }

    /**
     * long 타입의 숫자를 지정된 최소 길이를 가진 Base62 문자열로 인코딩합니다.
     * 부족한 자릿수는 앞자리를 0에 해당하는 첫 번째 문자로 패딩합니다.
     * @param number 인코딩할 숫자
     * @param minLength 최소 문자열 길이
     * @return 패딩된 Base62 문자열
     */
    public static String encodeWithPadding(long number, int minLength) {
        String encoded = encode(number);
        if (encoded.length() >= minLength) {
            return encoded;
        }
        return String.valueOf(BASE62_CHARS[0]).repeat(minLength - encoded.length()) + encoded;
    }

    /**
     * Base62 문자열을 long 타입의 숫자로 디코딩합니다.
     * @param str 디코딩할 Base62 문자열
     * @return 디코딩된 숫자
     */
    public static long decode(String str) {
        long result = 0;
        long power = 1;
        for (int i = str.length() - 1; i >= 0; i--) {
            int digit = new String(BASE62_CHARS).indexOf(str.charAt(i));
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid Base62 character in string: " + str);
            }
            result += digit * power;
            power *= BASE;
        }
        return result;
    }
}
