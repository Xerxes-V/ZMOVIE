package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.Collections;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface CollectionsMapper extends BaseMapper<Collections> {

    //当唯一索引冲突时则取消插入操作
    @Insert(" INSERT IGNORE INTO collections (  movie_id, user_id ,collect_time ) VALUES ( #{movie_id},#{user_id},#{time} )")
    public int insertCollection(Long movie_id, Long user_id, Date time);

}
