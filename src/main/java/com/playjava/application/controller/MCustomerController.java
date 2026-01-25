package com.playjava.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.usecase.service.impl.MCustomerServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import com.playjava.enterprise.entity.MCustomer;
import com.playjava.frameworks.context.UserContext;
import com.baomidou.mybatisplus.core.metadata.IPage;


@RestController
@RequestMapping("/api/v1/customers")
public class MCustomerController {

    private final Logger log = LoggerFactory.getLogger(MCustomerController.class);

    @Autowired
    private MCustomerServiceImpl mCustomerService;

    // 顧客作成
    @PostMapping
    public void createCustomer(@Valid @RequestBody MCustomer customer) {
        log.info("createCustomer: customer={}", customer);
        
        try {
            // 顧客作成処理（内部でUserContextが設定される）
            mCustomerService.createCustomerImpl(customer);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 顧客更新
    @PutMapping
    public void updateCustomer(@Valid @RequestBody MCustomer customer) {
        log.info("updateCustomer: customer={}", customer);
        
        try {
            // 顧客更新処理（UserContextからログインユーザーIDを取得してupdateUserに設定される）
            mCustomerService.updateCustomerImpl(customer);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 顧客論理削除
    @DeleteMapping("/{customerId}")
    public void deleteCustomer(@PathVariable String customerId) {
        log.info("deleteCustomer: customerId={}", customerId);
        
        try {
            // 顧客論理削除処理
            mCustomerService.deleteCustomerImpl(customerId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 顧客検索（ページング・ソート対応）
    @GetMapping("/search")
    public IPage<MCustomer> searchCustomer(
            @RequestParam(required = false) String customerNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean deleteFlag,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("searchCustomer: customerNumber={}, customerName={}, address={}, mobileNumber={}, email={}, deleteFlag={}, pageNum={}, pageSize={}, sortBy={}, sortOrder={}",
                customerNumber, customerName, address, mobileNumber, email, deleteFlag, pageNum, pageSize, sortBy, sortOrder);

        return mCustomerService.searchCustomerImpl(
                customerNumber, customerName, address, mobileNumber, email, deleteFlag,
                pageNum, pageSize, sortBy, sortOrder);
    }
}
