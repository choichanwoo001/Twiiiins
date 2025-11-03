package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.dto.NewsDto;
import com.twiiiins.service.FileUploadService;
import com.twiiiins.service.NewsService;
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
@RequestMapping("/api/media/news")
@RequiredArgsConstructor
@Tag(name = "뉴스 관리", description = "뉴스 관리 API")
public class NewsController {
    
    private final NewsService newsService;
    private final FileUploadService fileUploadService;
    
    @GetMapping
    @Operation(summary = "모든 뉴스 조회", description = "전체 뉴스 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<NewsDto>>> getAllNews(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<NewsDto> newsList;
        if (title != null || startDate != null || endDate != null) {
            java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
            java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;
            newsList = newsService.getNewsWithFilters(title, start, end);
        } else {
            newsList = newsService.getAllNews();
        }
        return ResponseUtil.listSuccess(newsList);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "뉴스 상세 조회", description = "특정 뉴스의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<NewsDto>> getNewsById(@PathVariable Long id) {
        return ResponseUtil.success(newsService.getNewsById(id));
    }
    
    @PostMapping
    @Operation(summary = "뉴스 생성", description = "새로운 뉴스를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 생성됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<NewsDto>> createNews(@RequestBody NewsDto newsDto) {
        return ResponseUtil.created(newsService.createNews(newsDto), "뉴스가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "뉴스 수정", description = "기존 뉴스 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<NewsDto>> updateNews(
            @PathVariable Long id,
            @RequestBody NewsDto newsDto) {
        return ResponseUtil.success(newsService.updateNews(id, newsDto), "뉴스가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "뉴스 삭제", description = "뉴스를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseUtil.deleted("뉴스가 성공적으로 삭제되었습니다.");
    }
    
    @PostMapping("/{id}/images")
    @Operation(summary = "뉴스 사진 업로드", description = "뉴스에 사진을 업로드합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 업로드됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<NewsDto>> uploadNewsImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        
        NewsDto news = newsService.getNewsById(id);
        List<String> imageUrls = news.getImageUrls() != null ? new ArrayList<>(news.getImageUrls()) : new ArrayList<>();
        
        // 파일 업로드
        for (MultipartFile file : files) {
            FileUploadResponseDto uploadResponse = fileUploadService.uploadImage(file);
            imageUrls.add(uploadResponse.getUrl());
        }
        
        // NewsDto 업데이트
        news.setImageUrls(imageUrls);
        NewsDto updatedNews = newsService.updateNews(id, news);
        
        return ResponseUtil.success(updatedNews, "사진이 성공적으로 업로드되었습니다.");
    }
}
