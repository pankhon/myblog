package com.mtons.mblog.modules.service;

import java.util.Map;

/**
 * 邮箱验证服务接口
 */
public interface MailService {
    void config();
    void sendTemplateEmail(String to, String title, String template, Map<String, Object> content);
}
