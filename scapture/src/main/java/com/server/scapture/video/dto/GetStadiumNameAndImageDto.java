package com.server.scapture.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class GetStadiumNameAndImageDto {
    private String name;
    private String image;
}
