package com.server.scapture.user.dto;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo
{

    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
