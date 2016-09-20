package com.lipy.linechart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 双线折线图
 * Created by Lipy on 16/4/7.
 */
public class DoubleLineChartView extends View implements View.OnTouchListener {

    private Surface surface;
    private String[] XLabels;
    private String[] YLabels;
    private BigDecimal perlabelBig1;
    private BigDecimal ylabelBig1;
    private float[] data;
    private float[] data1;
    private static Bitmap mBitmapBg;
    private Bitmap mCreatBitmap;

    public DoubleLineChartView(Context context) {
        this(context, null);
    }

    public DoubleLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surface = new Surface();
        if (mBitmapBg == null) {
            mBitmapBg = BitmapFactory.decodeResource(getResources(), R.drawable
                    .lined_diagram);
        }
        surface.density = getResources().getDisplayMetrics().density;
        setBackgroundColor(surface.bgColor);
        setOnTouchListener(this);

    }

    public DoubleLineChartView(Context context, AttributeSet attrs, int defStyle) {
        this(context, null);
    }

    private int YCoord(float y0) {
        try {

            return surface.YLength - 2 - new BigDecimal(y0).subtract(ylabelBig1)
                    .divide(perlabelBig1, new MathContext(4, RoundingMode.HALF_EVEN)).multiply
                            (new BigDecimal(surface.YLength - surface.YScale)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return (int) y0;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (XLabels == null || YLabels == null) {
            return;
        }
        surface.XScale = (surface.XLength / (XLabels.length)) + Utils.dp2px(getContext(), 6);
        surface.YScale = surface.YLength / (YLabels.length);
        int startX = surface.XPoint - Utils.dp2px(getContext(), 20);
        float xTop = surface.xyTextPaint.measureText("0") + Utils.dp2px(getContext(), 10);
        Rect rectF1 = new Rect(startX, surface.YPoint - YLabels.length * surface.YScale, surface
                .XPoint + surface.XLength, surface.YLength);
        for (int i = 0; i < YLabels.length && i * surface.YScale < surface.YLength; i++) {
            canvas.drawLine(startX, surface.YPoint - i * surface.YScale, surface.XPoint + surface
                    .XLength + Utils.dp2px(getContext(), 5), surface.YPoint - i * surface
                    .YScale, surface.gridPaint);
        }
        try {
            if (mCreatBitmap == null) {
                mCreatBitmap = Bitmap.createBitmap(mBitmapBg, startX, surface.YPoint - YLabels
                        .length * surface.YScale, surface.XLength, Utils.dp2px(getContext()
                        , 260));
            }
            BitmapShader bitmapShader = new BitmapShader(mCreatBitmap,
                    Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            for (int i = 0; i < XLabels.length && i * surface.XScale < surface.width; i++) {
                if (i == 0) {
                    canvas.drawLine(startX, surface.YPoint, startX, surface.YPoint - surface
                            .YLength, surface.gridPaint);
                } else {
                    //x刻度线
                    int i1 = (surface.XLength / 7) + Utils.dp2px(getContext(), 11);
                    canvas.drawLine(startX + i * i1, surface.YPoint, startX + i *
                            i1, surface.YPoint - surface.YLength, surface.gridPaint);
                }

                if (i > 0) {
                    //x刻度
                    canvas.drawText(XLabels[i], startX + i * surface.XScale - Utils.dp2px
                            (getContext(), 20), surface.YPoint + xTop, surface.xyTextPaint);
                }
            }

            if (data1 != null && data1.length > 0) {
                //第二条折线
                Path path1 = new Path();
                path1.moveTo(startX, surface.YPoint);
                for (int i = 0; i < XLabels.length && i * surface.XScale < surface.width; i++) {
                    if (i > 0) {
                        canvas.drawLine(startX + (i - 1) * surface.XScale,
                                YCoord(data1[i - 1]), startX + i * surface.XScale, YCoord
                                        (data1[i]), surface.linePaint_);
                    }
                    if (data1[i] != -1) {
                        path1.lineTo(startX + (i) * surface.XScale, YCoord(data1[i]));
                        if (i == XLabels.length - 1) {
                            path1.lineTo(startX + i * surface.XScale, surface.YPoint - 10);
                        }
                    } else {
                        path1.lineTo(startX + (i - 1) * surface.XScale, surface.YPoint - 10);
                        break;
                    }
                }
                path1.close();
                ShapeDrawable shapeDrawable1 = new ShapeDrawable(new PathShape(path1, surface
                        .XLength, Utils.dp2px(getContext(), 260)));
                shapeDrawable1.getPaint().setShader(bitmapShader);
                shapeDrawable1.setBounds(rectF1);
                canvas.drawPath(path1, shapeDrawable1.getPaint());
            }

            //x刻度 0 位置
            canvas.drawText(XLabels[0], startX - Utils.dp2px(getContext(), 4),
                    surface.YPoint + xTop, surface.xyTextPaint);

            //第一条折线
            Path path = new Path();
            path.moveTo(startX, surface.YPoint);
            for (int i = 0; i < XLabels.length && i * surface.XScale < surface.width; i++) {
                if (i > 0) {//0数据连线
                    canvas.drawLine(startX + (i - 1) * surface.XScale,
                            YCoord(data[i - 1]), startX + i * surface.XScale, YCoord(data[i]),
                            surface.linePaint);
                }
                if (data[i] != -1) {
                    path.lineTo(startX + (i) * surface.XScale, YCoord(data[i]));
                    if (i == XLabels.length - 1) {
                        path.lineTo(startX + i * surface.XScale, surface.YPoint - 10);
                    }
                } else {
                    path.lineTo(startX + (i - 1) * surface.XScale, surface.YPoint - 10);
                    break;
                }
            }
            path.close();
            ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(path, surface.XLength,
                    Utils.dp2px(getContext(), 260)));
            shapeDrawable.getPaint().setShader(bitmapShader);
            shapeDrawable.setBounds(rectF1);
            canvas.drawPath(path, shapeDrawable.getPaint());

            for (int i = 0; i < YLabels.length && i * surface.YScale < surface.YLength; i++) {
                String yLabel = YLabels[i];
                if (formatData != null && !"".equals(yLabel))
                    yLabel = formatData.format(yLabel);
                //y刻度值
                canvas.drawText(yLabel + "%", startX + 5,
                        surface.YPoint - i * surface.YScale - 5, surface.xyTextPaint);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            surface.init();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        surface.height = getMeasuredHeight();
        surface.width = getMeasuredWidth();
    }

    public void setData(String[] XLabels, String[] YLabels, float[] data) {
        this.XLabels = XLabels;
        this.YLabels = YLabels;
        this.data = data;
        this.ylabelBig1 = new BigDecimal(this.YLabels[0]);
        invalidate();
    }

    public void setData(String[] XLabels, String[] YLabels, float[] data, float[] data2) {
        this.XLabels = XLabels;
        this.YLabels = YLabels;
        this.data = data;
        this.data1 = data2;
        this.ylabelBig1 = new BigDecimal(this.YLabels[0]);
        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    private class Surface {
        public float density;
        public int width;
        public int height;
        public int XPoint = Utils.dp2px(getContext(), 35);
        public int YPoint;
        public int XScale = Utils.dp2px(getContext(), 27);
        public int YScale = Utils.dp2px(getContext(), 30);
        public int XLength;
        public int YLength;
        public int marginBottom = Utils.dp2px(getContext(), 20);

        public int bgColor = getResources().getColor(R.color.white);
        public int lineColor = getResources().getColor(R.color.common_tab_btn);
        public int lineColor_ = getResources().getColor(R.color.common_char_line);
        public int textColor = Color.WHITE;
        public int axisXColor = getResources().getColor(R.color.common_line_chartview_scal);
        public int gridColor = getResources().getColor(R.color.common_point_line);

        public Paint xyTextPaint;
        public Paint linePaint;
        public Paint linePaint_;
        public Paint pointPaint;
        public Paint gridPaint;

        public void init() {
            YPoint = height - marginBottom;
            XLength = width - XPoint - Utils.dp2px(getContext(), 20);
            YLength = YPoint - Utils.sp2px(getContext(), 1);
            xyTextPaint = new Paint();
            xyTextPaint.setColor(axisXColor);
            xyTextPaint.setAntiAlias(true);
            xyTextPaint.setTextSize(Utils.sp2px(getContext(), 10));
            xyTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

            linePaint = new Paint();
            linePaint.setColor(lineColor);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setAntiAlias(true);
            linePaint.setStrokeWidth(5);

            linePaint_ = new Paint();
            linePaint_.setColor(lineColor_);
            linePaint_.setStyle(Paint.Style.STROKE);
            linePaint_.setAntiAlias(true);
            linePaint_.setStrokeWidth(5);

            pointPaint = new Paint();
            pointPaint.setColor(lineColor);
            pointPaint.setStyle(Paint.Style.STROKE);
            pointPaint.setAntiAlias(true);
            pointPaint.setStrokeWidth(10);

            gridPaint = new Paint();
            gridPaint.setColor(gridColor);
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(2);

        }

    }

    private FormatData formatData;

    public FormatData getFormatData() {
        return formatData;
    }

    public void setFormatData(FormatData formatData) {
        this.formatData = formatData;
    }

    public interface FormatData {
        public String format(String data);

    }

    /**
     * 格式化数据
     */
    public String[] getWeekYdata(String maxValue, String minValue) {
        String[] temp = new String[]{"2", "3", "4", "5", "6"};
        if (maxValue != null && minValue != null) {
            BigDecimal max = new BigDecimal(maxValue);
            BigDecimal min = new BigDecimal(minValue);
            this.perlabelBig1 = max.subtract(min);
            BigDecimal per = max.subtract(min).divide(new BigDecimal(4), 5, BigDecimal.ROUND_HALF_UP);
            for (int i = 0; i < 4; i++) {
                temp[i] = min.add(per.multiply(new BigDecimal(i + "")), new MathContext(5,
                        RoundingMode.HALF_EVEN)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            }
            temp[4] = max.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        return temp;
    }

    /**
     * 回收bitmap
     */
    public void recycleBitmap() {
        if (mBitmapBg != null && !mBitmapBg.isRecycled()) {
            mBitmapBg.recycle();
            mBitmapBg = null;
        }
        if (mCreatBitmap != null && !mCreatBitmap.isRecycled()) {
            mCreatBitmap.recycle();
            mCreatBitmap = null;
        }

    }
}
