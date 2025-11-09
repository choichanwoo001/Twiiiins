package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.dto.request.DownloadFileCreateRequest;
import com.twiiiins.dto.request.DownloadFileUpdateRequest;
import com.twiiiins.service.DownloadFileService;
import com.twiiiins.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/download-files")
@RequiredArgsConstructor
@Validated
public class DownloadFileController {
    
    private final DownloadFileService downloadFileService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DownloadFileDto>>> getAllDownloadFiles() {
        return ResponseUtil.listSuccess(downloadFileService.getAllDownloadFiles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DownloadFileDto>> getDownloadFileById(@PathVariable Long id) {
        return ResponseUtil.success(downloadFileService.getDownloadFileById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<DownloadFileDto>> createDownloadFile(@Valid @RequestBody DownloadFileCreateRequest request) {
        return ResponseUtil.created(downloadFileService.createDownloadFile(request), "파일이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DownloadFileDto>> updateDownloadFile(
            @PathVariable Long id,
            @Valid @RequestBody DownloadFileUpdateRequest request) {
        return ResponseUtil.success(downloadFileService.updateDownloadFile(id, request), "파일이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDownloadFile(@PathVariable Long id) {
        downloadFileService.deleteDownloadFile(id);
        return ResponseUtil.deleted("파일이 성공적으로 삭제되었습니다.");
    }
}

