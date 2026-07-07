package com.daisy.health.mapper;

import com.daisy.health.model.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select id, nickname, real_name as realName, gender, birthday, phone, id_card as idCard, address, height, weight, blood_type as bloodType, chronic_disease as chronicDisease, status, created_at as createdAt, updated_at as updatedAt from user where (#{keyword} is null or nickname like concat('%', #{keyword}, '%') or real_name like concat('%', #{keyword}, '%')) order by created_at desc limit #{offset}, #{pageSize}")
    List<UserEntity> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);
}
