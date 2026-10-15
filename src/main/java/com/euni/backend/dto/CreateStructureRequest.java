package com.euni.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStructureRequest {
    private Long parentId;
    
    @NotBlank(message = "Mã khối kiến thức không được để trống")
    private String code;
    
    @NotBlank(message = "Tên khối kiến thức không được để trống")
    private String name;
    
    @NotNull(message = "Số tín chỉ yêu cầu không được để trống")
    @Min(value = 1, message = "Số tín chỉ yêu cầu phải lớn hơn 0")
    private Integer creditRequired;
    
    private Integer orderIndex;
}
