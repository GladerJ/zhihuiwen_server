package top.mygld.zhihuiwen_server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.UserMapper;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.UserService;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public void insertUser(User user) {
        if(user.getCreatedAt() == null) user.setCreatedAt(new Date());
        if(user.getUpdatedAt() == null) user.setUpdatedAt(new Date());
        userMapper.insertUser(user);
    }
    @Override
    public User selectUserByUsername(String username) {
        List<User> users = userMapper.selectUserByUsername(username);
        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public User selectUserByEmail(String email) {
        List<User> users = userMapper.selectUserByEmail(email);
        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public User selectUserByUsernameAndPassword(String username, String password) {
        List<User> users = userMapper.selectUserByUsernameAndPassword(username,password);
        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return userMapper.selectUserByUsername(username).get(0).getId();
    }

    @Override
    public User getUserById(Long id) {
        List<User> users = userMapper.selectUserById(id);
        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public void updateUserProfile(User user) {
        userMapper.updateUserProfile(user);
    }

    @Override
    public void updateUserPassword(User user) {
        userMapper.updateUserPassword(user);
    }

    @Override
    public void updateUserEmail(User user) {
        userMapper.updateUserEmail(user);
    }
}
