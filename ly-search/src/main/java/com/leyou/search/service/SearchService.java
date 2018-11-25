package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodsRespository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private GoodsRespository goodsRespository;
    public Goods buildGoods(Spu spu){

        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        List<String> categoryName = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all=spu.getTitle() + StringUtils.join(categoryName," ") + brand.getName();


        //查询每个sku的价格
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }


//        Set<Long> skuPriceList = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());


        //对skuList的字段做简化处理
        List<Map<String,Object>> skus=new ArrayList<>();
        //价格集合
        Set<Long> skuPriceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String,Object> map =new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
            //处理价格
            skuPriceList.add(sku.getPrice());
        }



        //查询规格参数
        List<SpecParam> specParams = specClient.queryParamByList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long ,List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});



        //规格参数,key规格参数的名字，值是规格参数的值
        Map<String,Object> specs =new HashMap<>();

        for (SpecParam param : specParams) {
            //规格名称
            String key = param.getName();
            Object value=null;
            //判断是否通用属性
            if (param.getGeneric()){
                value =genericSpec.get(param.getId());
                //判断是否为数值类型
                if (param.getNumeric()){
                    //处理成段
                  value= chooseSegment(value.toString(),param);
                }
            }
            else {
                value = specialSpec.get(param.getId());
            }
            //存入map
            specs.put(key,value);
        }


        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);// 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(skuPriceList);// 所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skus));// 所有的sku集合的json格式
        goods.setSpecs(specs);// 所有可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        Integer page = request.getPage()-1;
        Integer size = request.getSize();
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //1，结果过滤 （查询中返回的字段太多了过滤其他字段，只要id,sebtitle,skus）
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //2.分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //3.过滤
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()));
        //4.查询
        Page<Goods> result = goodsRespository.search(queryBuilder.build());

        //5.解析结果
        long total = result.getTotalElements();
        Integer totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();

        return new PageResult<>(total,totalPages,goodsList);
    }
}
