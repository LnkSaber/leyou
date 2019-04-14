package com.leyou.bid.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.bid.mapper.BidMapper;
import com.leyou.bid.pojo.Bid;
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

@Service
public class BidService {
    @Autowired
    private BidMapper bidMapper;
    public PageResult<Bid> queryVenderByPage(Integer page, Integer rows, Long uid, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Bid.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("title","%"+key+"%").andEqualTo("uid",uid);
        }
        else {
            example.createCriteria().andEqualTo("uid",uid);
        }

        List<Bid> list = bidMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BID_NO_FOUND);
        }
        //解析分页结果
        PageInfo<Bid> info =new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    public PageResult<Bid> queryAdminByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Bid.class);
        example.createCriteria().orLike("title","%"+key+"%");
        List<Bid> list = bidMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BID_NO_FOUND);
        }
        //解析分页结果
        PageInfo<Bid> info =new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    @Transactional
    public void updateTenderStateByAdmin(Long bidid) {
        Bid bid = bidMapper.selectByPrimaryKey(bidid);
        bid.setState(3);
        int count = bidMapper.updateByPrimaryKeySelective(bid);
        if (count != 1){
            throw new LyException(ExceptionEnum.BID_SAVE_ERROR);
        }
    }

    @Transactional
    public void saveBid(Bid bid) {
        bid.setBidid(null);
        bid.setState(0);
        int count = bidMapper.insert(bid);
        if (count != 1){
            throw new LyException(ExceptionEnum.BID_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateBidStateRemoveByVender(Long bidid) {
        Bid bid = bidMapper.selectByPrimaryKey(bidid);
        bid.setState(4);
        int count = bidMapper.updateByPrimaryKeySelective(bid);
        if (count != 1){
            throw new LyException(ExceptionEnum.BID_SAVE_ERROR);
        }
    }
}
