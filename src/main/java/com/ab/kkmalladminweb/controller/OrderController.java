package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.OrderCancelRequest;
import com.ab.kkmalladminweb.dto.OrderDeliverRequest;
import com.ab.kkmalladminweb.dto.OrderQueryRequest;
import com.ab.kkmalladminweb.dto.OrderResponse;
import com.ab.kkmalladminweb.dto.OrderStatisticsResponse;
import com.ab.kkmalladminweb.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Order management controller.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * Query order list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<PageResult<OrderResponse>> list(@Valid OrderQueryRequest queryRequest) {
        return Result.success(orderService.list(queryRequest));
    }

    /**
     * Export orders.
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('order:manage')")
    public ResponseEntity<byte[]> export(@Valid OrderQueryRequest queryRequest) {
        byte[] data = orderService.export(queryRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("orders.csv")
                        .build().toString())
                .contentType(new MediaType("text", "csv"))
                .body(data);
    }

    /**
     * Get order statistics.
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<OrderStatisticsResponse> statistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(orderService.statistics(startTime, endTime));
    }

    /**
     * Get order detail by order number.
     */
    @GetMapping("/no/{orderNo}")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<OrderResponse> detailByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getByOrderNo(orderNo));
    }

    /**
     * Get order detail by id.
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<OrderResponse> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    /**
     * Deliver order.
     */
    @PutMapping("/{id:\\d+}/deliver")
    @PreAuthorize("hasAuthority('order:deliver')")
    public Result<Void> deliver(@PathVariable Long id, @Valid @RequestBody OrderDeliverRequest request) {
        orderService.deliver(id, request);
        return Result.success();
    }

    /**
     * Cancel order.
     */
    @PutMapping("/{id:\\d+}/cancel")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<Void> cancel(@PathVariable Long id, @Valid @RequestBody(required = false) OrderCancelRequest request) {
        orderService.cancel(id, request);
        return Result.success();
    }

    /**
     * Complete order.
     */
    @PutMapping("/{id:\\d+}/complete")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
