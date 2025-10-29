package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoGroupDto {
    private Long id;
    private String title;
    private Integer displayOrder;
    private List<PhotoDto> photos;
}
