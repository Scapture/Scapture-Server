package com.server.scapture.user.service;

import com.server.scapture.subscribe.dto.CreateSubscribeRequestDto;
import com.server.scapture.util.response.CustomAPIResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    // 버내너 잔액 조회
    ResponseEntity<CustomAPIResponse<?>> getBananaBalance(String authorizationHeader);

    //  버내너 충전
    ResponseEntity<CustomAPIResponse<?>> addBananas(String authorizationHeader, int balance);

    //프로필 조회
    ResponseEntity<CustomAPIResponse<?>> getProfile(String authorizationHeader);

    // 구독 생성
    ResponseEntity<CustomAPIResponse<?>> createSubscribe(String authorizationHeader, CreateSubscribeRequestDto createSubscribeRequestDto);

    // 구독 갱신
    ResponseEntity<CustomAPIResponse<?>> extensionSubscribe(String authorizationHeader);

    // 예약 내역 조회
    ResponseEntity<CustomAPIResponse<?>> searchReservations(String authorizationHeader);
}
