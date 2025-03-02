package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.User;

import java.util.List;

@Mapper
public interface UserMapper {
    public void insertUser(User user);
    public List<User> selectUserByUsername(String username);
    public List<User> selectUserByEmail(String email);
    public List<User> selectUserByUsernameAndPassword(String username,String password);
}
