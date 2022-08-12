package treeview.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import treeview.adapter.DrawInfo;
import treeview.adapter.TreeViewHolder;
import treeview.cache_pool.PointPool;
import treeview.layout.TreeLayoutManager;
import treeview.model.NodeModel;
import treeview.util.DensityUtils;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/18  16:47
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * AngledLine between two nodes
 */
public class AngledLine extends BaseLine {
    public static final int DEFAULT_LINE_WIDTH_DP = 3;
    private int lineColor = Color.parseColor("#055287");
    private int lineWidth = DEFAULT_LINE_WIDTH_DP;
    public AngledLine() {
        super();
    }

    public AngledLine(int lineColor, int lineWidth_dp) {
        this();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth_dp;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
    @Override
    public void draw(DrawInfo drawInfo) {
        Canvas canvas = drawInfo.getCanvas();
        TreeViewHolder<?> fromHolder = drawInfo.getFromHolder();
        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
        Paint mPaint = drawInfo.getPaint();
        Path mPath = drawInfo.getPath();
        int layoutType = drawInfo.getLayoutType();
        int spacePeerToPeer = drawInfo.getSpacePeerToPeer();
        int spaceParentToChild = drawInfo.getSpaceParentToChild();

        //get view and node
        View fromView = fromHolder.getView();
        NodeModel<?> fromNode = fromHolder.getNode();
        View toView = toHolder.getView();
        NodeModel<?> toNode = toHolder.getNode();
        Context context = fromView.getContext();

        //set paint
        mPaint.reset();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,lineWidth));
        mPaint.setAntiAlias(true);

        //setPath
        mPath.reset();
        if(layoutType== TreeLayoutManager.LAYOUT_TYPE_HORIZON_RIGHT){
            if(toView.getLeft()-fromView.getRight()<spaceParentToChild){
                return;
            }
            PointF startPoint = PointPool.obtain(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f);
            PointF point1 = PointPool.obtain(startPoint.x+spaceParentToChild/3f,startPoint.y);
            PointF endPoint =  PointPool.obtain(toView.getLeft(),(toView.getTop()+toView.getBottom())/2f);
            PointF point2 = PointPool.obtain(startPoint.x+spaceParentToChild/3f,endPoint.y);
            mPath.moveTo(startPoint.x,startPoint.y);
            mPath.lineTo(point1.x,point1.y);
            mPath.lineTo(point2.x,point2.y);
            mPath.lineTo(endPoint.x,endPoint.y);

            //do not forget release
            PointPool.free(startPoint);
            PointPool.free(point1);
            PointPool.free(point2);
            PointPool.free(endPoint);
        }else if(layoutType== TreeLayoutManager.LAYOUT_TYPE_VERTICAL_DOWN){
            if(toView.getTop()-fromView.getBottom()<spaceParentToChild){
                return;
            }
            PointF startPoint =  PointPool.obtain((fromView.getLeft()+fromView.getRight())/2f,fromView.getBottom());
            PointF point1 = PointPool.obtain(startPoint.x,startPoint.y+spaceParentToChild/3f);
            PointF endPoint =  PointPool.obtain((toView.getLeft()+toView.getRight())/2f,toView.getTop());
            PointF point2 = PointPool.obtain(endPoint.x,startPoint.y+spaceParentToChild/3f);
            mPath.moveTo(startPoint.x,startPoint.y);
            mPath.lineTo(point1.x,point1.y);
            mPath.lineTo(point2.x,point2.y);
            mPath.lineTo(endPoint.x,endPoint.y);

            //do not forget release
            PointPool.free(startPoint);
            PointPool.free(point1);
            PointPool.free(point2);
            PointPool.free(endPoint);
        }else{
            return;
        }

        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
