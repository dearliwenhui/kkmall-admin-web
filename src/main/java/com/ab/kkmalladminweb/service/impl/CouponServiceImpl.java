package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.CouponQueryRequest;
import com.ab.kkmalladminweb.dto.CouponResponse;
import com.ab.kkmalladminweb.dto.CouponSaveRequest;
import com.ab.kkmalladminweb.entity.Coupon;
import com.ab.kkmalladminweb.entity.Order;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.CouponMapper;
import com.ab.kkmalladminweb.mapper.OrderMapper;
import com.ab.kkmalladminweb.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Coupon service implementation.
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final int TYPE_FULL_REDUCTION = 1;
    private static final int TYPE_DISCOUNT = 2;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CouponMapper couponMapper;
    private final OrderMapper orderMapper;

    @Override
    public PageResult<CouponResponse> list(CouponQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String name = trimToNull(queryRequest.getName());

        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Coupon::getName, name)
                .eq(queryRequest.getType() != null, Coupon::getType, queryRequest.getType())
                .eq(queryRequest.getStatus() != null, Coupon::getStatus, queryRequest.getStatus())
                .orderByDesc(Coupon::getUpdateTime)
                .orderByDesc(Coupon::getId);

        Page<Coupon> page = couponMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<CouponResponse> records = page.getRecords().stream().map(this::toResponse).toList();
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CouponResponse getById(Long id) {
        return toResponse(requireCoupon(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponResponse create(CouponSaveRequest request) {
        Coupon coupon = new Coupon();
        applyRequest(coupon, request, null);
        coupon.setReceivedCount(0);
        couponMapper.insert(coupon);
        return getById(coupon.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponResponse update(Long id, CouponSaveRequest request) {
        Coupon coupon = requireCoupon(id);
        applyRequest(coupon, request, coupon);
        int affected = couponMapper.updateById(coupon);
        ensureUpdated(affected, "数据已变化，请刷新后重试");
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Coupon coupon = requireCoupon(id);
        validateDeletable(coupon);
        couponMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Coupon> coupons = couponMapper.selectBatchIds(ids);
        if (coupons.size() != ids.size()) {
            throw new BusinessException("Some coupons do not exist");
        }
        coupons.forEach(this::validateDeletable);
        couponMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status must be 0 or 1");
        }
        Coupon coupon = requireCoupon(id);
        coupon.setStatus(status);
        int affected = couponMapper.updateById(coupon);
        ensureUpdated(affected, "数据已变化，请刷新后重试");
    }

    private Coupon requireCoupon(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("Coupon not found");
        }
        return coupon;
    }

    private void applyRequest(Coupon target, CouponSaveRequest request, Coupon existing) {
        String name = trimToNull(request.getName());
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("name is required");
        }

        if (request.getType() == null || (request.getType() != TYPE_FULL_REDUCTION && request.getType() != TYPE_DISCOUNT)) {
            throw new BusinessException("type must be 1 or 2");
        }

        BigDecimal minAmount = request.getMinAmount() == null ? BigDecimal.ZERO : request.getMinAmount();
        if (minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("minAmount must be >= 0");
        }

        int receivedCount = existing == null || existing.getReceivedCount() == null ? 0 : existing.getReceivedCount();
        if (request.getTotalCount() == null || request.getTotalCount() < 1) {
            throw new BusinessException("totalCount must be >= 1");
        }
        if (request.getTotalCount() < receivedCount) {
            throw new BusinessException("totalCount cannot be smaller than receivedCount");
        }

        if (request.getValidDays() == null || request.getValidDays() < 1) {
            throw new BusinessException("validDays must be >= 1");
        }

        LocalDateTime startTime = parseDateTime(request.getStartTime(), "startTime");
        LocalDateTime endTime = parseDateTime(request.getEndTime(), "endTime");
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("startTime must be before endTime");
        }

        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new BusinessException("status must be 0 or 1");
        }

        BigDecimal discountAmount = null;
        BigDecimal discountRate = null;
        if (request.getType() == TYPE_FULL_REDUCTION) {
            discountAmount = request.getDiscountAmount();
            if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("discountAmount must be > 0 for full reduction coupons");
            }
        } else {
            discountRate = normalizeDiscountRate(request.getDiscountRate());
        }

        target.setName(name);
        target.setType(request.getType());
        target.setDiscountAmount(discountAmount == null ? null : discountAmount.setScale(2, RoundingMode.HALF_UP));
        target.setDiscountRate(discountRate);
        target.setMinAmount(minAmount.setScale(2, RoundingMode.HALF_UP));
        target.setTotalCount(request.getTotalCount());
        target.setReceivedCount(receivedCount);
        target.setValidDays(request.getValidDays());
        target.setStartTime(startTime);
        target.setEndTime(endTime);
        target.setStatus(request.getStatus());
    }

    private BigDecimal normalizeDiscountRate(BigDecimal rawRate) {
        if (rawRate == null) {
            throw new BusinessException("discountRate is required for discount coupons");
        }
        if (rawRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("discountRate must be > 0");
        }

        BigDecimal normalized = rawRate;
        if (rawRate.compareTo(BigDecimal.ONE) <= 0) {
            normalized = rawRate.multiply(BigDecimal.TEN);
        }

        if (normalized.compareTo(BigDecimal.TEN) >= 0) {
            throw new BusinessException("discountRate must be < 10");
        }

        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDeletable(Coupon coupon) {
        int receivedCount = coupon.getReceivedCount() == null ? 0 : coupon.getReceivedCount();
        if (receivedCount > 0) {
            throw new BusinessException("Coupon has already been received and cannot be deleted");
        }

        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getCouponId, coupon.getId())
        );
        if (orderCount != null && orderCount > 0) {
            throw new BusinessException("Coupon has already been used in orders and cannot be deleted");
        }
    }

    private CouponResponse toResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setName(coupon.getName());
        response.setType(coupon.getType());
        response.setTypeName(coupon.getType() != null && coupon.getType() == TYPE_FULL_REDUCTION
                ? "Full reduction"
                : "Discount");
        response.setDiscountAmount(coupon.getDiscountAmount());
        response.setDiscountRate(coupon.getDiscountRate());
        response.setMinAmount(coupon.getMinAmount());
        response.setTotalCount(coupon.getTotalCount());
        response.setReceivedCount(coupon.getReceivedCount());
        int totalCount = coupon.getTotalCount() == null ? 0 : coupon.getTotalCount();
        int receivedCount = coupon.getReceivedCount() == null ? 0 : coupon.getReceivedCount();
        response.setRemainingCount(Math.max(totalCount - receivedCount, 0));
        response.setValidDays(coupon.getValidDays());
        response.setStartTime(coupon.getStartTime());
        response.setEndTime(coupon.getEndTime());
        response.setStatus(coupon.getStatus());
        response.setStatusName(coupon.getStatus() != null && coupon.getStatus() == 1 ? "Enabled" : "Disabled");
        response.setCreateTime(coupon.getCreateTime());
        response.setUpdateTime(coupon.getUpdateTime());
        return response;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(fieldName + " is required");
        }

        try {
            if (normalized.contains("T")) {
                return LocalDateTime.parse(normalized);
            }
            if (normalized.length() == 10) {
                return LocalDate.parse(normalized).atStartOfDay();
            }
            return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Unsupported datetime format for " + fieldName + ": " + value);
        }
    }

    private void ensureUpdated(int affected, String message) {
        if (affected > 0) {
            return;
        }
        throw new BusinessException(message);
    }
}
