# myblog
1. 新增开关控制(注册开关, 发文开关, 评论开发)
    2. 后台重写, 替换了所有后台页面功能更完善
    3. 上传图片添加更多支持(本地/又拍云/阿里云/七牛云), 详情见后台系统配置
    4. 使用后台为spring-boot
    5. 调整模板静态资源引用,方便扩展
    6. 表名调整, 旧版本升级时请自行在数据库重命名
    7. config(改为options), 可在applicaiton.yaml设置默认配置, 启动后将以options中配置为准
    8. 支持后台设置主题
    9. 去除了本地文件上传目录配置, 改为自动取项目运行目录(会在jar同级目录生成storeage和indexes目录)
	10. 替换表单验证插件, 评论表情改为颜文字
    11. 我的主页和Ta人主页合并
    12. 优化了图片裁剪功能
    13. 邮件服务后台可配
    14. 新增标签页
    15. 新增注册邮箱验证开关(需要手动删除之前的 mto_security_code 表)
