package com.server.scapture.stadium.dto;

import com.server.scapture.field.dto.SimpleFieldResponseDto;
import com.server.scapture.image.dto.SimpleImageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Builder
public class GetStadiumDetailDto {
    private String name;
    private String description;
    private String location;
    private Boolean isOutside;
    private String parking;
    private String hours;
    private List<SimpleImageResponseDto> images;
    private List<SimpleFieldResponseDto> fields;
}
