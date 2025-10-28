package com.twiiiins.controller;

import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.service.DownloadFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/download-files")
@RequiredArgsConstructor
public class DownloadFileController {
    
    private final DownloadFileService downloadFileService;
    
    @GetMapping
    public ResponseEntity<List<DownloadFileDto>> getAllDownloadFiles() {
        return ResponseEntity.ok(downloadFileService.getAllDownloadFiles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DownloadFileDto> getDownloadFileById(@PathVariable Long id) {
        return ResponseEntity.ok(downloadFileService.getDownloadFileById(id));
    }
    
    @PostMapping
    public ResponseEntity<DownloadFileDto> createDownloadFile(@RequestBody DownloadFileDto downloadFileDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(downloadFileService.createDownloadFile(downloadFileDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DownloadFileDto> updateDownloadFile(
            @PathVariable Long id,
            @RequestBody DownloadFileDto downloadFileDto) {
        return ResponseEntity.ok(downloadFileService.updateDownloadFile(id, downloadFileDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDownloadFile(@PathVariable Long id) {
        downloadFileService.deleteDownloadFile(id);
        return ResponseEntity.noContent().build();
    }
}
