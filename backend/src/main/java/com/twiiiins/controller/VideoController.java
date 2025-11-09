package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.VideoDto;
import com.twiiiins.dto.request.VideoCreateRequest;
import com.twiiiins.dto.request.VideoUpdateRequest;
import com.twiiiins.service.VideoService;
import com.twiiiins.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/videos")
@RequiredArgsConstructor
@Validated
@Tag(name = "동영상 관리", description = "동영상 관리 API")
public class VideoController {
    
    private final VideoService videoService;
    
    @GetMapping
    @Operation(summary = "모든 동영상 조회", description = "전체 동영상 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<VideoDto>>> getAllVideos(
            @RequestParam(required = false) @Size(min = 1, max = 255) String title) {
        List<VideoDto> videos;
        if (title != null) {
            videos = videoService.getVideosWithFilters(title);
        } else {
            videos = videoService.getAllVideos();
        }
        return ResponseUtil.listSuccess(videos, "동영상 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "동영상 상세 조회", description = "특정 동영상의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<VideoDto>> getVideoById(@PathVariable @NonNull Long id) {
        VideoDto video = videoService.getVideoById(id);
        return ResponseUtil.success(video, "동영상 정보를 성공적으로 조회했습니다.");
    }
    
    @PostMapping
    @Operation(summary = "동영상 생성", description = "새로운 동영상을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 생성됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<VideoDto>> createVideo(@Valid @RequestBody @NonNull VideoCreateRequest request) {
        VideoDto createdVideo = videoService.createVideo(request);
        return ResponseUtil.created(createdVideo, "동영상이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "동영상 수정", description = "기존 동영상 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<VideoDto>> updateVideo(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull VideoUpdateRequest request) {
        VideoDto updatedVideo = videoService.updateVideo(id, request);
        return ResponseUtil.success(updatedVideo, "동영상이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "동영상 삭제", description = "동영상을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable @NonNull Long id) {
        videoService.deleteVideo(id);
        return ResponseUtil.deleted("동영상이 성공적으로 삭제되었습니다.");
    }
}

