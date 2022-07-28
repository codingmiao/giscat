giscat是一套简洁、快速的java gis工具集，包含如下工具：

(持续施工中)

# 矢量

## pojo

基于jts,提供了一套基础gis对象(Feature Geometry properties)的定义，及其与geojson的互转。
同时定义了一套基于protobuf的二进制压缩规范ProtoFeature，提供了比wkb、geojson更高压缩率的序列化方法。

[pojo详细说明与示例](giscat-vector/giscat-vector-pojo)

[ProtoFeature规范](giscat-vector/giscat-vector-pojo/src/main/resources/ProtoFeature.proto)

## util

一系列处理矢量数据的工具，包含坐标转换、图形分析裁剪等
详见[测试用例](giscat-vector/giscat-vector-util/src/test/java)

## mvt

矢量瓦片生成与解析工具
[基于springboot编写矢量瓦片服务示例](https://blog.wowtools.org/2022/04/28/2022-04-28-mapbox-gl-tutorial-8/)

# 栅格

# license

本软件采用自定义协议，请阅读[LICENSE文件](https://github.com/codingmiao/giscat/blob/main/LICENSE)
