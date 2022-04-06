giscat是一套简洁、快速的java gis工具集，包含如下工具：

(持续施工中)

# 矢量

## pojo
基于jts,提供了一套基础gis对象(Feature Geometry properties)的定义，及其与geojson的互转。
同时定义了一套基于protobuf的二进制压缩规范ProtoFeature，提供了比wkb、geojson更高压缩率的序列化方法。

[pojo详细说明与示例](giscat-vector/giscat-vector-pojo)

[ProtoFeature规范](giscat-vector/giscat-vector-pojo/src/main/resources/ProtoFeature.proto)
# 栅格
