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
import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

public class ModeSelectMenuView extends FrameLayout {
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llMainContainer;
    private ImageButton ibCameraMode;
    private ImageButton ibGalleryMode;
    private ImageButton ibSettings;
    private boolean isVisibleMenu;
    private int modeMenuSize;
    // Listner
    private View.OnClickListener listenerClickMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            setEnableMenu( true );
            v.setEnabled( false );

            switch ( v.getId() ) {
                case R.id.ibCameraMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.CAMERA;

                    break;
                case R.id.ibGalleryMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.GALLERY;

                    break;
                case R.id.ibSettings:
                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.SETTINGS;

                    break;
            }
        }
    };

    public ModeSelectMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public ModeSelectMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public ModeSelectMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        modeMenuSize = HarrisUtil.dp2px( HarrisConfig.MODE_MENU_SIZE, getResources() );
        isVisibleMenu = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_mode_select_menu_view, this, true );

        initializeOfMenu();
    }

    private void initializeOfMenu() {
        this.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
        this.setAlpha( 0.0f );
        mainContainer = ( ViewGroup ) getChildAt( 0 );
        llMainContainer = ( LinearLayout ) mainContainer.findViewById( R.id.modeMenuContainer );
        ibCameraMode = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraMode );
        ibGalleryMode = ( ImageButton ) mainContainer.findViewById( R.id.ibGalleryMode );
        ibSettings = ( ImageButton ) mainContainer.findViewById( R.id.ibSettings );
        ibCameraMode.setEnabled( false );
        llMainContainer.setX( -modeMenuSize );

        ibCameraMode.setOnClickListener( listenerClickMenu );
        ibGalleryMode.setOnClickListener( listenerClickMenu );
        ibSettings.setOnClickListener( listenerClickMenu );
    }

    public void showingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llMainContainer.setX( ratioDistance * modeMenuSize - modeMenuSize );

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Showing" );
    }

    public void hidingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llMainContainer.setX( ratioDistance * -modeMenuSize );

        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Hiding" );
    }

    private void animateShowMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();

        llMainContainer.setVisibility( View.VISIBLE );
        isVisibleMenu = true;

        HarrisUtil.jlog( "Mode Shown" );
    }

    private void animateHideMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), -modeMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();

        llMainContainer.setVisibility( View.VISIBLE );
        isVisibleMenu = false;

        HarrisUtil.jlog( "Mode Hided" );
    }

    public void showMenu() {
        animateShowMenu();
    }

    public void hideMenu() {
        animateHideMenu();
    }

    public boolean isVisibleMenu() {
        return isVisibleMenu;
    }

    public void setEnableMenu( boolean isEnable ) {
        ibCameraMode.setEnabled( isEnable );
        ibGalleryMode.setEnabled( isEnable );
        ibSettings.setEnabled( isEnable );
    }
}