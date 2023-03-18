package org.wowtools.giscat.vector.rocksrtree.internal;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.wowtools.giscat.vector.rocksrtree.pojo.PointNd;
import org.wowtools.giscat.vector.rocksrtree.pojo.RectNd;

/**
 * Created by jcairns on 4/30/15.
 */
public interface RectBuilder {

    /**
     * Build a bounding rectangle for the given element
     *
     * @param t - element to bound
     * @return HyperRect impl for this entry
     */
    RectNd getBBox(RectNd t);


    /**
     * Build a bounding rectangle for given points (min and max, usually)
     *
     * @param p1 - first point (top-left point, for example)
     * @param p2 - second point (bottom-right point, for example)
     * @return HyperRect impl defined by two points
     */
    RectNd getMbr(PointNd p1, PointNd p2);
}
