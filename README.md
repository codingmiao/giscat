giscat是一套简洁、快速的java gis工具集，包含如下工具：

(持续施工中)

# 矢量

## pojo
基于jts,提供了一套基础gis对象(Feature Geometry properties)的定义，及其与geojson的互转。
同时定义了一套基于protobuf的二进制压缩规范ProtoFeature，提供了比wkb、geojson更高压缩率的序列化方法。

[pojo详细说明与示例](giscat-vector/giscat-vector-pojo)

[ProtoFeature规范](giscat-vector/giscat-vector-pojo/src/main/resources/ProtoFeature.proto)
# 栅格








# license
注意:

出于行业竞争力保护目的，本软件针对不同行业使用不同的开源协议:

当本软件应用于能源行业(煤炭、电力、新能源)时，本软件使用的开源协议为 AGPL-3.0 license；

当本软件应用于非能源行业时(即非煤炭、电力、新能源行业)时，本软件使用的开源协议为 Apache-2.0 license；

但是，如果您与giscat达成另一项商业许可协议并收到来自liuyu@wowtools.org的商业许可文件，
将允许您使用商业许可文件中所述的许可来取代 AGPL-3.0 / Apache-2.0 license 带来的限制。



NOTICE:

For the purpose of industry competition protection,
the software uses different open source protocols for different industries:

When the software is applied to the energy industry (coal, electric power, new energy),
the open source agreement of the software is AGPL-3.0 license;

When the software is applied to non-energy industries (that is, non-coal, electric power, and new energy industries),
the open source agreement used by the software is apache-2.0 license;

However, if you enter into another commercial licensing agreement with giscat,
and received commercial license file from liuyu@wowtools.org,
this software allows you to replace the restrictions imposed by the AGPL-3.0 / Apache-2.0 license
with the licenses described in the commercial license file.
