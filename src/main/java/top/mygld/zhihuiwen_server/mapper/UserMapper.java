package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.User;

import java.util.List;

@Mapper
public interface UserMapper {
    public int insertUser(User user);
    public List<User> selectUserByUsername(String username);
    public List<User> selectUserByEmail(String email);
    public List<User> selectUserByUsernameAndPassword(String username,String password);
    public List<User> selectUserById(Long id);
    public void updateUserProfile(User user);
    public void updateUserPassword(User user);
    public void updateUserEmail(User user);
}
