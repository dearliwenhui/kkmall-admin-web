package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.OrderCancelRequest;
import com.ab.kkmalladminweb.dto.OrderDeliverRequest;
import com.ab.kkmalladminweb.dto.OrderItemResponse;
import com.ab.kkmalladminweb.dto.OrderQueryRequest;
import com.ab.kkmalladminweb.dto.OrderResponse;
import com.ab.kkmalladminweb.dto.OrderStatisticsResponse;
import com.ab.kkmalladminweb.entity.MallUser;
import com.ab.kkmalladminweb.entity.Order;
import com.ab.kkmalladminweb.entity.OrderItem;
import com.ab.kkmalladminweb.entity.Product;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.MallUserMapper;
import com.ab.kkmalladminweb.mapper.OrderItemMapper;
import com.ab.kkmalladminweb.mapper.OrderMapper;
import com.ab.kkmalladminweb.mapper.ProductMapper;
import com.ab.kkmalladminweb.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Order management service implementation.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int STATUS_PENDING_PAYMENT = 0;
    private static final int STATUS_PENDING_SHIPMENT = 1;
    private static final int STATUS_PENDING_RECEIPT = 2;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELLED = 4;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MallUserMapper mallUserMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<OrderResponse> list(OrderQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        LambdaQueryWrapper<Order> wrapper = buildOrderQuery(queryRequest);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Order> orders = page.getRecords();
        Map<Long, MallUser> userMap = loadUserMap(orders);
        Map<Long, List<OrderItem>> itemMap = loadItemsMap(orders);

        List<OrderResponse> records = orders.stream()
                .map(order -> toResponse(order, userMap.get(order.getUserId()), itemMap.get(order.getId()), false))
                .toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public OrderResponse getById(Long id) {
        Order order = requireOrderById(id);
        MallUser user = loadUser(order.getUserId());
        List<OrderItem> items = loadItems(order.getId());
        return toResponse(order, user, items, true);
    }

    @Override
    public OrderResponse getByOrderNo(String orderNo) {
        String normalizedOrderNo = trimToNull(orderNo);
        if (!StringUtils.hasText(normalizedOrderNo)) {
            throw new BusinessException("orderNo is required");
        }

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, normalizedOrderNo).last("limit 1")
        );
        if (order == null) {
            throw new BusinessException("Order not found");
        }

        MallUser user = loadUser(order.getUserId());
        List<OrderItem> items = loadItems(order.getId());
        return toResponse(order, user, items, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliver(Long id, OrderDeliverRequest request) {
        Order order = requireOrderById(id);
        if (!STATUS_PENDING_SHIPMENT_EQ(order)) {
            throw new BusinessException("Only pending-shipment orders can be delivered");
        }

        order.setStatus(STATUS_PENDING_RECEIPT);
        order.setLogisticsCompany(request.getExpressCompany().trim());
        order.setTrackingNumber(request.getExpressNo().trim());
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, OrderCancelRequest request) {
        Order order = requireOrderById(id);
        if (order.getStatus() == null
                || (order.getStatus() != STATUS_PENDING_PAYMENT && order.getStatus() != STATUS_PENDING_SHIPMENT)) {
            throw new BusinessException("Only unpaid or unshipped orders can be cancelled");
        }

        List<OrderItem> items = loadItems(order.getId());
        restoreStock(items);

        String reason = request == null ? null : trimToNull(request.getReason());
        if (StringUtils.hasText(reason)) {
            String existingRemark = trimToNull(order.getRemark());
            order.setRemark(StringUtils.hasText(existingRemark)
                    ? existingRemark + "\n[admin-cancel] " + reason
                    : "[admin-cancel] " + reason);
        }
        order.setStatus(STATUS_CANCELLED);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        Order order = requireOrderById(id);
        if (order.getStatus() == null || order.getStatus() != STATUS_PENDING_RECEIPT) {
            throw new BusinessException("Only pending-receipt orders can be completed");
        }

        order.setStatus(STATUS_COMPLETED);
        order.setConfirmTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public OrderStatisticsResponse statistics(String startTime, String endTime) {
        LocalDateTime parsedStartTime = parseDateTime(startTime, false);
        LocalDateTime parsedEndTime = parseDateTime(endTime, true);

        OrderStatisticsResponse response = new OrderStatisticsResponse();
        response.setTotalOrders(orderMapper.selectCount(buildTimeRangeQuery(parsedStartTime, parsedEndTime)));
        response.setPendingPaymentOrders(orderMapper.selectCount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime).eq(Order::getStatus, STATUS_PENDING_PAYMENT)
        ));
        response.setPendingShipmentOrders(orderMapper.selectCount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime).eq(Order::getStatus, STATUS_PENDING_SHIPMENT)
        ));
        response.setPendingReceiptOrders(orderMapper.selectCount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime).eq(Order::getStatus, STATUS_PENDING_RECEIPT)
        ));
        response.setCompletedOrders(orderMapper.selectCount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime).eq(Order::getStatus, STATUS_COMPLETED)
        ));
        response.setCancelledOrders(orderMapper.selectCount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime).eq(Order::getStatus, STATUS_CANCELLED)
        ));
        response.setTotalSales(sumAmount(
                buildTimeRangeQuery(parsedStartTime, parsedEndTime)
                        .in(Order::getStatus, STATUS_PENDING_SHIPMENT, STATUS_PENDING_RECEIPT, STATUS_COMPLETED)
        ));

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.of(23, 59, 59));
        response.setTodayOrders(orderMapper.selectCount(buildTimeRangeQuery(todayStart, todayEnd)));
        response.setTodaySales(sumAmount(
                buildTimeRangeQuery(todayStart, todayEnd)
                        .in(Order::getStatus, STATUS_PENDING_SHIPMENT, STATUS_PENDING_RECEIPT, STATUS_COMPLETED)
        ));

        return response;
    }

    @Override
    public byte[] export(OrderQueryRequest queryRequest) {
        List<Order> orders = orderMapper.selectList(buildOrderQuery(queryRequest));
        Map<Long, MallUser> userMap = loadUserMap(orders);

        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append(String.join(",",
                csvCell("\u8BA2\u5355\u53F7"),
                csvCell("\u4E0B\u5355\u7528\u6237"),
                csvCell("\u7528\u6237\u8D26\u53F7"),
                csvCell("\u8BA2\u5355\u91D1\u989D"),
                csvCell("\u8BA2\u5355\u72B6\u6001"),
                csvCell("\u6536\u8D27\u4EBA"),
                csvCell("\u6536\u8D27\u624B\u673A"),
                csvCell("\u6536\u8D27\u5730\u5740"),
                csvCell("\u7269\u6D41\u516C\u53F8"),
                csvCell("\u7269\u6D41\u5355\u53F7"),
                csvCell("\u652F\u4ED8\u65F6\u95F4"),
                csvCell("\u53D1\u8D27\u65F6\u95F4"),
                csvCell("\u5B8C\u6210\u65F6\u95F4"),
                csvCell("\u521B\u5EFA\u65F6\u95F4"),
                csvCell("\u5907\u6CE8")
        )).append("\r\n");

        for (Order order : orders) {
            MallUser user = userMap.get(order.getUserId());
            builder.append(String.join(",",
                    csvCell(order.getOrderNo()),
                    csvCell(user == null ? null : user.getNickname()),
                    csvCell(user == null ? null : user.getUsername()),
                    csvCell(order.getTotalAmount() == null ? null : order.getTotalAmount().toPlainString()),
                    csvCell(statusText(order.getStatus())),
                    csvCell(order.getReceiverName()),
                    csvCell(order.getReceiverPhone()),
                    csvCell(order.getReceiverAddress()),
                    csvCell(order.getLogisticsCompany()),
                    csvCell(order.getTrackingNumber()),
                    csvCell(formatDateTime(order.getPayTime())),
                    csvCell(formatDateTime(order.getShipTime())),
                    csvCell(formatDateTime(order.getConfirmTime())),
                    csvCell(formatDateTime(order.getCreateTime())),
                    csvCell(order.getRemark())
            )).append("\r\n");
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private boolean STATUS_PENDING_SHIPMENT_EQ(Order order) {
        return order.getStatus() != null && order.getStatus() == STATUS_PENDING_SHIPMENT;
    }

    private Order requireOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        return order;
    }

    private MallUser loadUser(Long userId) {
        return userId == null ? null : mallUserMapper.selectById(userId);
    }

    private Map<Long, MallUser> loadUserMap(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return mallUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(MallUser::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
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

    private LambdaQueryWrapper<Order> buildOrderQuery(OrderQueryRequest queryRequest) {
        String orderNo = trimToNull(queryRequest.getOrderNo());
        String keyword = trimToNull(queryRequest.getKeyword());
        LocalDateTime startTime = parseDateTime(queryRequest.getStartTime(), false);
        LocalDateTime endTime = parseDateTime(queryRequest.getEndTime(), true);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StringUtils.hasText(orderNo), Order::getOrderNo, orderNo)
                .eq(queryRequest.getStatus() != null, Order::getStatus, queryRequest.getStatus())
                .ge(startTime != null, Order::getCreateTime, startTime)
                .le(endTime != null, Order::getCreateTime, endTime)
                .orderByDesc(Order::getUpdateTime)
                .orderByDesc(Order::getId);

        if (StringUtils.hasText(keyword)) {
            List<Long> matchedUserIds = findMatchedUserIds(keyword);
            wrapper.and(query -> {
                query.like(Order::getOrderNo, keyword)
                        .or()
                        .like(Order::getReceiverName, keyword)
                        .or()
                        .like(Order::getReceiverPhone, keyword)
                        .or()
                        .like(Order::getReceiverAddress, keyword);
                if (!matchedUserIds.isEmpty()) {
                    query.or().in(Order::getUserId, matchedUserIds);
                }
            });
        }
        return wrapper;
    }

    private List<OrderItem> loadItems(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId)
        );
    }

    private Map<Long, List<OrderItem>> loadItemsMap(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).filter(id -> id != null).toList();
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .orderByAsc(OrderItem::getId)
        );

        return items.stream().collect(Collectors.groupingBy(OrderItem::getOrderId, LinkedHashMap::new, Collectors.toList()));
    }

    private void restoreStock(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

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
    }

    private OrderResponse toResponse(Order order, MallUser user, List<OrderItem> items, boolean includeItems) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setUsername(user == null ? null : user.getUsername());
        response.setNickname(user == null ? null : user.getNickname());
        response.setTotalAmount(order.getTotalAmount());
        response.setPayAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setStatusText(statusText(order.getStatus()));
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setLogisticsCompany(order.getLogisticsCompany());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setRemark(order.getRemark());
        response.setPayTime(order.getPayTime());
        response.setShipTime(order.getShipTime());
        response.setConfirmTime(order.getConfirmTime());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());

        List<OrderItem> safeItems = items == null ? Collections.emptyList() : items;
        response.setItemCount(safeItems.stream()
                .map(OrderItem::getQuantity)
                .filter(quantity -> quantity != null)
                .reduce(0, Integer::sum));

        if (includeItems) {
            List<OrderItemResponse> itemResponses = safeItems.stream().map(this::toItemResponse).toList();
            response.setItems(itemResponses);
        } else {
            response.setItems(new ArrayList<>());
        }

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        BeanUtils.copyProperties(item, response);
        return response;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case STATUS_PENDING_PAYMENT -> "Pending payment";
            case STATUS_PENDING_SHIPMENT -> "Pending shipment";
            case STATUS_PENDING_RECEIPT -> "Pending receipt";
            case STATUS_COMPLETED -> "Completed";
            case STATUS_CANCELLED -> "Cancelled";
            default -> "Unknown";
        };
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private LocalDateTime parseDateTime(String value, boolean endOfDay) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }

        try {
            if (normalized.contains("T")) {
                return LocalDateTime.parse(normalized);
            }
            if (normalized.length() == 10) {
                LocalDate date = LocalDate.parse(normalized);
                return endOfDay ? LocalDateTime.of(date, LocalTime.of(23, 59, 59)) : date.atStartOfDay();
            }
            return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Unsupported datetime format: " + value);
        }
    }

    private LambdaQueryWrapper<Order> buildTimeRangeQuery(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startTime != null, Order::getCreateTime, startTime)
                .le(endTime != null, Order::getCreateTime, endTime);
        return wrapper;
    }

    private BigDecimal sumAmount(LambdaQueryWrapper<Order> wrapper) {
        wrapper.select(Order::getTotalAmount);
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    private String csvCell(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }
}
