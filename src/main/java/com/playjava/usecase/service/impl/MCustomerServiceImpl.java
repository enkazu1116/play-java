package com.playjava.usecase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.playjava.frameworks.mapper.MCustomerMapper;
import com.playjava.enterprise.entity.MCustomer;
import com.playjava.usecase.service.contract.MCustomerService;
import com.playjava.usecase.handler.UuidFactory;
import com.playjava.usecase.handler.CustomerNumberGenerator;

import java.util.UUID;
import java.util.List;

@Service
public class MCustomerServiceImpl extends ServiceImpl<MCustomerMapper, MCustomer> implements MCustomerService {

    private static final int MAX_RETRY_COUNT = 10;

    /**
     * 顧客作成処理
     * @param customer UIから@RequestBodyで受け取った顧客情報
     * @return 作成成功の場合true
     */
    public boolean createCustomerImpl(MCustomer customer) {
        // UUIDv7を生成して顧客IDを設定
        UUID customerId = UuidFactory.newUuid();
        customer.setCustomerId(customerId.toString());

        // 顧客番号を自動生成（重複チェック付き）
        String customerNumber = generateUniqueCustomerNumber();
        customer.setCustomerNumber(customerNumber);

        customer.setDeleteFlag(false);

        // 顧客を保存（UserContextからログインユーザーIDを取得してcreateUser/updateUserに設定される）
        boolean result = this.save(customer);
        
        return result;
    }

    /**
     * 顧客検索処理（簡易版・一覧）
     */
    public List<MCustomer> searchCustomerImpl(
            String customerNumber,
            String customerName,
            String address,
            String mobileNumber,
            String email,
            Boolean deleteFlag) {

        LambdaQueryWrapper<MCustomer> wrapper = buildSearchWrapper(
                customerNumber, customerName, address, mobileNumber, email, deleteFlag);

        return this.list(wrapper);
    }

    /**
     * 顧客検索処理（ページング・ソート対応）
     * @param customerNumber 顧客番号（完全一致）
     * @param customerName 顧客名（部分一致）
     * @param address 住所（部分一致）
     * @param mobileNumber 携帯番号（完全一致）
     * @param email メールアドレス（完全一致）
     * @param deleteFlag 削除フラグ（true/false/null、未指定時はfalseのみ）
     * @param pageNum ページ番号（1から開始）
     * @param pageSize ページサイズ
     * @param sortBy ソート対象カラム（customerNumber, customerName, createDate, updateDate）
     * @param sortOrder ソート順（asc/desc）
     * @return ページング情報を含む検索結果
     */
    public IPage<MCustomer> searchCustomerImpl(
            String customerNumber,
            String customerName,
            String address,
            String mobileNumber,
            String email,
            Boolean deleteFlag,
            int pageNum,
            int pageSize,
            String sortBy,
            String sortOrder) {

        Page<MCustomer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MCustomer> wrapper = buildSearchWrapper(
                customerNumber, customerName, address, mobileNumber, email, deleteFlag);

        // ソート設定
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            switch (sortBy) {
                case "customerNumber":
                    wrapper.orderByAsc(MCustomer::getCustomerNumber);
                    break;
                case "customerName":
                    wrapper.orderByAsc(MCustomer::getCustomerName);
                    break;
                case "createDate":
                    wrapper.orderByAsc(MCustomer::getCreateDate);
                    break;
                case "updateDate":
                default:
                    wrapper.orderByAsc(MCustomer::getUpdateDate);
                    break;
            }
        } else {
            switch (sortBy) {
                case "customerNumber":
                    wrapper.orderByDesc(MCustomer::getCustomerNumber);
                    break;
                case "customerName":
                    wrapper.orderByDesc(MCustomer::getCustomerName);
                    break;
                case "createDate":
                    wrapper.orderByDesc(MCustomer::getCreateDate);
                    break;
                case "updateDate":
                default:
                    wrapper.orderByDesc(MCustomer::getUpdateDate);
                    break;
            }
        }

        return this.page(page, wrapper);
    }

    /**
     * 顧客検索条件の共通Wrapper生成
     */
    private LambdaQueryWrapper<MCustomer> buildSearchWrapper(
            String customerNumber,
            String customerName,
            String address,
            String mobileNumber,
            String email,
            Boolean deleteFlag) {

        LambdaQueryWrapper<MCustomer> wrapper = new LambdaQueryWrapper<>();

        // 顧客番号（完全一致）
        if (customerNumber != null && !customerNumber.isEmpty()) {
            wrapper.eq(MCustomer::getCustomerNumber, customerNumber);
        }

        // 顧客名（部分一致）
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(MCustomer::getCustomerName, customerName);
        }

        // 住所（部分一致）
        if (address != null && !address.isEmpty()) {
            wrapper.like(MCustomer::getAddress, address);
        }

        // 携帯番号（完全一致）
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            wrapper.eq(MCustomer::getMobileNumber, mobileNumber);
        }

        // メールアドレス（完全一致）
        if (email != null && !email.isEmpty()) {
            wrapper.eq(MCustomer::getEmail, email);
        }

        // 削除フラグ（未指定時はfalseのみ、明示的に指定した場合のみ削除済みも含める）
        if (deleteFlag != null) {
            wrapper.eq(MCustomer::getDeleteFlag, deleteFlag);
        } else {
            wrapper.eq(MCustomer::getDeleteFlag, false);
        }

        return wrapper;
    }

    /**
     * 顧客更新処理
     * @param customer UIから@RequestBodyで受け取った顧客情報
     * @return 更新成功の場合true
     */
    public boolean updateCustomerImpl(MCustomer customer) {
        // 存在チェック
        MCustomer existingCustomer = this.getById(customer.getCustomerId());
        if (existingCustomer == null) {
            throw new RuntimeException("顧客が存在しません: customerId=" + customer.getCustomerId());
        }

        // 顧客番号は変更不可（自動採番項目のため）
        customer.setCustomerNumber(existingCustomer.getCustomerNumber());

        // 顧客を更新（UserContextからログインユーザーIDを取得してupdateUserに設定される）
        boolean result = this.updateById(customer);
        
        return result;
    }

    /**
     * 顧客論理削除処理
     * @param customerId 削除対象の顧客ID
     * @return 削除成功の場合true
     */
    public boolean deleteCustomerImpl(String customerId) {
        // 存在チェック（論理削除を含めて取得）
        MCustomer existingCustomer = this.getBaseMapper().selectByIdIncludeDeleted(customerId);
        
        if (existingCustomer == null) {
            throw new RuntimeException("顧客が存在しません: customerId=" + customerId);
        }

        // 既に削除済みかチェック
        if (Boolean.TRUE.equals(existingCustomer.getDeleteFlag())) {
            throw new RuntimeException("顧客は既に削除されています: customerId=" + customerId);
        }

        // MyBatis Plusの論理削除機能を使用
        // removeByIdを使うと、@TableLogicの設定に従って自動的にdeleteFlag=trueに更新される
        boolean result = this.removeById(customerId);
        
        return result;
    }

    /**
     * 顧客番号の存在チェック
     * @param customerNumber チェックする顧客番号
     * @return 存在する場合true
     */
    private boolean existsByCustomerNumber(String customerNumber) {
        LambdaQueryWrapper<MCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MCustomer::getCustomerNumber, customerNumber);
        return this.count(wrapper) > 0;
    }

    /**
     * 一意な顧客番号を生成する（重複チェック付き）
     * @return 一意な顧客番号
     */
    private String generateUniqueCustomerNumber() {
        int retryCount = 0;
        String customerNumber;
        
        do {
            customerNumber = CustomerNumberGenerator.generate();
            retryCount++;
            
            if (retryCount > MAX_RETRY_COUNT) {
                throw new RuntimeException("顧客番号の生成に失敗しました（最大試行回数を超えました）");
            }
        } while (existsByCustomerNumber(customerNumber));
        
        return customerNumber;
    }
}
