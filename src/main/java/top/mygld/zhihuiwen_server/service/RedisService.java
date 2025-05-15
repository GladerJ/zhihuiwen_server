package top.mygld.zhihuiwen_server.service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface RedisService {
    // 基础操作
    void setValue(String key, Object value);
    Object getValue(String key);
    void deleteValue(String key);

    // 带过期时间的操作
    void setValueWithExpiry(String key, Object value, long timeout, TimeUnit timeUnit);

    // 批量操作
    void deleteBatch(Collection<String> keys);

    // 存在性检查
    Boolean hasKey(String key);

    // 连接检查（可选）
    String ping();

    // 新增：根据前缀批量删除
    void deleteByPrefix(String prefix);
}
