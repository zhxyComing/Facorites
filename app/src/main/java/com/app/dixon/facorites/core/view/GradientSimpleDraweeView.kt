package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import androidx.palette.graphics.Palette
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.request.BasePostprocessor
import com.facebook.imagepipeline.request.ImageRequest.RequestLevel
import com.facebook.imagepipeline.request.ImageRequestBuilder


/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：
 * 创建人：xuzheng
 * 创建时间：4/14/22 5:47 PM
 */
class GradientSimpleDraweeView : SimpleDraweeView {

    constructor(context: Context?, hierarchy: GenericDraweeHierarchy?) : super(context, hierarchy) {}
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        var adcb = controllerBuilder
        adcb = adcb.setOldController(controller).setCallerContext(callerContext)
        var imageRequestBuilder: ImageRequestBuilder? = null
        var imageDecodeOptions: ImageDecodeOptions? = null
        imageDecodeOptions = ImageDecodeOptions.newBuilder().build()
        /** 构建自定义ImageRequest，以便于对即将渲染的Bitmap进行修改  */
        imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
            .setImageDecodeOptions(imageDecodeOptions)
            .setRotationOptions(RotationOptions.autoRotate())
            .setLocalThumbnailPreviewsEnabled(true)
            .setLowestPermittedRequestLevel(RequestLevel.FULL_FETCH)
            .setProgressiveRenderingEnabled(true)
        /** 设置Bitmap处理器  */
        imageRequestBuilder.postprocessor = object : BasePostprocessor() {
            override fun process(bitmap: Bitmap) {
                super.process(bitmap)
                // 获取bitmap中活跃颜色值
                var color = Palette.from(bitmap).generate().getVibrantColor(Color.parseColor("#666666"))
                // 防止颜色值过浅，对其进行条件变暗处理
                val red = color shr 16 and 0xFF
                val green = color shr 8 and 0xFF
                val blue = color and 0xFF
                if (red >= 0xA0 && green >= 0xA0 && blue >= 0xA0) {
                    color = Color.rgb(Math.round(red * 0.8f), Math.round(green * 0.8f), Math.round(blue * 0.8f))
                }
                // 创建线性渐变蒙层，这就是我们需要叠加上去的遮罩蒙层拉
                // 其参数为以图片y的中点为起点，图片宽度的75%处为终点，起点颜色为
                // 上面运算后的颜色，终点为透明色进行渐变，渐变模式为CLAMP
                val linearGradient: LinearGradient = LinearGradient(
                    0f, bitmap.height / 2f,
                    bitmap.width * 0.75f, bitmap.height / 2f, color, Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
                val paint = Paint()
                paint.shader = linearGradient

                // 将bitmap捆到画板上，并在画板上绘制蒙层遮罩，高度为图片高度，长度为终点（即图片宽度的75%）
                val canvas = Canvas(bitmap)
                canvas.drawRect(0f, 0f, bitmap.width * 0.75f, bitmap.height.toFloat(), paint)
            }
        }
        val imageRequest = imageRequestBuilder.build()
        adcb.imageRequest = imageRequest
        val controller: DraweeController = adcb.build()
        setController(controller)
    }
}
