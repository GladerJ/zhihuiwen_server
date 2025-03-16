package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Response;

@Mapper
public interface ResponseMapper {
    public int insertResponse(Response response);
}
