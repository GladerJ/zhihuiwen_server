package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Answer;

@Mapper
public interface AnswerMapper{
    int insertAnswer(Answer answer);
}
