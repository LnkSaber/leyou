package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import sun.awt.image.GifImageDecoder;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group =new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list=specGroupMapper.select(group);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }


    @Transactional
    public void addSpecGroup(SpecGroup specGroup) {
        int count = specGroupMapper.insert(specGroup);
        if(count !=1){
            throw  new LyException(ExceptionEnum.SPEC_GROUP_ADD_ERROR);
        }
    }

    @Transactional
    public void addSpecParam(SpecParam specParam) {
        int count = specParamMapper.insert(specParam);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPEC_PARAM_ADD_ERROR);
        }
    }

    @Transactional
    public void UpdateSpecParam(SpecParam specParam) {
        int count = specParamMapper.updateByPrimaryKeySelective(specParam);
        if(count !=1){
            throw new LyException(ExceptionEnum.SPEC_PARAM_UPDATE_ERROR);
        }
    }

    @Transactional
    public void UpdateSpecGroup(SpecGroup specGroup) {
        int count = specGroupMapper.updateByPrimaryKeySelective(specGroup);
        if(count !=1){
            throw new LyException(ExceptionEnum.SPEC_GROUP_UPDATE_ERROR);
        }
    }

    @Transactional
    public void DeleteSpecParamById(Long id) {
        int delete = specParamMapper.deleteByPrimaryKey(id);
        if(delete!=1){
            throw new LyException(ExceptionEnum.SPEC_PARAM_DELETE_ERROR);
        }
    }

    @Transactional
    public void DeleteSpecGroupByGid(Long gid) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        List<SpecParam> specParamList = specParamMapper.select(specParam);

        //删除规格组
        int groupDelete = specGroupMapper.deleteByPrimaryKey(gid);
        if (groupDelete==1){
            if (!CollectionUtils.isEmpty(specParamList)){
                List<Long> ids = specParamList.stream().map(SpecParam::getId).collect(Collectors.toList());
                int deleteByIdlist = specParamMapper.deleteByIdList(ids); //批量删除，删了多少条就返回多少，因为前面判断集合不为空了，故不为0
                if (deleteByIdlist ==0 ){
                    throw new LyException(ExceptionEnum.SPEC_PARAM_DELETE_ERROR);
                }
            }
        }
        else {
            throw new LyException(ExceptionEnum.SPEC_GROUP_DELETE_ERROR);
        }
    }
}
