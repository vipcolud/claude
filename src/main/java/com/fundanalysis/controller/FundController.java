package com.fundanalysis.controller;

import com.fundanalysis.dto.*;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.service.FundAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundAnalysisService analysisService;

    @GetMapping
    public ResponseEntity<List<FundDTO>> getAllFunds() {
        return ResponseEntity.ok(analysisService.getAllFunds());
    }

    @GetMapping("/{code}")
    public ResponseEntity<FundDetailDTO> getFundDetail(@PathVariable String code) {
        return ResponseEntity.ok(analysisService.getFundDetail(code));
    }

    @GetMapping("/{code}/metrics")
    public ResponseEntity<FundMetricsDTO> getFundMetrics(@PathVariable String code) {
        return ResponseEntity.ok(analysisService.getFundMetrics(code));
    }

    @PostMapping("/compare")
    public ResponseEntity<FundComparisonResponse> compareFunds(
            @Valid @RequestBody FundComparisonRequest request) {
        return ResponseEntity.ok(analysisService.compareFunds(request.getFundCodes()));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<FundDTO>> filterFunds(
            @RequestParam(required = false) FundType type,
            @RequestParam(required = false) BigDecimal minReturn) {
        return ResponseEntity.ok(analysisService.filterFunds(type, minReturn));
    }

    @GetMapping("/rank/return")
    public ResponseEntity<List<FundMetricsDTO>> rankByReturn() {
        return ResponseEntity.ok(analysisService.rankFundsByReturn());
    }

    @GetMapping("/rank/sharpe")
    public ResponseEntity<List<FundMetricsDTO>> rankBySharpeRatio() {
        return ResponseEntity.ok(analysisService.rankFundsBySharpeRatio());
    }
}
