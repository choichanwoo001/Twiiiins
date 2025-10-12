package com.twiiiins.service;

import com.twiiiins.entity.Product;
import com.twiiiins.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByActiveTrueAndCategory(category);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());
        product.setActive(productDetails.getActive());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);  // 소프트 삭제
        productRepository.save(product);
    }

    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStock() - quantity;
        
        if (newStock < 0) {
            throw new RuntimeException("재고가 부족합니다");
        }
        
        product.setStock(newStock);
        productRepository.save(product);
    }
}

