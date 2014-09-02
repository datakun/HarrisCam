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

public class OptionSelectMenuView extends FrameLayout {
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llSubContainer;
    private ImageButton ibFlashlight;
    private ImageButton ibGuideline;
    private ImageButton ibCameraSwitcher;
    private ImageButton ibIntervalWatch;
    private boolean isVisibleMenu;
    private int optionMenuSize;

    public OptionSelectMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public OptionSelectMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public OptionSelectMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        optionMenuSize = HarrisUtil.dp2px( HarrisConfig.OPTION_MENU_SIZE, getResources() );
        isVisibleMenu = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_option_select_menu_view, this, true );

        initializeOfMenu();
    }

    private void initializeOfMenu() {
//        this.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
//        this.setAlpha( 0.0f );
        mainContainer = ( ViewGroup ) getChildAt( 0 );
        llSubContainer = ( LinearLayout ) mainContainer.findViewById( R.id.optionMenuContainer );
        ibFlashlight = ( ImageButton ) mainContainer.findViewById( R.id.ibFlashlight );
        ibGuideline = ( ImageButton ) mainContainer.findViewById( R.id.ibGuideline );
        ibCameraSwitcher = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraSwitcher );
        ibIntervalWatch = ( ImageButton ) mainContainer.findViewById( R.id.ibIntervalWatch );
//        llSubContainer.setX( optionMenuSize );
    }

    public void showingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llSubContainer.setX( optionMenuSize - ( ratioDistance * optionMenuSize ) );

        llSubContainer.setVisibility( View.VISIBLE );
    }

    public void hidingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llSubContainer.setX( ratioDistance * optionMenuSize );

        llSubContainer.setVisibility( View.VISIBLE );
    }

    private void animateShowMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();

        llSubContainer.setVisibility( View.VISIBLE );
        isVisibleMenu = true;
    }

    private void animateHideMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(),
                optionMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();

        llSubContainer.setVisibility( View.VISIBLE );
        isVisibleMenu = false;
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

    public void setOnMenuClickListener( OnClickListener listener ) {
        ibFlashlight.setOnClickListener( listener );
        ibGuideline.setOnClickListener( listener );
        ibCameraSwitcher.setOnClickListener( listener );
        ibIntervalWatch.setOnClickListener( listener );
    }

    public void setOnMenuTouchListener( OnTouchListener listener ) {
        ibFlashlight.setOnTouchListener( listener );
        ibGuideline.setOnTouchListener( listener );
        ibCameraSwitcher.setOnTouchListener( listener );
        ibIntervalWatch.setOnTouchListener( listener );
    }

    public void setFlashlightEnable( boolean isEnable ) {
        ibFlashlight.setEnabled( isEnable );
        if ( isEnable )
            ibFlashlight.setVisibility( View.VISIBLE );
        else
            ibFlashlight.setVisibility( View.GONE );

    }

    public void setCameraSwitcherEnable( boolean isEnable ) {
        ibCameraSwitcher.setEnabled( isEnable );
        if ( isEnable )
            ibCameraSwitcher.setVisibility( View.VISIBLE );
        else
            ibCameraSwitcher.setVisibility( View.GONE );
    }

    public void updateView() {
        ibFlashlight.setImageResource( HarrisConfig.RESOURCE_FLASHLIGHT[ HarrisConfig.FLAG_FLASHLIGHT ] );
        ibGuideline.setImageResource( HarrisConfig.RESOURCE_GUIDELINE[ HarrisConfig.FLAG_GUIDELINE ] );
        ibIntervalWatch.setImageResource( HarrisConfig.RESOURCE_WATCH[ HarrisConfig.CAPTURE_INTERVAL / 500 ] );
    }
}