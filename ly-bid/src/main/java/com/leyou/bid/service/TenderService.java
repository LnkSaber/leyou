package com.leyou.bid.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.bid.mapper.BidMapper;
import com.leyou.bid.mapper.TenderMapper;
import com.leyou.bid.pojo.Bid;
import com.leyou.bid.pojo.Tender;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenderService {

    @Autowired
    private TenderMapper tenderMapper;

    @Autowired
    private BidMapper bidMapper;

    public PageResult<Tender> queryUserTanderByPage(Long uid, Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Tender.class);

        example.createCriteria().andEqualTo("uid",uid);

        //查询
        List<Tender> list = tenderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.TENDER_NO_FOUND);
        }
        List<Long> tenderIds = list.stream().map(Tender::getTenderid).collect(Collectors.toList());
        int num = 0;
        for (Long tenderId : tenderIds) {
            Bid bid = new Bid();
            bid.setTenderid(tenderId);
            List<Bid> bidList = bidMapper.select(bid);
            if ( !CollectionUtils.isEmpty(bidList)) {
                list.get(num).setBids(bidList);
            }
            num++;
        }

        //解析分页结果
        PageInfo<Tender> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(), list);

    }

    private PageResult<Tender> getTenderPageResult(Example example) {
        //查询
        List<Tender> list = tenderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.TENDER_NO_FOUND);
        }
        //解析分页结果
        PageInfo<Tender> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(), list);
    }

    public PageResult<Tender> queryVenderByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Tender.class);

        example.createCriteria().orEqualTo("state",0);

        //查询
        return getTenderPageResult(example);
    }

    public PageResult<Tender> queryAdminByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key){
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Tender.class);

        example.createCriteria();

        //查询
        return getTenderPageResult(example);
    }

    @Transactional
    public void updateTenderStateByAdmin(Long tenderid) {
        Tender tender = tenderMapper.selectByPrimaryKey(tenderid);
        tender.setState(3);
        int count = tenderMapper.updateByPrimaryKeySelective(tender);
        if (count != 1){
            throw new LyException(ExceptionEnum.TENDER_UPDATE_ERROR);
        }
    }

    @Transactional
    public void saveTender(Tender tender) {
        tender.setTenderid(null);
        tender.setTotalpay(tender.getPrice()*tender.getNum());
        tender.setState(0);
        int count = tenderMapper.insert(tender);
        if (count != 1){
            throw new LyException(ExceptionEnum.TENDER_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateTenderRemoveStateByUser(Long tenderid) {
        Tender tender = tenderMapper.selectByPrimaryKey(tenderid);
        //修改招标状态码
        tender.setState(4);
        int count = tenderMapper.updateByPrimaryKeySelective(tender);
        if (count != 1){
            throw new LyException(ExceptionEnum.TENDER_UPDATE_ERROR);
        }
        //修改于招标对应的投标状态码
        Bid bid = new Bid();
        bid.setTenderid(tenderid);
        List<Bid> bidList = bidMapper.select(bid);
        for (Bid bidOne : bidList) {
            bidOne.setState(6);
            count = bidMapper.updateByPrimaryKeySelective(bidOne);
            if (count != 1){
                throw new LyException(ExceptionEnum.TENDER_UPDATE_ERROR);
            }
        }
    }

    @Transactional
    public void updateTenderRemoveChooseByUser(Long tenderid, Long bidid) {
        Tender tender = tenderMapper.selectByPrimaryKey(tenderid);
        //修改招标状态码
        tender.setState(1);
        tender.setFinalbidid(bidid);
        int count = tenderMapper.updateByPrimaryKeySelective(tender);
        if (count != 1){
            throw new LyException(ExceptionEnum.TENDER_UPDATE_ERROR);
        }
        //修改投标状态码
        Bid bid = new Bid();
        bid.setTenderid(tenderid);
        List<Bid> bidList = bidMapper.select(bid);
        for (Bid bidOne : bidList) {
            bidOne.setState(5);
            if (bidOne.getBidid().equals(bidid)){
                bidOne.setState(1);
            }
            count = bidMapper.updateByPrimaryKeySelective(bidOne);
            if (count != 1){
                throw new LyException(ExceptionEnum.TENDER_UPDATE_ERROR);
            }
        }

    }
}
