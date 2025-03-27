package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Answer;

import java.util.List;

@Mapper
public interface AnswerMapper{
    int insertAnswer(Answer answer);
    List<Answer> selectAllAnswersByResponseId(Long responseId);
    int deleteAnswerByResponseId(Long responseId);
}
