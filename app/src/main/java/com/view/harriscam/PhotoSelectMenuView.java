package com.view.harriscam;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.image.harriscam.HarrisNative;
import com.main.harriscam.R;
import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

import java.io.InputStream;

public class PhotoSelectMenuView extends FrameLayout {
    // Containers & Views
    private Context context;
    private ViewGroup mainContainer;
    private LinearLayout llSubContainer;
    private ImageButton ibFirstPhoto;
    private ImageButton ibSecondPhoto;
    private ImageButton ibThirdPhoto;
    private ImageView ivHarrisResult;
    private boolean isVisibleMenu;
    private int photoMenuSize;
    boolean isFirst, isSecond, isThird;
    private Bitmap bmpSource; // Source bitmap on image sizable function.
    // Listner
    private View.OnClickListener listenerClickMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            Intent intent = new Intent( Intent.ACTION_PICK );
            intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE );
            switch ( v.getId() ) {
                case R.id.ibFirstPhoto:
                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_FIRST );

                    break;
                case R.id.ibSecondPhoto:
                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_SECOND );

                    break;
                case R.id.ibThirdPhoto:
                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_THIRD );

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
        photoMenuSize = HarrisUtil.dp2px( HarrisConfig.PHOTO_MENU_SIZE, getResources() );
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

    public void showingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( ratioDistance );
        llSubContainer.setX( photoMenuSize - ( ratioDistance * photoMenuSize ) );

        llSubContainer.setVisibility( View.VISIBLE );
    }

    public void hidingMenu( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        this.setAlpha( 1.0f - ratioDistance );
        llSubContainer.setX( ratioDistance * photoMenuSize );

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

    public void setResultImageView( ImageView iv ) {
        ivHarrisResult = iv;
    }

    public void clearImageViewDrawable() {
        isFirst = isSecond = isThird = false;

        ibFirstPhoto.setImageResource( R.drawable.ic_album );
        ibSecondPhoto.setImageResource( R.drawable.ic_album );
        ibThirdPhoto.setImageResource( R.drawable.ic_album );
        ivHarrisResult.setImageResource( R.drawable.ic_album );

        bmpSource = null;
        System.gc();
    }

    public void setImageButtonImage( ImageButton ib, Bitmap bmp, InputStream is ) {
        if ( HarrisConfig.IS_SCALE_FILL ) {
            ib.setImageBitmap( HarrisUtil.scaleToFillBitmap( bmp, is ) );
        } else {
            ib.setImageBitmap( HarrisUtil.scaleToStretchBitmap( bmp, is ) );
        }
    }

    public void applyFirstPhoto( InputStream is ) {
        isFirst = true;

        if ( bmpSource == null ) {
            ibFirstPhoto.setImageBitmap( BitmapFactory.decodeStream( is ) );
            bmpSource = ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() ).getBitmap();
        } else {
            setImageButtonImage( ibFirstPhoto, bmpSource, is );
        }
    }

    public void applySecondPhoto( InputStream is ) {
        isSecond = true;

        if ( bmpSource == null ) {
            ibSecondPhoto.setImageBitmap( BitmapFactory.decodeStream( is ) );
            bmpSource = ( ( BitmapDrawable ) ibSecondPhoto.getDrawable() ).getBitmap();
        } else {
            setImageButtonImage( ibSecondPhoto, bmpSource, is );
        }
    }

    public void applyThirdPhoto( InputStream is ) {
        isThird = true;

        if ( bmpSource == null ) {
            ibThirdPhoto.setImageBitmap( BitmapFactory.decodeStream( is ) );
            bmpSource = ( ( BitmapDrawable ) ibThirdPhoto.getDrawable() ).getBitmap();
        } else {
            setImageButtonImage( ibThirdPhoto, bmpSource, is );
        }
    }

    public void checkEnableApplyEffect() {
        if ( isFirst && isSecond && isThird ) {
            HarrisConfig.BMP_HARRIS_RESULT = Bitmap.createBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
                    .getBitmap() );
            HarrisNative.naApplyHarris( HarrisConfig.BMP_HARRIS_RESULT, ( ( BitmapDrawable ) ibSecondPhoto.getDrawable() )
                    .getBitmap(), ( ( BitmapDrawable ) ibThirdPhoto.getDrawable() ).getBitmap() );
            ivHarrisResult.setImageBitmap( HarrisConfig.BMP_HARRIS_RESULT );
        }
    }

    public boolean isSelectedAnyPhoto() {
        return isFirst || isSecond || isThird;
    }

    public boolean isSelectedAllPhoto() {
        return isFirst && isSecond && isThird;
    }
}