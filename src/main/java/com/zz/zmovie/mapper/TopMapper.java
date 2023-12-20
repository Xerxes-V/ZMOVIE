package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.Top;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TopMapper extends BaseMapper<Top> {
//    @Select("SELECT movie_id from movie order by rating_num desc LIMIT 0,10;")
//    List<Long> getHottest

}
