package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.NewsDto;
import com.twiiiins.service.NewsService;
import com.twiiiins.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/news")
@RequiredArgsConstructor
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsDto>>> getAllNews() {
        return ResponseUtil.listSuccess(newsService.getAllNews());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsDto>> getNewsById(@PathVariable Long id) {
        return ResponseUtil.success(newsService.getNewsById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<NewsDto>> createNews(@RequestBody NewsDto newsDto) {
        return ResponseUtil.created(newsService.createNews(newsDto), "뉴스가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsDto>> updateNews(
            @PathVariable Long id,
            @RequestBody NewsDto newsDto) {
        return ResponseUtil.success(newsService.updateNews(id, newsDto), "뉴스가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseUtil.deleted("뉴스가 성공적으로 삭제되었습니다.");
    }
}
