package com.server.scapture.user.controller;

import com.server.scapture.user.service.UserService;
import com.server.scapture.util.response.CustomAPIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;


    // 프로필 조회
    @GetMapping("/progile")
    public ResponseEntity<CustomAPIResponse<?>> getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader){
        return userService.getProfile(authorizationHeader);
    }

    // 버내너 잔액 조회
    @GetMapping("/bananas")
    public ResponseEntity<CustomAPIResponse<?>> searchBananas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader){
        return userService.getBananaBalance(authorizationHeader);
    }

    // 버내너 충전
    @PostMapping("/bananas")
    public ResponseEntity<CustomAPIResponse<?>> addBananas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody int balance) {
        return userService.addBananas(authorizationHeader, balance);
    }
}
