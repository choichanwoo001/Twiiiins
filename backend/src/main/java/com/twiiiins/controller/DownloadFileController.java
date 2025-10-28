package com.twiiiins.controller;

import com.twiiiins.entity.DownloadFile;
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
    public ResponseEntity<List<DownloadFile>> getAllDownloadFiles() {
        return ResponseEntity.ok(downloadFileService.getAllDownloadFiles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DownloadFile> getDownloadFileById(@PathVariable Long id) {
        return ResponseEntity.ok(downloadFileService.getDownloadFileById(id));
    }
    
    @PostMapping
    public ResponseEntity<DownloadFile> createDownloadFile(@RequestBody DownloadFile downloadFile) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(downloadFileService.createDownloadFile(downloadFile));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DownloadFile> updateDownloadFile(
            @PathVariable Long id,
            @RequestBody DownloadFile downloadFile) {
        return ResponseEntity.ok(downloadFileService.updateDownloadFile(id, downloadFile));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDownloadFile(@PathVariable Long id) {
        downloadFileService.deleteDownloadFile(id);
        return ResponseEntity.noContent().build();
    }
}
