package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.PhotoGroupDto;
import com.twiiiins.service.FileUploadService;
import com.twiiiins.service.PhotoService;
import com.twiiiins.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "사진 관리", description = "사진 및 사진 그룹 관리 API")
public class PhotoController {
    
    private final PhotoService photoService;
    private final FileUploadService fileUploadService;
    
    // PhotoGroup endpoints
    @GetMapping({"/photos/groups", "/photo-groups"})
    @Operation(summary = "사진 그룹 목록 조회", description = "모든 사진 그룹을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<PhotoGroupDto>>> getAllPhotoGroups(
            @RequestParam(required = false) String title) {
        List<PhotoGroupDto> groups;
        if (title != null) {
            groups = photoService.getPhotoGroupsWithFilters(title);
        } else {
            groups = photoService.getAllPhotoGroups();
        }
        return ResponseUtil.listSuccess(groups, "사진 그룹 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/photos/groups/{id}")
    @Operation(summary = "사진 그룹 상세 조회", description = "특정 사진 그룹의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<PhotoGroupDto>> getPhotoGroupById(@PathVariable Long id) {
        PhotoGroupDto group = photoService.getPhotoGroupById(id);
        return ResponseUtil.success(group, "사진 그룹 정보를 성공적으로 조회했습니다.");
    }
    
    @PostMapping("/photos/groups")
    @Operation(summary = "사진 그룹 생성", description = "새로운 사진 그룹을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 생성됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<PhotoGroupDto>> createPhotoGroup(@RequestBody PhotoGroupDto photoGroupDto) {
        PhotoGroupDto createdGroup = photoService.createPhotoGroup(photoGroupDto);
        return ResponseUtil.created(createdGroup, "사진 그룹이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/photos/groups/{id}")
    @Operation(summary = "사진 그룹 수정", description = "기존 사진 그룹 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<PhotoGroupDto>> updatePhotoGroup(
            @PathVariable Long id,
            @RequestBody PhotoGroupDto photoGroupDto) {
        PhotoGroupDto updatedGroup = photoService.updatePhotoGroup(id, photoGroupDto);
        return ResponseUtil.success(updatedGroup, "사진 그룹이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/photos/groups/{id}")
    @Operation(summary = "사진 그룹 삭제", description = "사진 그룹을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> deletePhotoGroup(@PathVariable Long id) {
        photoService.deletePhotoGroup(id);
        return ResponseUtil.deleted("사진 그룹이 성공적으로 삭제되었습니다.");
    }
    
    // Photo endpoints
    @GetMapping("/photos/groups/{groupId}/photos")
    @Operation(summary = "그룹별 사진 조회", description = "특정 그룹의 사진 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<PhotoDto>>> getPhotosByGroupId(@PathVariable Long groupId) {
        List<PhotoDto> photos = photoService.getPhotosByGroupId(groupId);
        return ResponseUtil.listSuccess(photos, "그룹별 사진 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/photos/{id}")
    @Operation(summary = "사진 상세 조회", description = "특정 사진의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<PhotoDto>> getPhotoById(@PathVariable Long id) {
        PhotoDto photo = photoService.getPhotoById(id);
        return ResponseUtil.success(photo, "사진 정보를 성공적으로 조회했습니다.");
    }
    
    @PostMapping("/photos/groups/{groupId}/photos")
    @Operation(summary = "사진 생성", description = "새로운 사진을 생성합니다. (파일 업로드 지원)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 생성됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<PhotoDto>>> createPhoto(
            @PathVariable Long groupId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "altText", required = false) String altText) {
        
        List<PhotoDto> createdPhotos = new ArrayList<>();
        
        // 파일 업로드가 있는 경우
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                // 파일과 썸네일을 S3에 업로드
                var uploadResponse = fileUploadService.uploadImageWithThumbnail(file);
                String imageUrl = uploadResponse.getUrl();
                String thumbnailUrl = uploadResponse.getThumbnailUrl();
                
                // PhotoDto 생성
                PhotoDto newPhotoDto = new PhotoDto();
                newPhotoDto.setImageUrl(imageUrl);
                newPhotoDto.setThumbnailUrl(thumbnailUrl);
                if (altText != null) {
                    newPhotoDto.setAltText(altText);
                }
                
                // 사진 생성
                PhotoDto createdPhoto = photoService.createPhoto(groupId, newPhotoDto);
                createdPhotos.add(createdPhoto);
            }
        }
        
        return ResponseUtil.created(createdPhotos, "사진이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/photos/{id}")
    @Operation(summary = "사진 수정", description = "기존 사진 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<PhotoDto>> updatePhoto(
            @PathVariable Long id,
            @RequestBody PhotoDto photoDto) {
        PhotoDto updatedPhoto = photoService.updatePhoto(id, photoDto);
        return ResponseUtil.success(updatedPhoto, "사진이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/photos/{id}")
    @Operation(summary = "사진 삭제", description = "사진을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseUtil.deleted("사진이 성공적으로 삭제되었습니다.");
    }
}
