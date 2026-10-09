package be.url_backend.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62UtilsTest {

    @Test
    @DisplayName("고유 ID를 Base62 7자리 고정 길이로 패딩 인코딩 및 디코딩 검증")
    void testEncodeWithPaddingAndDecode() {
        long id = 1L;
        String encoded = Base62Utils.encodeWithPadding(id, Base62Utils.SHORT_KEY_LENGTH);

        assertThat(encoded).hasSize(7);
        assertThat(encoded).isEqualTo("AAAAAAB");

        long decoded = Base62Utils.decode(encoded);
        assertThat(decoded).isEqualTo(id);
    }

    @Test
    @DisplayName("큰 숫자 ID에 대한 7자리 Base62 인코딩 및 디코딩 검증")
    void testLargeIdEncoding() {
        long id = 125_000_000L;
        String encoded = Base62Utils.encodeWithPadding(id, Base62Utils.SHORT_KEY_LENGTH);

        assertThat(encoded).hasSize(7);
        long decoded = Base62Utils.decode(encoded);
        assertThat(decoded).isEqualTo(id);
    }
}
