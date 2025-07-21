package com.prm.groupproject_flowershop.apis;

public class OderDetailRepository {
    public static OrderDetailService getOrderDetailService() {
        return ApiClient2.getClient().create(OrderDetailService.class);
    }
}
