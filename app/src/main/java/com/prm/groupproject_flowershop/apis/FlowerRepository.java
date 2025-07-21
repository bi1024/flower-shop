package com.prm.groupproject_flowershop.apis;

public class FlowerRepository {
    public static FlowerService getFlowerService() {
        return ApiClient.getClient().create(FlowerService.class);
    }
}
