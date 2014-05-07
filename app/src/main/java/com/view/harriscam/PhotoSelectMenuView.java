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

public class PhotoSelectMenuView extends FrameLayout {
    // Constants
    private static final int PHOTO_MENU_SIZE = 128;
    private static final String TAG = "junu";
    private static float SWIPE_MAX_DISTANCE;
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llSubContainer;
    private ImageButton ibFirstPhoto;
    private ImageButton ibSecondPhoto;
    private ImageButton ibThirdPhoto;
    private boolean isVisibleMenu;
    private int photoMenuSize;
    // Listner
    private View.OnClickListener listenerClickMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            switch ( v.getId() ) {
                case R.id.ibFirstPhoto:

                    break;
                case R.id.ibSecondPhoto:

                    break;
                case R.id.ibThirdPhoto:

                    break;
            }
        }
    };

    public PhotoSelectMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public PhotoSelectMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public PhotoSelectMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        photoMenuSize = HarrisUtil.dp2px( PHOTO_MENU_SIZE, getResources() );
        isVisibleMenu = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_photo_select_menu_view, this, true );

        initializeOfMenu();
    }

    private void initializeOfMenu() {
        this.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
        this.setAlpha( 0.0f );
        mainContainer = ( ViewGroup ) getChildAt( 0 );
        llSubContainer = ( LinearLayout ) mainContainer.findViewById( R.id.photoMenuContainer );
        ibFirstPhoto = ( ImageButton ) mainContainer.findViewById( R.id.ibFirstPhoto );
        ibSecondPhoto = ( ImageButton ) mainContainer.findViewById( R.id.ibSecondPhoto );
        ibThirdPhoto = ( ImageButton ) mainContainer.findViewById( R.id.ibThirdPhoto );
        llSubContainer.setX( photoMenuSize );

        ibFirstPhoto.setOnClickListener( listenerClickMenu );
        ibSecondPhoto.setOnClickListener( listenerClickMenu );
        ibThirdPhoto.setOnClickListener( listenerClickMenu );
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        SWIPE_MAX_DISTANCE = this.getWidth() / 4;
    }

    public void showingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llSubContainer.setX( photoMenuSize - ( ratioDistance * photoMenuSize ) );

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Photo Showing" );
    }

    public void hidingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llSubContainer.setX( ratioDistance * photoMenuSize );

        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Photo Hiding" );
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

        HarrisUtil.jlog( "Photo Shown" );
    }

    private void animateHideMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(),
                photoMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( this, "alpha", this.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();

        llSubContainer.setVisibility( View.VISIBLE );
        isVisibleMenu = false;

        HarrisUtil.jlog( "Photo Hided" );
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
}