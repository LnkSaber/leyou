package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
//    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOND(404,"商品分类沒查到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组没查到"),
    SPEC_GROUP_ADD_ERROR(500,"商品规格组新增失败"),
    SPEC_GROUP_UPDATE_ERROR(500,"商品规格组更新失败"),
    SPEC_GROUP_DELETE_ERROR(500,"商品规格组删除失败"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数没查到"),
    SPEC_PARAM_ADD_ERROR(500,"商品规格参数新增失败"),
    SPEC_PARAM_UPDATE_ERROR(500,"商品规格参数更新失败"),
    SPEC_PARAM_DELETE_ERROR(500,"商品规格参数删除失败"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    GOODS_STOCK_DELETE_ERROR(500,"商品库存失败"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    CATEGORY_BRAND_SAVE_ERROR(500,"新增品牌分类中间表失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效文件类型"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_UPDATE_ERROR(500,"更新商品失败"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品id不能为空")
    ;

    private int code;
    private String msg;
}
