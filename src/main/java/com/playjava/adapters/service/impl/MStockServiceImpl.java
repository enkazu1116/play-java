package com.playjava.adapters.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.playjava.frameworks.mapper.MStockMapper;
import com.playjava.frameworks.mapper.MProductMapper;
import com.playjava.enterprise.entity.MStock;
import com.playjava.enterprise.entity.MProduct;
import com.playjava.adapters.service.contract.MStockService;
import com.playjava.adapters.handler.UuidFactory;
import com.playjava.frameworks.context.UserContext;

import java.util.UUID;
import java.time.OffsetDateTime;

import io.vavr.control.Option;

@Service
public class MStockServiceImpl extends ServiceImpl<MStockMapper, MStock> implements MStockService {

    @Autowired
    private MProductMapper mProductMapper;

    /**
     * 在庫登録処理
     * @param stock UIから@RequestBodyで受け取った在庫情報
     * @return 作成成功の場合true
     */
    public boolean createStockImpl(MStock stock) {
        // 業務ロジック: バリデーションと変換をVavrで処理
        
        // 商品マスタの存在チェック（MyBatis呼び出し - 副作用あり、一括処理）
        MProduct product = mProductMapper.selectById(stock.getProductId());
        
        // 商品マスタの存在チェック（Option使用）
        Option.of(product)
            .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + stock.getProductId()));
        
        // 一意性チェック（MyBatis呼び出し - 副作用あり、一括処理）
        LambdaQueryWrapper<MStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MStock::getProductId, stock.getProductId());
        long count = this.count(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("この商品の在庫レコードは既に存在します。更新APIを使用してください: productId=" + stock.getProductId());
        }
        
        // 業務ロジック: UUID生成と設定
        UUID stockId = UuidFactory.newUuid();
        stock.setStockId(stockId.toString());

        // 業務ロジック: quantityのバリデーション（Option使用）
        Integer quantity = Option.of(stock.getQuantity())
            .filter(q -> q >= 0)
            .getOrElseThrow(() -> new RuntimeException("在庫数量は0以上の整数である必要があります: quantity=" + stock.getQuantity()));
        
        // 業務ロジック: statusの自動設定（Option使用）
        Integer status = Option.of(stock.getStatus())
            .map(s -> {
                // statusのバリデーション（0-2の範囲）
                if (s < 0 || s > 2) {
                    throw new RuntimeException("在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります: status=" + s);
                }
                return s;
            })
            .getOrElse(() -> quantity > 0 ? 0 : 1); // 在庫あり: 0, 在庫なし: 1
        
        stock.setStatus(status);
        stock.setDeleteFlag(false);

        // MyBatis呼び出し - 副作用あり（一括処理）
        return this.save(stock);
    }

    /**
     * 在庫検索処理（ページング・ソート対応）
     * @param productId 商品ID（完全一致）
     * @param productName 商品名（部分一致、商品マスタとJOIN）
     * @param quantityMin 在庫数量最小値（以上）
     * @param quantityMax 在庫数量最大値（以下）
     * @param status 在庫ステータス（完全一致、0=在庫あり、1=在庫なし、2=発注済み）
     * @param deleteFlag 削除フラグ（true/false/null、未指定時はfalseのみ）
     * @param pageNum ページ番号（1から開始）
     * @param pageSize ページサイズ
     * @param sortBy ソート対象カラム（stockId, productId, quantity, status, createDate, updateDate）
     * @param sortOrder ソート順（asc/desc）
     * @return ページング情報を含む検索結果
     */
    public IPage<MStock> searchStockImpl(
            String productId,
            String productName,
            Integer quantityMin,
            Integer quantityMax,
            Integer status,
            Boolean deleteFlag,
            int pageNum,
            int pageSize,
            String sortBy,
            String sortOrder) {

        Page<MStock> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MStock> wrapper = buildSearchWrapper(
                productId, productName, quantityMin, quantityMax, status, deleteFlag);

        // ソート設定
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            switch (sortBy) {
                case "stockId":
                    wrapper.orderByAsc(MStock::getStockId);
                    break;
                case "productId":
                    wrapper.orderByAsc(MStock::getProductId);
                    break;
                case "quantity":
                    wrapper.orderByAsc(MStock::getQuantity);
                    break;
                case "status":
                    wrapper.orderByAsc(MStock::getStatus);
                    break;
            }
        } else {
            switch (sortBy) {
                case "stockId":
                    wrapper.orderByDesc(MStock::getStockId);
                    break;
                case "productId":
                    wrapper.orderByDesc(MStock::getProductId);
                    break;
                case "quantity":
                    wrapper.orderByDesc(MStock::getQuantity);
                    break;
                case "status":
                    wrapper.orderByDesc(MStock::getStatus);
                    break;
            }
        }

        return this.page(page, wrapper);
    }

    /**
     * 在庫検索条件の共通Wrapper生成
     * 注意: 商品名による検索はJOINが必要なため、現時点ではproductIdのみで検索する
     * TODO: 商品マスタとJOINして商品名での検索を実装する
     */
    private LambdaQueryWrapper<MStock> buildSearchWrapper(
            String productId,
            String productName,
            Integer quantityMin,
            Integer quantityMax,
            Integer status,
            Boolean deleteFlag) {

        LambdaQueryWrapper<MStock> wrapper = new LambdaQueryWrapper<>();

        // 商品ID（完全一致）
        if (productId != null && !productId.isEmpty()) {
            wrapper.eq(MStock::getProductId, productId);
        }

        // 商品名（部分一致）は商品マスタとJOINが必要なため、現時点では未実装
        // TODO: 商品マスタとJOINして商品名での検索を実装する
        if (productName != null && !productName.isEmpty()) {
            // 商品名で商品IDを取得してから在庫を検索する必要がある
            // 簡易実装として、productIdで検索する（商品名検索は別途実装が必要）
        }

        // 在庫数量最小値（以上）
        if (quantityMin != null) {
            wrapper.ge(MStock::getQuantity, quantityMin);
        }

        // 在庫数量最大値（以下）
        if (quantityMax != null) {
            wrapper.le(MStock::getQuantity, quantityMax);
        }

        // 在庫ステータス（完全一致）
        if (status != null) {
            wrapper.eq(MStock::getStatus, status);
        }

        // 削除フラグ（未指定時はfalseのみ、明示的に指定した場合のみ削除済みも含める）
        if (deleteFlag != null) {
            wrapper.eq(MStock::getDeleteFlag, deleteFlag);
        } else {
            wrapper.eq(MStock::getDeleteFlag, false);
        }

        return wrapper;
    }

    /**
     * 在庫照会処理（商品ID指定）
     * @param productId 商品ID
     * @return 在庫情報
     */
    public MStock getStockByProductIdImpl(String productId) {
        // 業務ロジック: バリデーションをVavrで処理
        
        // 商品マスタの存在チェック（MyBatis呼び出し - 副作用あり、一括処理）
        MProduct product = mProductMapper.selectById(productId);
        
        // 商品マスタの存在チェック（Option使用）
        Option.of(product)
            .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + productId));

        // 商品IDで在庫を検索（MyBatis呼び出し - 副作用あり、一括処理）
        LambdaQueryWrapper<MStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MStock::getProductId, productId);
        // 削除済みも含めて取得（在庫照会のため）
        // 論理削除フラグは@TableLogicにより自動的に除外されるため、明示的に指定しない
        
        MStock stock = this.getOne(wrapper);
        
        // 在庫レコードの存在チェック（Option使用）
        return Option.of(stock)
            .getOrElseThrow(() -> new RuntimeException("在庫レコードが存在しません。在庫登録を行ってください: productId=" + productId));
    }

    /**
     * 在庫更新処理
     * @param stock UIから@RequestBodyで受け取った在庫情報
     * @return 更新成功の場合true
     */
    public boolean updateStockImpl(MStock stock) {
        try {
            // 業務ロジック: バリデーションをVavrで処理
            String updateUser = UserContext.getCurrentUserId();

            // 在庫IDのバリデーション（Option使用）
            String stockId = Option.of(stock.getStockId())
                .filter(id -> !id.isEmpty())
                .getOrElseThrow(() -> new RuntimeException("在庫IDは必須です: stockId=" + stock.getStockId()));

            // 既存の在庫レコードを取得（MyBatis呼び出し - 副作用あり、一括処理）
            MStock existingStock = this.getById(stockId);
            
            // 在庫レコードの存在チェック（Option使用）
            Option.of(existingStock)
                .getOrElseThrow(() -> new RuntimeException("在庫レコードが存在しません: stockId=" + stockId));
            
            // 商品マスタの存在チェック（productIdが変更される場合）
            Option.of(stock.getProductId())
                .filter(productId -> !productId.equals(existingStock.getProductId()))
                .forEach(productId -> {
                    // MyBatis呼び出し - 副作用あり、一括処理
                    MProduct product = mProductMapper.selectById(productId);
                    Option.of(product)
                        .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + productId));
                });
            
            // 業務ロジック: quantityのバリデーション（Option使用）
            Option.of(stock.getQuantity())
                .filter(q -> q < 0)
                .forEach(q -> {
                    throw new RuntimeException("在庫数量は0以上の整数である必要があります: quantity=" + q);
                });
            
            // 業務ロジック: statusの自動設定（Option使用）
            Integer status = Option.of(stock.getStatus())
                .map(s -> {
                    // statusのバリデーション（0-2の範囲）
                    if (s < 0 || s > 2) {
                        throw new RuntimeException("在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります: status=" + s);
                    }
                    return s;
                })
                .getOrElse(() -> {
                    Integer quantity = stock.getQuantity();
                    return quantity != null && quantity > 0 ? 0 : 1; // 在庫あり: 0, 在庫なし: 1
                });
            
            stock.setStatus(status);

            // 更新内容を設定
            stock.setUpdateUser(updateUser);
            stock.setUpdateDate(OffsetDateTime.now());

            // MyBatis呼び出し - 副作用あり（一括処理）
            return this.updateById(stock);
        } catch (Exception e) {
            // エラー時はコンテキストをクリア
            UserContext.clear();
            throw e;
        }
    }

    /**
     * 在庫論理削除処理
     * @param stockId 削除対象の在庫ID
     * @return 削除成功の場合true
     */
    public boolean deleteStockImpl(String stockId) {
        try {
            // MyBatis呼び出し - 副作用あり（一括処理）
            // MyBatis Plusの論理削除機能を使用
            // removeByIdを使うと、@TableLogicの設定に従って自動的にdeleteFlag=trueに更新される
            return this.removeById(stockId);
        } catch (Exception e) {
            // エラー時はコンテキストをクリア
            UserContext.clear();
            throw e;
        }
    }
}
