package com.zz.zmovie.mapper;

//import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.Test;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper extends BaseMapper<Test> {
}
