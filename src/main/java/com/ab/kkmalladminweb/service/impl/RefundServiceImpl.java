package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.RefundAuditRequest;
import com.ab.kkmalladminweb.dto.RefundAuditLogResponse;
import com.ab.kkmalladminweb.dto.RefundQueryRequest;
import com.ab.kkmalladminweb.dto.RefundResponse;
import com.ab.kkmalladminweb.dto.RefundStatisticsResponse;
import com.ab.kkmalladminweb.entity.MallUser;
import com.ab.kkmalladminweb.entity.Order;
import com.ab.kkmalladminweb.entity.OrderItem;
import com.ab.kkmalladminweb.entity.Product;
import com.ab.kkmalladminweb.entity.Refund;
import com.ab.kkmalladminweb.entity.RefundAuditLog;
import com.ab.kkmalladminweb.entity.SysUser;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.MallUserMapper;
import com.ab.kkmalladminweb.mapper.OrderItemMapper;
import com.ab.kkmalladminweb.mapper.OrderMapper;
import com.ab.kkmalladminweb.mapper.ProductMapper;
import com.ab.kkmalladminweb.mapper.RefundAuditLogMapper;
import com.ab.kkmalladminweb.mapper.RefundMapper;
import com.ab.kkmalladminweb.service.RefundService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ab.kkmalladminweb.security.LoginUser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Refund management service implementation.
 */
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private static final int REFUND_TYPE_ONLY = 1;
    private static final int REFUND_TYPE_RETURN = 2;
    private static final int REFUND_STATUS_PENDING = 0;
    private static final int REFUND_STATUS_REJECTED = 2;
    private static final int REFUND_STATUS_SUCCESS = 3;
    private static final int ORDER_STATUS_CANCELLED = 4;
    private static final String ACTION_SUBMITTED = "SUBMITTED";
    private static final String ACTION_APPROVED = "APPROVED";
    private static final String ACTION_REJECTED = "REJECTED";
    private static final String ACTION_REFUNDED = "REFUNDED";

    private final RefundMapper refundMapper;
    private final RefundAuditLogMapper refundAuditLogMapper;
    private final MallUserMapper mallUserMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<RefundResponse> list(RefundQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();

        Page<Refund> page = refundMapper.selectPage(new Page<>(pageNum, pageSize), buildRefundQuery(queryRequest));
        List<Refund> refunds = page.getRecords();
        Map<Long, MallUser> userMap = loadUserMap(refunds);
        List<RefundResponse> records = refunds.stream()
                .map(refund -> toResponse(refund, userMap.get(refund.getUserId())))
                .toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public RefundResponse getById(Long id) {
        Refund refund = requireRefund(id);
        ensureSubmittedLogExists(refund);
        MallUser user = refund.getUserId() == null ? null : mallUserMapper.selectById(refund.getUserId());
        return toResponse(refund, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, RefundAuditRequest request) {
        if (request == null || request.getApproved() == null) {
            throw new BusinessException("approved is required");
        }

        Refund refund = requireRefund(id);
        if (refund.getStatus() == null || refund.getStatus() != REFUND_STATUS_PENDING) {
            throw new BusinessException("Only pending refunds can be audited");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            recordAuditLog(
                    refund,
                    ACTION_APPROVED,
                    currentOperatorId(),
                    currentOperatorName(),
                    buildAuditRemark(refund, ACTION_APPROVED, null)
            );
            processRefund(refund);
        } else {
            String rejectReason = trimToNull(request.getRejectReason());
            if (!StringUtils.hasText(rejectReason)) {
                throw new BusinessException("rejectReason is required when rejecting a refund");
            }
            refund.setStatus(REFUND_STATUS_REJECTED);
            refund.setRejectReason(rejectReason);
            refundMapper.updateById(refund);
            recordAuditLog(
                    refund,
                    ACTION_REJECTED,
                    currentOperatorId(),
                    currentOperatorName(),
                    buildAuditRemark(refund, ACTION_REJECTED, rejectReason)
            );
        }
    }

    @Override
    public RefundStatisticsResponse statistics() {
        RefundStatisticsResponse response = new RefundStatisticsResponse();
        response.setTotalRefunds(refundMapper.selectCount(new LambdaQueryWrapper<>()));
        response.setPendingRefunds(refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, REFUND_STATUS_PENDING)
        ));
        response.setRejectedRefunds(refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, REFUND_STATUS_REJECTED)
        ));
        response.setRefundedCount(refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, REFUND_STATUS_SUCCESS)
        ));
        response.setTotalRefundAmount(sumAmount(new LambdaQueryWrapper<>()));
        response.setRefundedAmount(sumAmount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, REFUND_STATUS_SUCCESS)
        ));
        response.setPendingAmount(sumAmount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, REFUND_STATUS_PENDING)
        ));
        return response;
    }

    private Refund requireRefund(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("Refund not found");
        }
        return refund;
    }

    private void processRefund(Refund refund) {
        refund.setStatus(REFUND_STATUS_SUCCESS);
        refund.setRejectReason(null);
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.updateById(refund);
        recordAuditLog(
                refund,
                ACTION_REFUNDED,
                currentOperatorId(),
                currentOperatorName(),
                buildAuditRemark(refund, ACTION_REFUNDED, null)
        );

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, refund.getOrderId())
        );

        for (OrderItem item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                continue;
            }
            int stock = product.getStock() == null ? 0 : product.getStock();
            product.setStock(stock + item.getQuantity());
            productMapper.updateById(product);
        }

        Order order = refund.getOrderId() == null ? null : orderMapper.selectById(refund.getOrderId());
        if (order != null) {
            order.setStatus(ORDER_STATUS_CANCELLED);
            orderMapper.updateById(order);
        }
    }

    private LambdaQueryWrapper<Refund> buildRefundQuery(RefundQueryRequest queryRequest) {
        String refundNo = trimToNull(queryRequest.getRefundNo());
        String orderNo = trimToNull(queryRequest.getOrderNo());
        String keyword = trimToNull(queryRequest.getKeyword());

        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(refundNo), Refund::getRefundNo, refundNo)
                .like(StringUtils.hasText(orderNo), Refund::getOrderNo, orderNo)
                .eq(queryRequest.getStatus() != null, Refund::getStatus, queryRequest.getStatus())
                .eq(queryRequest.getRefundType() != null, Refund::getRefundType, queryRequest.getRefundType())
                .orderByDesc(Refund::getCreateTime)
                .orderByDesc(Refund::getId);

        if (StringUtils.hasText(keyword)) {
            List<Long> matchedUserIds = findMatchedUserIds(keyword);
            wrapper.and(query -> {
                query.like(Refund::getRefundNo, keyword)
                        .or()
                        .like(Refund::getOrderNo, keyword)
                        .or()
                        .like(Refund::getReason, keyword)
                        .or()
                        .like(Refund::getRejectReason, keyword);
                if (!matchedUserIds.isEmpty()) {
                    query.or().in(Refund::getUserId, matchedUserIds);
                }
            });
        }
        return wrapper;
    }

    private List<Long> findMatchedUserIds(String keyword) {
        List<MallUser> users = mallUserMapper.selectList(
                new LambdaQueryWrapper<MallUser>()
                        .select(MallUser::getId)
                        .and(query -> query.like(MallUser::getUsername, keyword)
                                .or()
                                .like(MallUser::getNickname, keyword))
        );
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(MallUser::getId).toList();
    }

    private Map<Long, MallUser> loadUserMap(List<Refund> refunds) {
        if (refunds == null || refunds.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = refunds.stream()
                .map(Refund::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return mallUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(
                        MallUser::getId,
                        user -> user,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private RefundResponse toResponse(Refund refund, MallUser user) {
        RefundResponse response = new RefundResponse();
        response.setId(refund.getId());
        response.setRefundNo(refund.getRefundNo());
        response.setOrderId(refund.getOrderId());
        response.setOrderNo(refund.getOrderNo());
        response.setUserId(refund.getUserId());
        response.setUsername(user == null ? null : user.getUsername());
        response.setNickname(user == null ? null : user.getNickname());
        response.setRefundAmount(refund.getRefundAmount());
        response.setRefundType(refund.getRefundType());
        response.setRefundTypeText(refundTypeText(refund.getRefundType()));
        response.setReason(refund.getReason());
        response.setDescription(refund.getDescription());
        response.setImages(parseImages(refund.getImages()));
        response.setStatus(refund.getStatus());
        response.setStatusText(statusText(refund.getStatus()));
        response.setRejectReason(refund.getRejectReason());
        response.setAuditLogs(loadAuditLogs(refund.getId()));
        response.setRefundTime(refund.getRefundTime());
        response.setCreateTime(refund.getCreateTime());
        response.setUpdateTime(refund.getUpdateTime());
        return response;
    }

    private List<RefundAuditLogResponse> loadAuditLogs(Long refundId) {
        if (refundId == null) {
            return Collections.emptyList();
        }
        return refundAuditLogMapper.selectList(
                new LambdaQueryWrapper<RefundAuditLog>()
                        .eq(RefundAuditLog::getRefundId, refundId)
                        .orderByAsc(RefundAuditLog::getCreateTime)
                        .orderByAsc(RefundAuditLog::getId)
        ).stream().map(this::toAuditLogResponse).toList();
    }

    private RefundAuditLogResponse toAuditLogResponse(RefundAuditLog log) {
        RefundAuditLogResponse response = new RefundAuditLogResponse();
        response.setId(log.getId());
        response.setActionCode(log.getActionCode());
        response.setActionText(actionText(log.getActionCode()));
        response.setOperatorType(log.getOperatorType());
        response.setOperatorTypeText(operatorTypeText(log.getOperatorType()));
        response.setOperatorId(log.getOperatorId());
        response.setOperatorName(log.getOperatorName());
        response.setOperatorDisplayName(buildOperatorDisplayName(log));
        response.setRemark(log.getRemark());
        response.setCreateTime(log.getCreateTime());
        return response;
    }

    private void ensureSubmittedLogExists(Refund refund) {
        if (refund == null || refund.getId() == null) {
            return;
        }
        Long count = refundAuditLogMapper.selectCount(
                new LambdaQueryWrapper<RefundAuditLog>()
                        .eq(RefundAuditLog::getRefundId, refund.getId())
                        .eq(RefundAuditLog::getActionCode, ACTION_SUBMITTED)
        );
        if (count != null && count > 0) {
            return;
        }
        MallUser user = refund.getUserId() == null ? null : mallUserMapper.selectById(refund.getUserId());
        RefundAuditLog log = new RefundAuditLog();
        log.setRefundId(refund.getId());
        log.setRefundNo(refund.getRefundNo());
        log.setActionCode(ACTION_SUBMITTED);
        log.setOperatorType("USER");
        log.setOperatorId(refund.getUserId());
        log.setOperatorName(user == null ? null : (StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername()));
        log.setRemark(buildAuditRemark(refund, ACTION_SUBMITTED, refund.getReason()));
        log.setCreateTime(refund.getCreateTime());
        refundAuditLogMapper.insert(log);
    }

    private void recordAuditLog(Refund refund, String actionCode, Long operatorId, String operatorName, String remark) {
        RefundAuditLog log = new RefundAuditLog();
        log.setRefundId(refund.getId());
        log.setRefundNo(refund.getRefundNo());
        log.setActionCode(actionCode);
        log.setOperatorType("ADMIN");
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        refundAuditLogMapper.insert(log);
    }

    private Long currentOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser.getUserId();
    }

    private String currentOperatorName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return "admin";
        }
        SysUser sysUser = loginUser.getSysUser();
        if (sysUser == null) {
            return loginUser.getUsername();
        }
        return StringUtils.hasText(sysUser.getNickname()) ? sysUser.getNickname() : sysUser.getUsername();
    }

    private String actionText(String actionCode) {
        if (!StringUtils.hasText(actionCode)) {
            return "Unknown";
        }
        return switch (actionCode) {
            case ACTION_SUBMITTED -> "Submitted";
            case ACTION_APPROVED -> "Approved";
            case ACTION_REJECTED -> "Rejected";
            case ACTION_REFUNDED -> "Refunded";
            default -> actionCode;
        };
    }

    private String operatorTypeText(String operatorType) {
        if (!StringUtils.hasText(operatorType)) {
            return "Unknown";
        }
        return switch (operatorType) {
            case "USER" -> "Mall user";
            case "ADMIN" -> "Admin console";
            case "SYSTEM" -> "System";
            default -> operatorType;
        };
    }

    private String buildOperatorDisplayName(RefundAuditLog log) {
        String operatorName = trimToNull(log.getOperatorName());
        String operatorTypeText = operatorTypeText(log.getOperatorType());
        if (!StringUtils.hasText(operatorName)) {
            return operatorTypeText;
        }
        return operatorName + " (" + operatorTypeText + ")";
    }

    private String buildAuditRemark(Refund refund, String actionCode, String extraRemark) {
        String orderNo = trimToNull(refund.getOrderNo());
        String amount = refund.getRefundAmount() == null ? "0.00" : refund.getRefundAmount().toPlainString();
        return switch (actionCode) {
            case ACTION_SUBMITTED -> "User submitted refund for order "
                    + (StringUtils.hasText(orderNo) ? orderNo : "-")
                    + ", amount "
                    + amount
                    + (StringUtils.hasText(extraRemark) ? ", reason: " + extraRemark : "");
            case ACTION_APPROVED -> "Admin approved refund for order "
                    + (StringUtils.hasText(orderNo) ? orderNo : "-")
                    + ", amount "
                    + amount;
            case ACTION_REJECTED -> "Admin rejected refund for order "
                    + (StringUtils.hasText(orderNo) ? orderNo : "-")
                    + (StringUtils.hasText(extraRemark) ? ", reason: " + extraRemark : "");
            case ACTION_REFUNDED -> "Refund completed for order "
                    + (StringUtils.hasText(orderNo) ? orderNo : "-")
                    + ", amount "
                    + amount;
            default -> extraRemark;
        };
    }

    private String refundTypeText(Integer refundType) {
        if (refundType == null) {
            return "Unknown";
        }
        return switch (refundType) {
            case REFUND_TYPE_ONLY -> "Refund only";
            case REFUND_TYPE_RETURN -> "Return and refund";
            default -> "Unknown";
        };
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case REFUND_STATUS_PENDING -> "Pending review";
            case REFUND_STATUS_REJECTED -> "Rejected";
            case REFUND_STATUS_SUCCESS -> "Refunded";
            default -> "Unknown";
        };
    }

    private List<String> parseImages(String images) {
        if (!StringUtils.hasText(images)) {
            return Collections.emptyList();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private java.math.BigDecimal sumAmount(LambdaQueryWrapper<Refund> wrapper) {
        wrapper.select(Refund::getRefundAmount);
        return refundMapper.selectList(wrapper).stream()
                .map(Refund::getRefundAmount)
                .filter(amount -> amount != null)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
