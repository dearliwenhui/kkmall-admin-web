package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.OrderCancelRequest;
import com.ab.kkmalladminweb.dto.OrderDeliverRequest;
import com.ab.kkmalladminweb.dto.OrderQueryRequest;
import com.ab.kkmalladminweb.dto.OrderResponse;
import com.ab.kkmalladminweb.dto.OrderStatisticsResponse;

/**
 * Order management service.
 */
public interface OrderService {

    PageResult<OrderResponse> list(OrderQueryRequest queryRequest);

    OrderResponse getById(Long id);

    OrderResponse getByOrderNo(String orderNo);

    void deliver(Long id, OrderDeliverRequest request);

    void cancel(Long id, OrderCancelRequest request);

    void complete(Long id);

    OrderStatisticsResponse statistics(String startTime, String endTime);

    byte[] export(OrderQueryRequest queryRequest);
}
