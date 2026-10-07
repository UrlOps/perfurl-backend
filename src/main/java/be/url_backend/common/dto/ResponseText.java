package be.url_backend.common.dto;

import lombok.Getter;

@Getter
public enum ResponseText {

    // admin
    ADMIN_SIGNUP_SUCCESS("회원가입 성공"),
    ADMIN_LOGIN_SUCCESS("로그인 성공"),

    // url
    URL_CREATE_SUCCESS("단축 URL 생성 성공"),
    URL_LIST_FETCH_SUCCESS("모든 URL 조회 성공"),
    URL_INFO_FETCH_SUCCESS("URL 정보 조회 성공"),
    URL_DELETE_SUCCESS("URL 삭제 성공"),
    URL_STATS_FETCH_SUCCESS("URL 통계 조회 성공"),
    URL_DAILY_STATS_FETCH_SUCCESS("일별 통계 조회 성공"),
    URL_ANALYTICS_FETCH_SUCCESS("단축 URL 분석 통계 조회 성공");

    private final String msg;

    ResponseText(String msg) {
        this.msg = msg;
    }

    public String format(Object... args) {
        return String.format(this.msg, args);
    }
} 