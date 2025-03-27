package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Response;

import java.util.List;

@Mapper
public interface ResponseMapper {
    public int insertResponse(Response response);
    public List<Response> selectAllResponsesByQuestionnaireId(Long questionnaireId);
    int deleteResponseByQuestionnaireId(Long questionnaireId);
}
