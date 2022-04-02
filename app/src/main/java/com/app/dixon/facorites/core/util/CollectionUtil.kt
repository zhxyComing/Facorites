package com.app.dixon.facorites.core.util

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：集合工具类
 * 创建人：xuzheng
 * 创建时间：3/31/22 11:20 AM
 */
object CollectionUtil {

    /**
     * 插入数据到队首 如果没到最大限制则增加 达到最大限制则移除队尾元素
     *
     * >= 最大数量限制，先交换后修改位置0数据
     * A B C D
     * B A C D
     * C A B D
     * D A B C
     * 始终拿第一位与第index+1位交换 交换 n - 1 次
     *
     * < 最大数量限制 先添加后交换
     * B C A
     * A C B
     * A B C
     * 始终拿末尾位与第index位交换 交换 n - 1 次
     */
    fun <T> insertDataToHead(list: MutableList<T>, data: T, maxSize: Int) {
        when {
            list.size == 0 -> {
                // 空 直接添加
                list.add(data)
            }
            list.size < maxSize -> {
                // 小于最大数量限制 先添加到末尾 再交换调整顺序
                list.add(data)
                val lastPos = list.size - 1
                for (index in 0 until lastPos) {
                    val temp = list[lastPos]
                    list[lastPos] = list[index]
                    list[index] = temp
                }
            }
            list.size >= maxSize -> {
                // 大于等于最大值 先交换调整顺序 再添加数据到第一位
                for (index in 0 until list.size - 1) {
                    val temp = list[0]
                    list[0] = list[index + 1]
                    list[index + 1] = temp
                }
                list[0] = data
            }
        }
    }
}