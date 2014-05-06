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

public class LeftSlideMenuView extends FrameLayout {
    // Constants
    private static final int MODE_MENU_SIZE = 96;
    private static final String TAG = "junu";
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llMainContainer;
    private ImageButton ibCameraMode;
    private ImageButton ibGalleryMode;
    private ImageButton ibSettings;
    private boolean isVisibleModeMenu;
    private int modeMenuSize;
    // Listner
    private OnClickListener listenerModeMenu = new OnClickListener() {
        @Override
        public void onClick( View v ) {
            setEnableModeMenu( true );
            v.setEnabled( false );
        }
    };

    public LeftSlideMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public LeftSlideMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public LeftSlideMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        modeMenuSize = HarrisUtil.dp2px( MODE_MENU_SIZE, getResources() );
        isVisibleModeMenu = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_left_menu, this, true );

        initializeOfMenu();
    }

    private void initializeOfMenu() {
        this.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
        this.setAlpha( 0.0f );
        mainContainer = ( ViewGroup ) getChildAt( 0 );
        llMainContainer = ( LinearLayout ) mainContainer.findViewById( R.id.llMainContainer );
        ibCameraMode = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraMode );
        ibGalleryMode = ( ImageButton ) mainContainer.findViewById( R.id.ibGalleryMode );
        ibSettings = ( ImageButton ) mainContainer.findViewById( R.id.ibSettings );
        ibCameraMode.setEnabled( false );
        llMainContainer.setX( -modeMenuSize );

        setOnClickModeMenu( listenerModeMenu );
    }

    public void showingModeMenu( int distance, int swipeMaxDistance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) swipeMaxDistance;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llMainContainer.setX( ratioDistance * modeMenuSize - modeMenuSize );

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Showing" );
    }

    public void hidingModeMenu( int distance, int swipeMaxDistance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) swipeMaxDistance;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llMainContainer.setX( ratioDistance * -modeMenuSize );

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Hiding" );
    }

    private void animateShowModeMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleModeMenu = true;

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Shown" );
    }

    private void animateHideModeMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), -modeMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleModeMenu = false;

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Hided" );
    }

    public void showModeMenu() {
        animateShowModeMenu();
    }

    public void hideModeMenu() {
        animateHideModeMenu();
    }

    public boolean isVisibleModeMenu() {
        return isVisibleModeMenu;
    }

    public void setOnClickModeMenu( OnClickListener listener ) {
        ibCameraMode.setOnClickListener( listener );
        ibGalleryMode.setOnClickListener( listener );
        ibSettings.setOnClickListener( listener );
    }

    public void setEnableModeMenu( boolean isEnable ) {
        ibCameraMode.setEnabled( isEnable );
        ibGalleryMode.setEnabled( isEnable );
        ibSettings.setEnabled( isEnable );
    }
}