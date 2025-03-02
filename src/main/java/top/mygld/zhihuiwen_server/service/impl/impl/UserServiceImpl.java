package top.mygld.zhihuiwen_server.service.impl.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.UserMapper;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.impl.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public void insertUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public List<User> selectUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public List<User> selectUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }

    @Override
    public List<User> selectUserByUsernameAndPassword(String username, String password) {
        return userMapper.selectUserByUsernameAndPassword(username,password);
    }

}
