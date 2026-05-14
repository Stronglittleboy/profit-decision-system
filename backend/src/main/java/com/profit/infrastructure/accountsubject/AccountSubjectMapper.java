package com.profit.infrastructure.accountsubject;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface AccountSubjectMapper extends BaseMapper<AccountSubjectEntity> {

    @Select("SELECT id FROM account_subject WHERE parent_id = #{parentId}")
    List<Long> selectChildIds(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(1) FROM account_subject WHERE parent_id = #{parentId}")
    int countByParentId(@Param("parentId") Long parentId);
}
