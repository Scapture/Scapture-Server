package com.server.scapture.video.service;

import com.server.scapture.domain.*;
import com.server.scapture.field.repository.FieldRepository;
import com.server.scapture.oauth.jwt.JwtUtil;
import com.server.scapture.schedule.repository.ScheduleRepository;
import com.server.scapture.stadium.repository.StadiumRepository;
import com.server.scapture.store.repository.StoreRepository;
import com.server.scapture.util.response.CustomAPIResponse;
import com.server.scapture.video.dto.*;
import com.server.scapture.video.repository.VideoRepository;
import com.server.scapture.videoLike.repository.VideoLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService{
    private final VideoRepository videoRepository;
    private final ScheduleRepository scheduleRepository;
    private final FieldRepository fieldRepository;
    private final StadiumRepository stadiumRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final StoreRepository storeRepository;
    private final JwtUtil jwtUtil;
    @Override
    public ResponseEntity<CustomAPIResponse<?>> createVideo(VideoCreateRequestDto videoCreateRequestDto) {
        // 1. 운영 일정 조회
        Optional<Schedule> foundSchedule = scheduleRepository.findById(videoCreateRequestDto.getScheduleId());
        // 1-1. 실패
        if (foundSchedule.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 운영 시간입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        Schedule schedule = foundSchedule.get();
        // 2. 영상 생성
        for (VideoCreateDetailDto videoCreateDetailDto : videoCreateRequestDto.getData()) {
            Video video = Video.builder()
                    .schedule(schedule)
                    .name(videoCreateDetailDto.getName())
                    .image(videoCreateDetailDto.getImage())
                    .video(videoCreateDetailDto.getVideo())
                    .likeCount(0)
                    .build();
            videoRepository.save(video);
        }

        CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(), "영상 등록이 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getVideos(Long scheduleId) {
        // 1. Schedule 조회
        Optional<Schedule> foundSchedule = scheduleRepository.findById(scheduleId);
        // 1-1. 실패
        if (foundSchedule.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 운영 일정입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        Schedule schedule = foundSchedule.get();
        // 2. 영상 조회
        List<Video> videoList = videoRepository.findBySchedule(schedule);
        // 3. 경기장 조회
        // 3-1. 구장 조회
        Field field = fieldRepository.findById(schedule.getField().getId()).get();
        // 3-2. 경기장 조회
        Stadium stadium = stadiumRepository.findById(field.getStadium().getId()).get();
        // 4. Response
        // 4-1. data
        List<GetVideosResponseDto> data = null;
        if (!videoList.isEmpty()) {
            data = new ArrayList<>();
            for (Video video : videoList) {
                GetVideosResponseDto responseDto = GetVideosResponseDto.builder()
                        .videoId(video.getId())
                        .name(video.getName())
                        .image(video.getImage())
                        .stadiumName(stadium.getName())
                        .date(schedule.convertMonthAndDay())
                        .hours(schedule.convertHourAndMin())
                        .build();
                data.add(responseDto);
            }
        }
        // 4-2. responseBody
        CustomAPIResponse<List<GetVideosResponseDto>> responseBody = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), data, "경기 영상 조회 완료되었습니다.");
        // 4-3. ResponseEntity
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getVideosByLikeCount() {
        // 1. Response
        List<Video> videoList = videoRepository.findTop10ByOrderByLikeCountDesc();
        // 1-1. data
        List<GetVideosByLikeCountResponseDto> data = new ArrayList<>();
        for (Video video : videoList) {
            Schedule schedule = scheduleRepository.findById(video.getSchedule().getId()).get();
            Field field = fieldRepository.findById(schedule.getField().getId()).get();
            Stadium stadium = stadiumRepository.findById(field.getStadium().getId()).get();

            GetVideosByLikeCountResponseDto responseDto = GetVideosByLikeCountResponseDto.builder()
                    .videoId(video.getId())
                    .name(video.getName())
                    .image(video.getImage())
                    .stadiumName(stadium.getName())
                    .date(schedule.convertMonthAndDay())
                    .likeCount(video.getLikeCount())
                    .build();
            data.add(responseDto);
        }
        // 1-2. responseBody
        CustomAPIResponse<List<GetVideosByLikeCountResponseDto>> responseBody = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), data, "인기 동영상 조회 완료되었습니다.");
        // 1-3. ResponseEntity
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getStoredVideo(String header, String sort) {
        // 1. 사용자 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 1-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        User user = foundUser.get();
        // 2. 저장한 영상 가져오기
        List<Store> storeList = storeRepository.findByUser(user);
        // 2-1. 없는 경우
        if (storeList.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.OK.value(), "저장 영상 조회 완료되었습니다.");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(responseBody);
        }
        // 3. Video 조회
        List<Video> videoList = new ArrayList<>();
        for (Store store : storeList) {
            Optional<Video> foundVideo = videoRepository.findById(store.getVideo().getId());
            // 3-1. video 조회 실패
            if (foundVideo.isEmpty()) {
                CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(responseBody);
            }
            // 3-2. 성공
            videoList.add(foundVideo.get());
        }
        // 4. 정렬
        if (sort.equals("latest")) {
            videoList.sort(new Comparator<Video>() {
                @Override
                public int compare(Video o1, Video o2) {
                    Schedule schedule1 = scheduleRepository.findById(o1.getSchedule().getId()).get();
                    Schedule schedule2 = scheduleRepository.findById(o2.getSchedule().getId()).get();
                    return schedule2.getEndDate().compareTo(schedule1.getEndDate());
                }
            });
        } else {
            videoList.sort(new Comparator<Video>() {
                @Override
                public int compare(Video o1, Video o2) {
                    return o2.getLikeCount() - o1.getLikeCount();
                }
            });
        }
        // 5. Response
        // 5-1. data
        List<GetStoredVideoResponseDto> data = new ArrayList<>();
        for (Video video : videoList) {
            GetStoredVideoResponseDto responseDto = GetStoredVideoResponseDto.builder()
                    .videoId(video.getId())
                    .image(video.getImage())
                    .build();
            data.add(responseDto);
        }
        // 5-2. responseBody
        CustomAPIResponse<List<GetStoredVideoResponseDto>> responseBody = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), data, "저장 영상 조회 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getVideoDetail(String header, Long videoId) {
        // 1. video 조회
        Optional<Video> foundVideo = videoRepository.findById(videoId);
        // 1-1. 실패
        if (foundVideo.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        Video video = foundVideo.get();
        // 2. User 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 2-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 2-2. 성공
        User user = foundUser.get();
        // 3. Stadium 조회
        Schedule schedule = scheduleRepository.findById(video.getSchedule().getId()).get();
        Field field = fieldRepository.findById(schedule.getField().getId()).get();
        Stadium stadium = stadiumRepository.findById(field.getStadium().getId()).get();
        // 4. 좋아요 여부 조회
        boolean isLiked = videoLikeRepository.findByVideoAndUser(video, user).isPresent();
        // 5. 저장 여부 조회
        boolean isStored = storeRepository.findByVideoAndUser(video, user).isPresent();
        // 6. Response
        // 6-1. data
        GetVideoDetailResponseDto data = GetVideoDetailResponseDto.builder()
                .name(video.getName())
                .image(video.getImage())
                .video(video.getVideo())
                .stadiumName(stadium.getName())
                .isLiked(isLiked)
                .isStored(isStored)
                .build();
        // 6-2. responseBody
        CustomAPIResponse<GetVideoDetailResponseDto> responseBody = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), data, "영상 세부 조회 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> createLike(String header, Long videoId) {
        // 1. User 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 1-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        User user = foundUser.get();
        // 2. Video 조회
        Optional<Video> foundVideo = videoRepository.findById(videoId);
        // 2-1. 실패
        if (foundVideo.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 2-2. 성공
        Video video = foundVideo.get();
        // 3. 영상_좋아요 저장
        // 3-1. 중복 데이터 검사
        Optional<VideoLike> foundVideoLike = videoLikeRepository.findByVideoAndUser(video, user);
        if (foundVideoLike.isPresent()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.CONFLICT.value(), "이미 존재하는 영상 좋아요입니다.");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(responseBody);
        }
        // 3-2. 영상_좋아요 생성
        VideoLike videoLike = VideoLike.builder()
                .video(video)
                .user(user)
                .build();
        // 3-3. 저장
        videoLikeRepository.save(videoLike);
        // 4. 영상 좋아요 증가
        video.increaseLikeCount();
        videoRepository.save(video);
        // 5. Response
        // 5-1. Response
        CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(), "영상 좋아요 추가 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> deleteLike(String header, Long videoId) {
        // 1. User 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 1-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        User user = foundUser.get();
        // 2. Video 조회
        Optional<Video> foundVideo = videoRepository.findById(videoId);
        // 2-1. 실패
        if (foundVideo.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 2-2. 성공
        Video video = foundVideo.get();
        // 3. 영상_좋아요 해제
        // 3-1. 데이터 유무 검사
        Optional<VideoLike> foundVideoLike = videoLikeRepository.findByVideoAndUser(video, user);
        if (foundVideoLike.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상 좋아요입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 3-2. 영상_좋아요 탐색
        VideoLike videoLike = foundVideoLike.get();
        // 3-3. 삭제
        videoLikeRepository.delete(videoLike);
        // 4. 영상 좋아요 감소
        video.decreaseLikeCount();
        videoRepository.save(video);
        // 5. Response
        // 5-1. Response
        CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.NO_CONTENT.value(), "영상 좋아요 해제 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> createStore(String header, Long videoId) {
        // 1. 사용자 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 1-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        User user = foundUser.get();
        // 2. 영상 조회
        Optional<Video> foundVideo = videoRepository.findById(videoId);
        // 2-1. 실패
        if (foundVideo.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 2-2. 성공
        Video video = foundVideo.get();
        // 3. Store 생성
        // 3-1. 중복 검사
        Optional<Store> foundStore = storeRepository.findByVideoAndUser(video, user);
        if (foundStore.isPresent()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.CONFLICT.value(), "존재하는 저장입니다.");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(responseBody);
        }
        // 3-2. Store 생성
        Store store = Store.builder()
                .user(user)
                .video(video)
                .build();
        // 3-3. 저장
        storeRepository.save(store);
        // 4. response
        CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(), "영상 저장 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }
    @Override
    public ResponseEntity<CustomAPIResponse<?>> deleteStore(String header, Long videoId) {
        // 1. 사용자 조회
        Optional<User> foundUser = jwtUtil.findUserByJwtToken(header);
        // 1-1. 실패
        if (foundUser.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 사용자입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 1-2. 성공
        User user = foundUser.get();
        // 2. 영상 조회
        Optional<Video> foundVideo = videoRepository.findById(videoId);
        // 2-1. 실패
        if (foundVideo.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 영상입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 2-2. 성공
        Video video = foundVideo.get();
        // 3. Store 객체 생성
        // 3-1. 중복 검사
        Optional<Store> foundStore = storeRepository.findByVideoAndUser(video, user);
        if (foundStore.isEmpty()) {
            CustomAPIResponse<Object> responseBody = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "존재하지 않는 저장입니다.");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(responseBody);
        }
        // 3-2. Store 가져오기
        Store store = foundStore.get();
        // 3-3. Store 삭제
        storeRepository.delete(store);
        // 4. Response
        CustomAPIResponse<Object> responseBody = CustomAPIResponse.createSuccessWithoutData(HttpStatus.NO_CONTENT.value(), "저장 삭제 완료되었습니다.");
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(responseBody);
    }
}
