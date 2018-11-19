1. 安装 protocol buffer编译工具， 验证: protoc 命令可用
2. 配置protocl buffer maven插件： org.xolstice.maven.plugins：protobuf-maven-plugin
3. 编辑proto格式文件
4. 依赖： com.google.protobuf：protobuf-java
5. 编译proto生个code: mvn clean compile
6. 开发调用