package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.ConcertDto;
import com.twiiiins.service.ConcertService;
import com.twiiiins.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
@Tag(name = "공연 관리", description = "공연 정보 관리 API")
public class ConcertController {
    
    private final ConcertService concertService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConcertDto>>> getAllConcerts() {
        List<ConcertDto> concerts = concertService.getAllConcerts();
        return ResponseUtil.listSuccess(concerts, "공연 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ConcertDto>>> getUpcomingConcerts() {
        List<ConcertDto> concerts = concertService.getUpcomingConcerts();
        return ResponseUtil.listSuccess(concerts, "예정 공연 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<ConcertDto>>> getPastConcerts() {
        List<ConcertDto> concerts = concertService.getPastConcerts();
        return ResponseUtil.listSuccess(concerts, "과거 공연 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "공연 상세 조회", description = "특정 공연의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<ConcertDto>> getConcertById(@PathVariable Long id) {
        ConcertDto concert = concertService.getConcertById(id);
        return ResponseUtil.success(concert, "공연 정보를 성공적으로 조회했습니다.");
    }
    
    @PostMapping
    @Operation(summary = "공연 생성", description = "새로운 공연을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 생성됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<ConcertDto>> createConcert(@RequestBody ConcertDto concertDto) {
        ConcertDto createdConcert = concertService.createConcert(concertDto);
        return ResponseUtil.created(createdConcert, "공연이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "공연 수정", description = "기존 공연 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<ConcertDto>> updateConcert(
            @PathVariable Long id,
            @RequestBody ConcertDto concertDto) {
        ConcertDto updatedConcert = concertService.updateConcert(id, concertDto);
        return ResponseUtil.success(updatedConcert, "공연이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "공연 삭제", description = "공연을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
        return ResponseUtil.deleted("공연이 성공적으로 삭제되었습니다.");
    }
    
    @PutMapping("/{id}/move-to-past")
    @Operation(summary = "공연을 과거로 이동", description = "공연을 과거 공연으로 이동시킵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 이동됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<ConcertDto>> moveToPastEvent(@PathVariable Long id) {
        ConcertDto movedConcert = concertService.moveToPastEvent(id);
        return ResponseUtil.success(movedConcert, "공연이 과거 공연으로 이동되었습니다.");
    }
    
    @PutMapping("/{id}/move-to-upcoming")
    @Operation(summary = "공연을 예정으로 이동", description = "공연을 예정 공연으로 이동시킵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 이동됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<ConcertDto>> moveToUpcomingEvent(@PathVariable Long id) {
        ConcertDto movedConcert = concertService.moveToUpcomingEvent(id);
        return ResponseUtil.success(movedConcert, "공연이 예정 공연으로 이동되었습니다.");
    }
    
    @PutMapping("/auto-move-past")
    @Operation(summary = "자동 과거 이동", description = "현재 날짜 기준으로 자동으로 과거 공연을 이동시킵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 처리됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<AutoMoveResponse>> autoMovePastEvents(@RequestBody AutoMoveRequest request) {
        int movedCount = concertService.autoMovePastEvents(request.getCurrentDate());
        AutoMoveResponse response = new AutoMoveResponse(movedCount);
        return ResponseUtil.success(response, "자동 이동이 완료되었습니다.");
    }
    
    // DTO 클래스들
    public static class AutoMoveRequest {
        private String currentDate;
        
        public String getCurrentDate() {
            return currentDate;
        }
        
        public void setCurrentDate(String currentDate) {
            this.currentDate = currentDate;
        }
    }
    
    public static class AutoMoveResponse {
        private int movedCount;
        
        public AutoMoveResponse(int movedCount) {
            this.movedCount = movedCount;
        }
        
        public int getMovedCount() {
            return movedCount;
        }
        
        public void setMovedCount(int movedCount) {
            this.movedCount = movedCount;
        }
    }
}

