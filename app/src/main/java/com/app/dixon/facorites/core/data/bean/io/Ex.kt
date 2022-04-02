package com.app.dixon.facorites.core.data.bean.io

import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.google.gson.Gson

/**
 * 全路径：com.app.dixon.facorites.core.data.bean.io
 * 类描述：存储用到的扩展方法
 * 创建人：xuzheng
 * 创建时间：3/18/22 10:34 AM
 */

// bean -> json
fun BaseEntryBean.toJson(): String {
    val type = this::class.java.name
    val beanJson = Gson().toJson(this)
    return Gson().toJson(Pair(type, beanJson))
}

// json -> bean
fun String.toEntry(): BaseEntryBean? {
    val pair: Pair<String, String>? =
        Gson().fromJson(this, Pair::class.java) as? Pair<String, String>
    pair?.let {
        // 假如版本迭代中Bean更换了目录，在还原时可能会报ClassNotFoundException
        // Gson自身支持继承：
        // http://www.tastones.com/stackoverflow/gson/getting-started-with-gson/using_gson_with_inheritance/
        // 原理同样是保存ClassName..
        // 该问题其实无解，Gson转为的String要保存到本地，无法感知App层面Class的变化，除非手动做转换覆盖掉旧数据。
        val clazz: Class<*> = Class.forName(it.first)
        (Gson().fromJson(it.second, clazz) as? BaseEntryBean)?.let { bean ->
            return bean
        }
    }
    return null
}