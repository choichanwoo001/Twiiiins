package com.twiiiins.translation;

import com.twiiiins.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.util.*;

@RestController @RequestMapping("/api/admin/translations") @RequiredArgsConstructor @Validated
public class TranslationController {
    private final TranslationService service;
    private final TranslationSettings settings;
    @GetMapping public ApiResponse<?> list(@RequestParam(required = false) String kind,
        @RequestParam(required = false) Long contentId, @RequestParam(defaultValue = "0") @Min(0) int page) {
        return ApiResponse.success(Map.of("available", settings.available(), "items", service.list(kind, contentId, page)));
    }
    @PostMapping("/{id}/retry") public ApiResponse<?> retry(@PathVariable Long id) { return ApiResponse.success(service.retry(id)); }
    public record Edit(@NotNull Long version, @NotBlank @Size(max = 100000) String en, @NotBlank @Size(max = 100000) String de) {}
    @PutMapping("/{id}") public ApiResponse<?> edit(@PathVariable Long id, @Valid @RequestBody Edit edit) {
        return ApiResponse.success(service.edit(id, edit.version(), edit.en(), edit.de()));
    }
}
