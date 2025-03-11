package top.mygld.zhihuiwen_server.service.impl;

import top.mygld.zhihuiwen_server.pojo.User;

import java.util.List;

public interface UserService {
    public void insertUser(User user);
    public User selectUserByUsername(String username);
    public User selectUserByEmail(String email);
    public User selectUserByUsernameAndPassword(String username,String password);
    public Long getUserIdByUsername(String username);
    public User getUserById(Long id);
}
