package com.server.scapture.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class GetVideoDetailResponseDto {
    private String name;
    private String image;
    private String video;
    private String stadiumName;
    private Boolean isLiked;
    private Boolean isStored;
    private int likeCount;
    private int views;
    private GetStadiumInfoDto stadium;
}
