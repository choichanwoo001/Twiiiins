package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String role;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    // 다운로드 URL들은 별도 엔티티로 관리하는 것이 더 나을 수 있지만, 간단하게 하나의 테이블로 관리
    @Column(name = "intro_text", length = 1000)
    private String introText;
    
    @Column(name = "portfolio_en_url")
    private String portfolioEnUrl;
    
    @Column(name = "portfolio_de_url")
    private String portfolioDeUrl;
    
    @Column(name = "portfolio_kr_url")
    private String portfolioKrUrl;
    
    @Column(name = "stage_rider_url")
    private String stageRiderUrl;
    
    @Column(name = "cv_url")
    private String cvUrl;
}

