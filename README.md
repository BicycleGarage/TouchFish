# TouchFish
~~此分支固定不变，为首次稳定版本分支  
需版本迭代时，可从此分支新建分支，或从其他版本新建~~

# 功能介绍

1. 显示全年假期
2. 计算发薪日
3. 下班倒计时
4. 设置窗口透明度

# 假期数据来源

假期需手动添加json文件到resource  
存储桶应定期更新json，从注释掉的url获取

# 打包流程
1. 执行打包指令
`mvn clean package`
2. 手动复制jre文件夹，到target下
3. 将以下3个文件或目录，打包zip发送用户：
   1. target/TouchFish.jar
   2. target/TouchFish-1.0-SNAPSHOT-jar-with-dependencies.jar
   3. target/jre