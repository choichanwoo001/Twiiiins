package com.twiiiins.dto.request;

import com.twiiiins.validation.ValidMediaUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectImagesUpdateRequest {

    @Size(max = 50)
    private List<@NotBlank @ValidMediaUrl @Size(max = 2048) String> imageUrls = new ArrayList<>();
}
