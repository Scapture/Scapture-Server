package com.server.scapture.user.dto;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getName();
}