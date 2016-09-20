package com.lipy.linechart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 折线图
 * Created by Lipy on 16/4/7.
 */
public class LineChartView extends View implements View.OnTouchListener {

    private Surface surface;
    private String[] XLabels;
    private String[] YLabels;
    private BigDecimal perlabelBig;
    private BigDecimal ylabelBig1;
    private float[] data;
    private String title;
    private String content;
    private static Bitmap mBitmapBg;
    private Bitmap mCreatBitmap;
    private Drawable mDrawableTip;
    private Drawable mDrawableDot;
    private Boolean isHasDto = true;


    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surface = new Surface();
        mDrawableDot = getResources().getDrawable(R.drawable.circle);
        mDrawableTip = getResources().getDrawable(R.drawable.bubble_details);
        if (mBitmapBg == null) {
            mBitmapBg = BitmapFactory.decodeResource(getResources(), R.drawable.lined_diagram);
        }
        surface.density = getResources().getDisplayMetrics().density;
        setBackgroundColor(surface.bgColor);
        setOnTouchListener(this);

    }

    public LineChartView(Context context, AttributeSet attrs, int defStyle) {
        this(context, null);
    }

    private int YCoord(float y0) {
        if (y0 < 0)
            return -1;
        float y;
        y = y0;
        try {
            if (perlabelBig.floatValue() == 0 || y == 0) {
                return surface.YPoint;//底边
            }
            if (y < ylabelBig1.floatValue()) {//小于最小值的情况
                return surface.YPoint - new BigDecimal(y).divide(ylabelBig1, new MathContext(4, RoundingMode.HALF_EVEN)).multiply(yScaleBig).intValue();
            }

            return surface.YPoint - new BigDecimal(y).subtract(ylabelBig1).
                    divide(perlabelBig, new MathContext(4, RoundingMode.HALF_EVEN)).
                    multiply(new BigDecimal(surface.YLength - surface.YScale)).intValue();
        } catch (Exception ignored) {

        }
        return (int) y;
    }

    private BigDecimal yScaleBig;

    @Override
    protected void onDraw(Canvas canvas) {
        if (XLabels == null || YLabels == null) {
            if (!Utils.isEmpty(title) && !Utils.isEmpty(content)) {
                canvas.drawText(title, surface.XPoint - Utils.dp2px(getContext(), 20), surface.marginTop + Utils.dp2px(getContext(), 6), surface.titlePaint);
                canvas.drawText(content, surface.XPoint - Utils.dp2px(getContext(), 18), surface.marginTop + Utils.dp2px(getContext(), 22), surface.titlePaint);
                super.onDraw(canvas);
            }
            return;
        }
        surface.XScale = (surface.XLength / (XLabels.length)) + Utils.dp2px(getContext(), 10);
        surface.YScale = surface.YLength / (YLabels.length);
        yScaleBig = new BigDecimal(surface.YScale);
        int startX = surface.XPoint - Utils.dp2px(getContext(), 20);
        float xTop = surface.xyTextPaint.measureText("0") + Utils.dp2px(getContext(), 10);
        Rect rectF1 = new Rect(startX, surface.YPoint - YLabels.length * surface.YScale, surface.XPoint + surface.XLength, surface.YLength);
        for (int i = 0; i < YLabels.length && i * surface.YScale < surface.YLength; i++) {
            canvas.drawLine(startX, surface.YPoint - i * surface.YScale, surface.XPoint + surface.XLength + Utils.dp2px(getContext(), 5), surface.YPoint - i * surface.YScale, surface.gridPaint);
        }

        for (int i = 0; i < XLabels.length && i * surface.XScale < surface.width; i++) {
            if (i == 0) {
                canvas.drawLine(startX, surface.YPoint, startX, surface.YPoint - surface.YLength, surface.gridPaint);

            } else {
                //x刻度线
                canvas.drawLine(startX + i * surface.XScale, surface.YPoint, startX + i * surface.XScale, surface.YPoint - surface.YLength, surface.gridPaint);
            }
            try {
                //x刻度
                canvas.drawText(XLabels[i], startX + i * surface.XScale - Utils.dp2px(getContext(), 8), surface.YPoint + xTop, surface.xyTextPaint);
                int yCoord = YCoord(data[i]);
                if (i > 0 && YCoord(data[i - 1]) != -1 && yCoord != -1) {//0数据连线
                    canvas.drawLine(startX + (i - 1) * surface.XScale,
                            YCoord(data[i - 1]), startX + i * surface.XScale, yCoord, surface.linePaint);

                }

                if (data[i] != -1) {
                    isHasDto = true;
                    Path path = new Path();
                    path.moveTo(startX + (i - 1) * surface.XScale, surface.YPoint);
                    path.lineTo(startX + (i) * surface.XScale, surface.YPoint - 10);
                    path.lineTo(startX + (i) * surface.XScale, YCoord(data[i]));
                    path.lineTo(startX + (i - 1) * surface.XScale, YCoord(data[i - 1]));
                    path.close();
                    ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(path, surface.XLength, Utils.dp2px(getContext(), 260)));
                    if (mCreatBitmap == null) {
                        mCreatBitmap = Bitmap.createBitmap(mBitmapBg, startX, surface.YPoint - YLabels.length * surface.YScale, surface.XLength, Utils.dp2px(getContext(), 260));
                    }
                    BitmapShader bitmapShader = new BitmapShader(mCreatBitmap,
                            Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                    shapeDrawable.getPaint().setShader(bitmapShader);
                    shapeDrawable.setBounds(rectF1);
                    canvas.drawPath(path, shapeDrawable.getPaint());
                    canvas.drawCircle(startX + i * surface.XScale, yCoord, 0, surface.pointPaint);
                    if (i == (XLabels.length - 1)) {
                        String popText_ = data[i] + "";
                        float perCharX = surface.textPaint.measureText(popText_.charAt(0) + "");
                        float textX = perCharX * (popText_).length() / 2.0f;
                        canvas.drawBitmap(drawableToBitamp(mDrawableTip, Utils.dp2px(getContext(), 35), Utils.dp2px(getContext(), 24)), startX + i * surface.XScale - textX - Utils.dp2px(getContext(), 15), yCoord - perCharX - 2 * surface.marginPopContent - Utils.dp2px(getContext(), 15), surface.pointPaint);
                        if (formatData != null) {
                            popText_ = formatData.format(popText_);
                        }
                        //值显示
                        canvas.drawText(popText_, startX + i * surface.XScale - textX - Utils.dp2px(getContext(), 10), yCoord - Utils.dp2px(getContext(), 5) - (data[i] == 0 ? (surface.marginPopContent + 4) : surface.marginPopContent) - Utils.dp2px(getContext(), 7), surface.textPaint);
                        canvas.drawBitmap(drawableToBitamp(mDrawableDot, Utils.dp2px(getContext(), 15), Utils.dp2px(getContext(), 15)), startX + i * surface.XScale - Utils.dp2px(getContext(), 8), yCoord - Utils.dp2px(getContext(), 8), surface.pointPaint);
                    }

                } else {
                    if (i == (XLabels.length - (XLabels.length - i))) {
                        if (isHasDto) {
                            int yCoord_ = YCoord(data[i - 1]);
                            String popText_ = data[i - 1] + "";
                            float perCharX = surface.textPaint.measureText(popText_.charAt(0) + "");
                            float textX = perCharX * (popText_).length() / 2.0f;
                            canvas.drawBitmap(drawableToBitamp(mDrawableTip, Utils.dp2px(getContext(), 35), Utils.dp2px(getContext(), 24)),
                                    startX + (i - 1) * surface.XScale - textX - Utils.dp2px(getContext(), 17), yCoord_ - perCharX - 2 * surface.marginPopContent - Utils.dp2px(getContext(), 15), surface.pointPaint);
                            if (formatData != null) {
                                popText_ = formatData.format(popText_);
                            }
                            canvas.drawText(popText_, startX + (i - 1) * surface.XScale - textX - Utils.dp2px(getContext(), 12), yCoord_ - Utils.dp2px(getContext(), 5) - (data[i] == 0 ? (surface.marginPopContent + 4) : surface.marginPopContent) - Utils.dp2px(getContext(), 8), surface.textPaint);
                            canvas.drawBitmap(drawableToBitamp(mDrawableDot, Utils.dp2px(getContext(), 15), Utils.dp2px(getContext(), 15)), startX + (i - 1) * surface.XScale - Utils.dp2px(getContext(), 10), yCoord_ - Utils.dp2px(getContext(), 8), surface.pointPaint);
                            isHasDto = false;
                        }
                    }
                }


            } catch (Exception e) {
            }
        }

        for (int i = 0; i < YLabels.length && i * surface.YScale < surface.YLength; i++) {
            try {
                //值格式化
                String yLabel = YLabels[i];
                if (formatData != null && !"".equals(yLabel))
                    yLabel = formatData.format(yLabel);
                //y刻度值
                canvas.drawText(yLabel, startX + 5,
                        surface.YPoint - i * surface.YScale - 5, surface.xyTextPaint);
            } catch (Exception e) {
            }
        }

        if (!Utils.isEmpty(title)) {
            canvas.drawText(title, surface.XPoint - Utils.dp2px(getContext(), 20), surface.marginTop, surface.titlePaint);
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

    public void setData(String[] XLabels, String[] YLabels, float[] data, String title) {
        this.XLabels = XLabels;
        this.YLabels = YLabels;
        this.data = data;
        this.title = title;
        this.ylabelBig1 = new BigDecimal(this.YLabels[0]);
        invalidate();
    }

    /**
     * 设置数据
     *
     * @param XLabels X轴数据
     * @param YLabels Y轴数据
     * @param data    折线点的数据
     */
    public void setData(String[] XLabels, String[] YLabels, float[] data) {
        this.XLabels = XLabels;
        this.YLabels = YLabels;
        this.data = data;
        this.ylabelBig1 = new BigDecimal(this.YLabels[1]);
        invalidate();
    }

    public void setData(String title, String content) {
        this.title = title;
        this.content = content;
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
        public int marginBottom = Utils.dp2px(getContext(), 25);
        public int marginTop = Utils.dp2px(getContext(), 15);
        public int marginPopContent = Utils.dp2px(getContext(), 5);


        public int bgColor = getResources().getColor(R.color.white);
        public int titleColor = getResources().getColor(R.color.common_text_gray);
        public int lineColor = getResources().getColor(R.color.common_top_bg);
        public int textColor = Color.WHITE;
        public int pointColor = lineColor;
        public int axisXColor = getResources().getColor(R.color.common_line_chartview_scal);
        public int gridColor = getResources().getColor(R.color.common_point_line);

        public Paint titlePaint;
        public Paint textPaint;
        public Paint xyTextPaint;
        public Paint linePaint;
        public Paint pointPaint;
        public Paint axisXPaint;
        public Paint gridPaint;

        public void init() {
            YPoint = height - marginBottom;
            XLength = width - XPoint - Utils.dp2px(getContext(), 20);

            titlePaint = new Paint();
            titlePaint.setColor(titleColor);
            titlePaint.setAntiAlias(true); //去掉锯齿
            titlePaint.setTextSize(Utils.sp2px(getContext(), 15));
            YLength = YPoint - (int) titlePaint.getTextSize() - marginTop;

            textPaint = new Paint();
            textPaint.setColor(textColor);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(Utils.sp2px(getContext(), 13));

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

            pointPaint = new Paint();
            pointPaint.setColor(pointColor);
            pointPaint.setStyle(Paint.Style.STROKE);
            pointPaint.setAntiAlias(true);
            pointPaint.setStrokeWidth(10);

            axisXPaint = new Paint();
            axisXPaint.setColor(axisXColor);
            axisXPaint.setStyle(Paint.Style.STROKE);
            axisXPaint.setAntiAlias(true);
            axisXPaint.setStrokeWidth(4);

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
        String format(String data);
    }

    private Bitmap drawableToBitamp(Drawable drawable, int w, int h) {
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public String[] getFundWeekYdata(String maxValue, String minValue) {
        String[] temp = new String[]{"0.00", "2", "3", "4", "5", "6"};
        if (maxValue != null && minValue != null) {
            BigDecimal max = new BigDecimal(maxValue);
            BigDecimal min = new BigDecimal(0.00);
            perlabelBig = max.subtract(min);
            BigDecimal per = max.subtract(min).divide(new BigDecimal(5), 5, BigDecimal.ROUND_HALF_UP);
            for (int i = 0; i < 5; i++) {
                temp[i] = min.add(per.multiply(new BigDecimal(i + "")), new MathContext(5, RoundingMode.HALF_EVEN)).
                        setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            }
            temp[5] = max.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
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
