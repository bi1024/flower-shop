package com.prm.groupproject_flowershop.apis;

public class CustomerRepository {
    public static CustomerService getCustomerService() {
        return ApiClient.getClient().create(CustomerService.class);
    }
}
