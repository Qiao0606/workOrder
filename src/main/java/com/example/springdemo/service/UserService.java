package com.example.springdemo.service;

import com.example.springdemo.model.User;

import java.util.List;

/**
 * User业务层接口：定义所有用户相关的业务方法
 * 职责：只声明方法，不写具体逻辑（由实现类完成）
 */
public interface UserService {

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 单个用户信息
     */
    User getUserById(Long id);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> listAllUsers();

    /**
     * 新增用户
     * @param user 待新增的用户信息（name/age/email必填）
     * @return 新增成功的用户ID
     */
    Long addUser(User user);

    /**
     * 修改用户信息
     * @param user 待修改的用户信息（id必填，其他字段选填）
     * @return 是否修改成功（true=成功，false=失败）
     */
    boolean updateUser(User user);

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 是否删除成功（true=成功，false=失败）
     */
    boolean deleteUser(Long id);
}