package com.server.scapture.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class GetStadiumInfoDto {
    private String name;
    private String description;
    private String location;
    private Boolean isOutside;
    private String parking;
    private String image;
}
