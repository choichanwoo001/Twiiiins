package com.twiiiins.controller;

import com.twiiiins.entity.Media;
import com.twiiiins.entity.Order;
import com.twiiiins.entity.Product;
import com.twiiiins.service.MediaService;
import com.twiiiins.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final MediaService mediaService;

    // 상품 관리
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // 미디어 관리
    @PostMapping("/media")
    public ResponseEntity<Media> createMedia(@Valid @RequestBody Media media) {
        return ResponseEntity.ok(mediaService.createMedia(media));
    }

    @PutMapping("/media/{id}")
    public ResponseEntity<Media> updateMedia(@PathVariable Long id, @Valid @RequestBody Media media) {
        return ResponseEntity.ok(mediaService.updateMedia(id, media));
    }

    @DeleteMapping("/media/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok().build();
    }
}

