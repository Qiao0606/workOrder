package com.example.springdemo.mapper;

import com.example.springdemo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// 标记为MyBatis Mapper接口（或在主启动类加@MapperScan）
@Mapper
@Repository
public interface UserMapper {
    // 根据ID查询用户
    User getUserById(Long id);

    // 查询所有用户
    List<User> listAllUsers();

    // 新增用户
    int addUser(User user);

    // 修改用户
    int updateUser(User user);

    // 删除用户
    int deleteUser(Long id);
}