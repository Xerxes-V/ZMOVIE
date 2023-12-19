package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.User;
import com.zz.zmovie.vo.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取用户信息：
     */

    @Select("SELECT  u.username,u.account,u.avatar,mc.movie_comment_num,c.comment_num  from user u LEFT JOIN\t\n" +
            "(SELECT count(id)   as 'movie_comment_num',user_id,id from movie_comments WHERE user_id = #{user_id})mc on mc.user_id = u.id\n" +
            "\n" +
            "left JOIN  \n" +
            "(SELECT count(id) as 'comment_num',user_id,id from comments WHERE user_id = #{user_id})c on c.user_id = u.id\n" +
            "\n" +
            "WHERE u.id = #{user_id} ;")
    public UserData getUserData(Long user_id);

//    /**
//     * 更新用户封禁状态
//     */
//
//    @Update("UPDATE user set `status` = `status` + #{like} WHERE id = #{commentId} ")
//    int updateLikes(Long commentId,int like);
}
