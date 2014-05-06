package com.view.harriscam;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.main.harriscam.R;
import com.main.harriscam.util.HarrisUtil;

public class RightSlideMenuView extends FrameLayout {
    // Constants
    private static final int OPTION_MENU_SIZE = 64;
    private static final String TAG = "junu";
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llSubContainer;
    private ImageButton ibFlashlight;
    private ImageButton ibGuideline;
    private ImageButton ibCameraSwitcher;
    private ImageButton ibIntervalWatch;
    private boolean isVisibleOptionsMenu;
    private int optionMenuSize;
    // Listner
    private OnClickListener listenerOptionMenu = new OnClickListener() {
        @Override
        public void onClick( View v ) {
            switch ( v.getId() ) {
                case R.id.ibFlashlight:

                    break;
                case R.id.ibGuideline:

                    break;
                case R.id.ibCameraSwitcher:

                    break;
                case R.id.ibIntervalWatch:

                    break;
            }
        }
    };

    public RightSlideMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public RightSlideMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public RightSlideMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        optionMenuSize = HarrisUtil.dp2px( OPTION_MENU_SIZE, getResources() );
        isVisibleOptionsMenu = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_right_menu, this, true );

        initializeOfMenu();
    }

    private void initializeOfMenu() {
        this.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
        this.setAlpha( 0.0f );
        mainContainer = ( ViewGroup ) getChildAt( 0 );
        llSubContainer = ( LinearLayout ) mainContainer.findViewById( R.id.llSubContainer );
        ibFlashlight = ( ImageButton ) mainContainer.findViewById( R.id.ibFlashlight );
        ibGuideline = ( ImageButton ) mainContainer.findViewById( R.id.ibGuideline );
        ibCameraSwitcher = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraSwitcher );
        ibIntervalWatch = ( ImageButton ) mainContainer.findViewById( R.id.ibIntervalWatch );
        llSubContainer.setX( optionMenuSize );

        setOnClickOptionMenu( listenerOptionMenu );
    }

    public void showingOptionMenu( int distance, int swipeMaxDistance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) swipeMaxDistance;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llSubContainer.setX( optionMenuSize - ( ratioDistance * optionMenuSize ) );

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Showing" );
    }

    public void hidingOptionMenu( int distance, int swipeMaxDistance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) swipeMaxDistance;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llSubContainer.setX( ratioDistance * optionMenuSize );

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Hiding" );
    }

    private void animateShowOptionMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleOptionsMenu = true;

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Shown" );
    }

    private void animateHideOptionMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(),
                optionMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleOptionsMenu = false;

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Hided" );
    }

    public void showOptionMenu() {
        animateShowOptionMenu();
    }

    public void hideOptionMenu() {
        animateHideOptionMenu();
    }

    public boolean isVisibleOptionsMenu() {
        return isVisibleOptionsMenu;
    }

    public void setOnClickOptionMenu( OnClickListener listener ) {
        ibFlashlight.setOnClickListener( listener );
        ibGuideline.setOnClickListener( listener );
        ibCameraSwitcher.setOnClickListener( listener );
        ibIntervalWatch.setOnClickListener( listener );
    }
}