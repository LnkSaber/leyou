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
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRespository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private ElasticsearchTemplate template;

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
        //3.搜索条件
        QueryBuilder basicQuery =buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()));

        //4.聚合分类和品牌
        //4.1聚合分类
        String categoryAggName ="category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //4.2聚合品牌
        String brandAggName ="brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        //5.查询
//        Page<Goods> result = goodsRespository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //6.1解析分页结果
        long total = result.getTotalElements();
        Integer totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //6.2解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories =parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands=parseBrandAgg(aggs.get(brandAggName));

        //7.完成规格参数聚合
        List<Map<String,Object>> specs=null;
        if (categories.size() ==1&& categories!=null){
            //商品分类存在并且数量为1，可以聚合参数
            specs =buildSpecification(categories.get(0).getId(),basicQuery);
        }

        return new SearchResult(total,totalPages,goodsList,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder =QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //过滤条件
        Map<String, String> map = request.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            //处理key
            if(!"cid3".equals(key) && ! "brandId".equals(key)){
                queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
            }
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecification(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs =new ArrayList<>();
        //1.查询需要聚合的规格参数
        List<SpecParam> params = specClient.queryParamByList(null, cid, true);
        //2.聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1带上查询条件
        queryBuilder.withQuery(basicQuery);
        //2.2聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //3.获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4.解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            //规格参数名
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //准备map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList()));

            specs.add(map);
        }

        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> Ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            System.out.println(Ids);
            List<Brand> brands = brandClient.queryBrandByIds(Ids);
            return brands;
        } catch (Exception e) {
            log.error("[搜索服务] 查询品牌异常" +e);
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> Ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(Ids);
            return categories;
        } catch (Exception e) {
            log.error("[搜索服务] 查询分类异常" +e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRespository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRespository.deleteById(spuId);
    }
}
