package com.view.harriscam;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    byte[] byImage1, byImage2, byImage3;
    boolean isFirst, isSecond, isThird;
    // Listner
    private View.OnClickListener listenerClickMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            Intent intent = new Intent( Intent.ACTION_PICK );
            intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE );
            switch ( v.getId() ) {
                case R.id.ibFirstPhoto:
//                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_FIRST );

                    break;
                case R.id.ibSecondPhoto:
//                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_SECOND );

                    break;
                case R.id.ibThirdPhoto:
//                    ( ( Activity ) context ).startActivityForResult( intent, HarrisConfig.REQUEST_THIRD );

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

    // TODO : gallery
//    public void clearImageViewDrawable() {
//        HarrisUtil.unbindViewDrawable( ibFirstPhoto );
//        HarrisUtil.unbindViewDrawable( ibSecondPhoto );
//        HarrisUtil.unbindViewDrawable( ibThirdPhoto );
//        HarrisUtil.unbindViewDrawable( ivHarrisResult );
//
//        ibFirstPhoto.setImageResource( R.drawable.ic_album );
//        ibSecondPhoto.setImageResource( R.drawable.ic_album );
//        ibThirdPhoto.setImageResource( R.drawable.ic_album );
//        ivHarrisResult.setImageResource( R.drawable.ic_album );
//    }
//
//    public void applyFirstPhoto( InputStream is ) {
//        isFirst = true;
////        HarrisUtil.unbindViewDrawable( ibFirstPhoto );
//        ibFirstPhoto.setImageBitmap( BitmapFactory.decodeStream( is ) );
//        byImage1 = null;
//        System.gc();
//        byImage1 = HarrisUtil.bitmapToByteArray( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() ).getBitmap() );
//
//        if ( isSecond ) {
//            HarrisUtil.unbindViewDrawable( ibSecondPhoto );
//            if ( HarrisConfig.IS_SCALE_FILL ) {
//                ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToFillBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                        .getBitmap(), byImage2 ) );
//            } else {
//                ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToStretchBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                        .getBitmap(), byImage2 ) );
//            }
//        }
//
//        if ( isThird ) {
//            HarrisUtil.unbindViewDrawable( ibSecondPhoto );
//            if ( HarrisConfig.IS_SCALE_FILL ) {
//                ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToFillBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                        .getBitmap(), byImage3 ) );
//            } else {
//                ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToStretchBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                        .getBitmap(), byImage3 ) );
//            }
//        }
//
//        ibSecondPhoto.setEnabled( true );
//        ibThirdPhoto.setEnabled( true );
//    }
//
//    public void applySecondPhoto( InputStream is ) {
//        isSecond = true;
////        HarrisUtil.unbindViewDrawable( ibSecondPhoto );
//        if ( HarrisConfig.IS_SCALE_FILL ) {
//            ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToFillBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                    .getBitmap(), is ) );
//        } else {
//            ibSecondPhoto.setImageBitmap( HarrisUtil.scaleToStretchBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                    .getBitmap(), is ) );
//        }
//        byImage2 = null;
//        System.gc();
//        byImage2 = HarrisUtil.bitmapToByteArray( ( ( BitmapDrawable ) ibSecondPhoto.getDrawable() ).getBitmap() );
//    }
//
//    public void applyThirdPhoto( InputStream is ) {
//        isThird = true;
////        HarrisUtil.unbindViewDrawable( ibThirdPhoto );
//        if ( HarrisConfig.IS_SCALE_FILL ) {
//            ibThirdPhoto.setImageBitmap( HarrisUtil.scaleToFillBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                    .getBitmap(), is ) );
//        } else {
//            ibThirdPhoto.setImageBitmap( HarrisUtil.scaleToStretchBitmap( ( ( BitmapDrawable ) ibFirstPhoto.getDrawable() )
//                    .getBitmap(), is ) );
//        }
//        byImage3 = null;
//        System.gc();
//        byImage3 = HarrisUtil.bitmapToByteArray( ( ( BitmapDrawable ) ibThirdPhoto.getDrawable() ).getBitmap() );
//    }
//
//    public void checkEnableApplyEffect() {
//        if ( isFirst && isSecond && isThird ) {
//            HarrisConfig.BMP_HARRIS_RESULT = BitmapFactory.decodeByteArray( byImage1, 0, byImage1.length );
//            HarrisNative.naApplyHarris( HarrisConfig.BMP_HARRIS_RESULT, ( ( BitmapDrawable ) ibSecondPhoto.getDrawable() )
//                    .getBitmap(), ( ( BitmapDrawable ) ibThirdPhoto.getDrawable() ).getBitmap() );
//            HarrisUtil.unbindViewDrawable( ivHarrisResult );
//            ivHarrisResult.setImageBitmap( HarrisConfig.BMP_HARRIS_RESULT );
//        }
//    }
//
//    public boolean isSelectedAnyPhoto() {
//        return isFirst || isSecond || isThird;
//    }
//
//    public boolean isSelectedAllPhoto() {
//        return isFirst && isSecond && isThird;
//    }
}