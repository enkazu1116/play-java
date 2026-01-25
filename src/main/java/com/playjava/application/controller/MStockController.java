package com.playjava.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.usecase.service.impl.MStockServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import com.playjava.enterprise.entity.MStock;
import com.playjava.frameworks.context.UserContext;
import com.baomidou.mybatisplus.core.metadata.IPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/v1/stocks")
@Tag(name = "在庫管理", description = "在庫マスタのCRUD操作と在庫検索API")
public class MStockController {

    private final Logger log = LoggerFactory.getLogger(MStockController.class);

    @Autowired
    private MStockServiceImpl mStockService;

    // 在庫登録
    @Operation(
        summary = "在庫登録",
        description = "新規在庫を登録します。在庫IDは自動生成されます。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登録成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public void createStock(
            @Parameter(description = "登録する在庫情報", required = true)
            @Valid @RequestBody MStock stock) {
        log.info("createStock: stock={}", stock);
        
        try {
            // 在庫登録処理（内部でUserContextが設定される）
            mStockService.createStockImpl(stock);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 在庫更新
    @Operation(
        summary = "在庫更新",
        description = "既存在庫の情報を更新します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
        @ApiResponse(responseCode = "404", description = "在庫が見つかりません")
    })
    @PutMapping("/updateStock")
    public void updateStock(
            @Parameter(description = "更新する在庫情報", required = true)
            @Valid @RequestBody MStock stock) {
        log.info("updateStock: stock={}", stock);
        
        try {
            // 更新者をコンテキストに設定
            UserContext.setCurrentUserId(stock.getStockId());
            
            // 在庫更新処理
            mStockService.updateStockImpl(stock);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 在庫論理削除
    @Operation(
        summary = "在庫削除",
        description = "指定された在庫を論理削除します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "在庫が見つかりません")
    })
    @DeleteMapping("/deleteStock/{stockId}")
    public void deleteStock(
            @Parameter(description = "削除する在庫のID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stockId) {
        log.info("deleteStock: stockId={}", stockId);
        
        try {
            // 削除者をコンテキストに設定
            UserContext.setCurrentUserId(stockId);
            
            // 在庫論理削除処理
            mStockService.deleteStockImpl(stockId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 在庫一覧（ページング・ソート対応）
    @Operation(
        summary = "在庫検索（ページング対応）",
        description = "在庫を条件で検索し、ページング・ソート機能を提供します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "検索成功")
    })
    @GetMapping("/search")
    public IPage<MStock> searchStock(
            @Parameter(description = "商品ID（完全一致）", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) String productId,
            @Parameter(description = "商品名（部分一致）", example = "商品名")
            @RequestParam(required = false) String productName,
            @Parameter(description = "在庫数量最小値（以上）", example = "0")
            @RequestParam(required = false) Integer quantityMin,
            @Parameter(description = "在庫数量最大値（以下）", example = "100")
            @RequestParam(required = false) Integer quantityMax,
            @Parameter(description = "在庫ステータス（0=在庫あり、1=在庫なし、2=発注済み）", example = "0")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "削除フラグ", example = "false")
            @RequestParam(required = false) Boolean deleteFlag,
            @Parameter(description = "ページ番号（1から開始）", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "1ページあたりの件数", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "ソート対象フィールド", example = "updateDate")
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @Parameter(description = "ソート順序（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("searchStock: productId={}, productName={}, quantityMin={}, quantityMax={}, status={}, deleteFlag={}, pageNum={}, pageSize={}, sortBy={}, sortOrder={}",
                productId, productName, quantityMin, quantityMax, status, deleteFlag, pageNum, pageSize, sortBy, sortOrder);

        return mStockService.searchStockImpl(
                productId, productName, quantityMin, quantityMax, status, deleteFlag,
                pageNum, pageSize, sortBy, sortOrder);
    }

    // 在庫照会（商品ID指定）
    @Operation(
        summary = "在庫照会（商品ID指定）",
        description = "指定された商品IDの在庫情報を取得します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "照会成功"),
        @ApiResponse(responseCode = "404", description = "在庫が見つかりません")
    })
    @GetMapping("/products/{productId}")
    public MStock getStockByProductId(
            @Parameter(description = "照会する商品ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String productId) {
        log.info("getStockByProductId: productId={}", productId);
        
        try {
            // 在庫照会処理
            return mStockService.getStockByProductIdImpl(productId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }
}
