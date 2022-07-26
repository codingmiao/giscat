package org.wowtools.giscat.vector.mbexpression;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 表达式名称
 *
 * @author liuyu
 * @date 2022/7/26
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpressionName {
    String value();
}
