package com.app.dixon.facorites.core.view

import android.content.Context
import android.net.Uri
import android.view.View
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.VideoEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.FileIOService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import com.google.android.exoplayer2.Player
import com.jarvanmo.exoplayerview.media.SimpleMediaSource
import kotlinx.android.synthetic.main.app_dialog_create_video_entry_content.*
import java.util.*

// 创建用构造函数
class CreateVideoEntryDialog(
    context: Context,
    private val uri: Uri,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"),
) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_video_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    // 视频
    private var tempVideoPath: String? = null // 新导入的临时视频 如果后续选择取消 则要删除掉这些新导入的视频

    // 是否点击了确认
    private var hasSave = false

    override fun initDialog() {
        initCommonLogic()
    }

    private fun initCommonLogic() {
        llContainer.normalFont()

        backUi {
            saveVideoToLocal()
        }

        tvVideoLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的视频有以下特性：\n\n1.应用外（如相册）不可见；\n" +
                        "\n2.删除手机（相册）的原视频不会影响到收藏视频。",
                title = "视频收藏提示"
            ).show()
        }
        tvCreate.setOnClickListener {
            saveVideo()
        }
        // 分类的下拉列表
        initSpinner()
    }

    private fun saveVideoToLocal() {
        val dialog = ProgressDialog(context, "视频导入中..").apply { show() }
        FileIOService.saveFile(FileIOService.FileType.VIDEO, uri, object : ProgressCallback<String> {
            override fun onSuccess(data: String) {
                dialog.dismiss()
                ToastUtil.toast("导入视频成功")
                tempVideoPath = data

                // 打开播放器进行播放
                videoView.show()
                tvVideoImportTip.hide()
                playVideo(data)
            }

            override fun onFail(msg: String) {
                dialog.dismiss()
                ToastUtil.toast("导入视频失败 $msg")
            }

            override fun onProgress(progress: Int) {
                dialog.setProgress(progress)
            }
        })
    }

    // 保存图片
    private fun saveVideo() {
        val title = etVideoTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && !tempVideoPath.isNullOrEmpty()) {
            DataService.createEntry(
                VideoEntryBean(
                    path = tempVideoPath!!,
                    title = title,
                    date = Date().time,
                    belongTo = categoryId,
                ),
                callback
            )
            hasSave = true
            dismiss()
        } else {
            // 未填数据提示
            etVideoTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (tempVideoPath.isNullOrEmpty()) {
                tvVideoSubLayout.shakeTip()
            }
        }
    }

    // 下拉选择框
    private fun initSpinner() {
        val expendInfoList = mutableListOf<CustomSpinner.ExpandInfo<CategoryInfoBean>>()
        DataService.getCategoryList().forEach {
            expendInfoList.add(CustomSpinner.ExpandInfo(it.name, it.bgPath, it))
        }
        categoryChoose.setData(expendInfoList)
        categoryChoose.setShowPos(CustomSpinner.ShowPos.TOP)
    }

    override fun onDetachedFromWindow() {
        // 如果视频在播放 则停止播放并释放资源
        videoView.stop()
        videoView.releasePlayer()
        if (!hasSave) {
            deleteExpiredImportVideo()
        }
        super.onDetachedFromWindow()
    }

    // 删除过期的导入图片
    private fun deleteExpiredImportVideo() {
        tempVideoPath?.let {
            FileIOService.deleteFile(it)
        }
    }

    private fun playVideo(path: String) {
        val mediaSource = SimpleMediaSource(Uri.parse(path)) //uri also supported
        videoView.play(mediaSource, 0) //play from a particular position
        videoView.player.repeatMode = Player.REPEAT_MODE_ALL
        videoView.changeWidgetVisibility(R.id.exo_player_enter_fullscreen, View.GONE)
    }
}