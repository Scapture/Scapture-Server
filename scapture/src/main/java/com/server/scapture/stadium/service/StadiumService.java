package com.server.scapture.stadium.service;


import com.server.scapture.stadium.dto.CreateStadiumRequestDto;
import com.server.scapture.util.response.CustomAPIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StadiumService {
    ResponseEntity<CustomAPIResponse<?>> createStadium(CreateStadiumRequestDto data, List<MultipartFile> images) throws IOException;
}