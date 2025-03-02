package top.mygld.zhihuiwen_server.service.impl;

import top.mygld.zhihuiwen_server.pojo.User;

import java.util.List;

public interface UserService {
    public void insertUser(User user);
    public List<User> selectUserByUsername(String username);
    public List<User> selectUserByEmail(String email);
    public List<User> selectUserByUsernameAndPassword(String username,String password);
}
