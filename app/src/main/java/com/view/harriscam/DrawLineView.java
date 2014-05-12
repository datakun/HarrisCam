package com.view.harriscam;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.main.harriscam.R;
import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

class Line {
    float startX, startY, stopX, stopY;

    public Line( float startX, float startY, float stopX, float stopY ) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
}

public class DrawLineView extends View {
    Paint paint = new Paint();
    Path pathStroke = new Path();

    public DrawLineView( Context context, AttributeSet attrs ) {
        super( context, attrs );

        paint.setAntiAlias( true );
        paint.setStrokeWidth( HarrisUtil.dp2px( 2, getResources() ) );
        paint.setColor( getResources().getColor( R.color.AlphaGray ) );
        paint.setStyle( Paint.Style.STROKE );
    }

    @Override
    protected void onDraw( Canvas canvas ) {
        pathStroke.reset();

        if ( HarrisConfig.FLAG_GUIDELINE == HarrisConfig.ON ) {
            pathStroke.moveTo( this.getWidth() / 3, 0 );
            pathStroke.lineTo( this.getWidth() / 3, this.getHeight() );
            pathStroke.moveTo( this.getWidth() * 2 / 3, 0 );
            pathStroke.lineTo( this.getWidth() * 2 / 3, this.getHeight() );
            pathStroke.moveTo( 0, this.getHeight() / 3 );
            pathStroke.lineTo( this.getWidth(), this.getHeight() / 3 );
            pathStroke.moveTo( 0, this.getHeight() * 2 / 3 );
            pathStroke.lineTo( this.getWidth(), this.getHeight() * 2 / 3 );
        }

        canvas.drawPath( pathStroke, paint );
    }
}