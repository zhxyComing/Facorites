package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：文件夹类型 多级文件夹中的子文件夹
 * 创建人：xuzheng
 * 创建时间：2022/4/26 15:01
 *
 * CategoryInfoBean 应当作为 BaseEntryBean 的子类
 * 现在改数据成本较大，所以包装了一层，这导致：
 * CategoryEntryBean.belongTo == CategoryInfoBean.belongTo
 * CategoryEntryBean.date == CategoryInfoBean.id
 * 可以这样理解：
 * CategoryEntryBean 是条目
 * CategoryInfoBean 是分类
 * 只不过该分类恰好也是一个条目
 */
class CategoryEntryBean(val categoryInfoBean: CategoryInfoBean, date: Long, belongTo: Long, star: Boolean = false) : BaseEntryBean(date, belongTo, star) {

    override fun toString(): String {
        return "CategoryEntryBean(categoryInfoBean=$categoryInfoBean) ${super.toString()}"
    }
}