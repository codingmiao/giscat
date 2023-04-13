/*****************************************************************
 *  Copyright (c) 2022- "giscat by 刘雨 (https://github.com/codingmiao/giscat)"
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.wowtools.giscat.vector.mvt;

import java.util.ArrayList;
import java.util.List;

/**
 * mvt feature
 *
 * @author liuyu
 * @date 2022/4/24
 */
public final class MvtFeature {
    //几何对象类型
    public VectorTile.Tile.GeomType geomType;
    //几何对象commands
    public List<Integer> commands;

    // tags用以映射属性
    public ArrayList<Integer> tags;
}
