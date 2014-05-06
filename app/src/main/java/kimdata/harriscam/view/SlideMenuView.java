package kimdata.harriscam.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.main.harriscam.HarrisUtil;
import com.main.harriscam.R;

public class SlideMenuView extends FrameLayout {
    // Constants
    private static final int MODE_MENU_SIZE = 96;
    private static final int OPTION_MENU_SIZE = 64;
    private static final String TAG = "junu";
    private static final int ID_MODE_SETTINGS = 1001;
    private static final int ID_MODE_GALLERY = 1002;
    private static final int ID_MODE_CAMERA = 1003;
    private static final int ID_OPTION_FLASHLIGHT = 2001;
    private static final int ID_OPTION_GUIDELINE = 2002;
    private static final int ID_OPTION_CAMERA_SWITCH = 2003;
    private static final int ID_OPTION_INTERVAL_WATCH = 2004;
    private static int SWIPE_MAX_DISTANCE;
    private static int SWIPE_MIN_DISTANCE;
    // Varables
    private int startTrackPoint;
    private int stopTrackPoint;
    private boolean isVisibleModeMenu;
    private boolean isVisibleOptionsMenu;
    private int modeMenuSize;
    private int optionMenuSize;
    // Containers & Views
    private Context context;
    private FrameLayout mainContainer;
    private LinearLayout llMainContainer;
    private LinearLayout llCameraMode;
    private LinearLayout llGalleryMode;
    private LinearLayout llSettings;
    private ImageButton ibCameraMode;
    private ImageButton ibGalleryMode;
    private ImageButton ibSettings;
    private ImageButton ibShutter;
    private LinearLayout llSubContainer;
    private ImageButton ibFlashlight;
    private ImageButton ibGuideline;
    private ImageButton ibCameraSwitcher;
    private ImageButton ibIntervalWatch;
    // Listner
    private View.OnClickListener listenerModeMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            setEnableModeMenu( true );
            v.setEnabled( false );
        }
    };
    private View.OnClickListener listenerOptionMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            switch ( v.getId() ) {
                case ID_OPTION_FLASHLIGHT:

                    break;
                case ID_OPTION_GUIDELINE:

                    break;
                case ID_OPTION_CAMERA_SWITCH:

                    break;
                case ID_OPTION_INTERVAL_WATCH:

                    break;
            }
        }
    };

    public SlideMenuView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public SlideMenuView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public SlideMenuView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;
        modeMenuSize = HarrisUtil.dp2px( MODE_MENU_SIZE, getResources() );
        optionMenuSize = HarrisUtil.dp2px( OPTION_MENU_SIZE, getResources() );
        isVisibleModeMenu = false;
        isVisibleOptionsMenu = false;

        initModeMenu();
        initOptionsMenu();
        initShutterButton();

        setOnClickModeMenu( listenerModeMenu );
        setOnClickOptionMenu( listenerOptionMenu );

        final TypedArray a = getContext().obtainStyledAttributes( attrs, R.styleable.SlideMenuView, defStyle, 0 );

        isVisibleModeMenu = a.getBoolean( R.styleable.SlideMenuView_isVisibleMode, false );
        isVisibleOptionsMenu = a.getBoolean( R.styleable.SlideMenuView_isVisibleOptions, false );

        a.recycle();
    }

    private void initModeMenu() {
        mainContainer = new FrameLayout( context );
        mainContainer.setBackgroundColor( Color.argb( 200, 0, 0, 0 ) );
        mainContainer.setAlpha( 0.0f );
        this.addView( mainContainer );

        llMainContainer = new LinearLayout( context );

        ibCameraMode = new ImageButton( context );
        ibGalleryMode = new ImageButton( context );
        ibSettings = new ImageButton( context );

        ibCameraMode.setId( ID_MODE_CAMERA );
        ibGalleryMode.setId( ID_MODE_GALLERY );
        ibSettings.setId( ID_MODE_SETTINGS );

        ibCameraMode.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibGalleryMode.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibSettings.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );

        ibCameraMode.setImageDrawable( getResources().getDrawable( R.drawable.xml_btn_camera ) );
        ibGalleryMode.setImageDrawable( getResources().getDrawable( R.drawable.xml_btn_pictures ) );
        ibSettings.setImageDrawable( getResources().getDrawable( R.drawable.xml_btn_gear ) );

        ibCameraMode.setLayoutParams( new LayoutParams( modeMenuSize, modeMenuSize ) );
        ibGalleryMode.setLayoutParams( new LayoutParams( modeMenuSize, modeMenuSize ) );
        ibSettings.setLayoutParams( new LayoutParams( modeMenuSize, modeMenuSize ) );

        ibCameraMode.setScaleType( ImageView.ScaleType.FIT_CENTER );
        ibGalleryMode.setScaleType( ImageView.ScaleType.FIT_CENTER );
        ibSettings.setScaleType( ImageView.ScaleType.FIT_CENTER );

        TextView tvCameraMode = new TextView( context );
        TextView tvGalleryMode = new TextView( context );
        TextView tvSettings = new TextView( context );

        tvCameraMode.setText( getResources().getString( R.string.mode_camera ) );
        tvGalleryMode.setText( getResources().getString( R.string.mode_gallery ) );
        tvSettings.setText( getResources().getString( R.string.mode_settings ) );

        tvCameraMode.setTextSize( MODE_MENU_SIZE / 4 );
        tvGalleryMode.setTextSize( MODE_MENU_SIZE / 4 );
        tvSettings.setTextSize( MODE_MENU_SIZE / 4 );

        tvCameraMode.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT ) );
        tvGalleryMode.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT ) );
        tvSettings.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT ) );

        tvCameraMode.setTextColor( getResources().getColor( R.color.LightGray ) );
        tvGalleryMode.setTextColor( getResources().getColor( R.color.LightGray ) );
        tvSettings.setTextColor( getResources().getColor( R.color.LightGray ) );

        tvCameraMode.setGravity( Gravity.CENTER_VERTICAL );
        tvGalleryMode.setGravity( Gravity.CENTER_VERTICAL );
        tvSettings.setGravity( Gravity.CENTER_VERTICAL );

        llCameraMode = new LinearLayout( context );
        llGalleryMode = new LinearLayout( context );
        llSettings = new LinearLayout( context );

        llCameraMode.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
        llGalleryMode.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
        llSettings.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );

        llCameraMode.setOrientation( LinearLayout.HORIZONTAL );
        llGalleryMode.setOrientation( LinearLayout.HORIZONTAL );
        llSettings.setOrientation( LinearLayout.HORIZONTAL );

        llCameraMode.addView( ibCameraMode );
        llCameraMode.addView( tvCameraMode );
        llGalleryMode.addView( ibGalleryMode );
        llGalleryMode.addView( tvGalleryMode );
        llSettings.addView( ibSettings );
        llSettings.addView( tvSettings );

        llMainContainer.addView( llSettings );
        llMainContainer.addView( llGalleryMode );
        llMainContainer.addView( llCameraMode );

        mainContainer.addView( llMainContainer );

        llMainContainer.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
        llMainContainer.setOrientation( LinearLayout.VERTICAL );
        llMainContainer.setGravity( Gravity.BOTTOM | Gravity.LEFT );
        llMainContainer.setX( -modeMenuSize );
        llMainContainer.setPadding( 0, 0, 0, modeMenuSize );
    }

    private void initOptionsMenu() {
        llSubContainer = new LinearLayout( context );

        ibFlashlight = new ImageButton( context );
        ibGuideline = new ImageButton( context );
        ibCameraSwitcher = new ImageButton( context );
        ibIntervalWatch = new ImageButton( context );

        ibFlashlight.setId( ID_OPTION_FLASHLIGHT );
        ibGuideline.setId( ID_OPTION_GUIDELINE );
        ibCameraSwitcher.setId( ID_OPTION_CAMERA_SWITCH );
        ibIntervalWatch.setId( ID_OPTION_INTERVAL_WATCH );

        ibFlashlight.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibGuideline.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibCameraSwitcher.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibIntervalWatch.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );

        ibFlashlight.setImageResource( R.drawable.ic_flashlight_off );
        ibGuideline.setImageResource( R.drawable.ic_guideline_off );
        ibCameraSwitcher.setImageResource( R.drawable.ic_switching );
        ibIntervalWatch.setImageResource( R.drawable.ic_watch_05 );

        ibFlashlight.setLayoutParams( new LayoutParams( optionMenuSize, optionMenuSize ) );
        ibGuideline.setLayoutParams( new LayoutParams( optionMenuSize, optionMenuSize ) );
        ibCameraSwitcher.setLayoutParams( new LayoutParams( optionMenuSize, optionMenuSize ) );
        ibIntervalWatch.setLayoutParams( new LayoutParams( optionMenuSize, optionMenuSize ) );

        ibFlashlight.setScaleType( ImageView.ScaleType.FIT_CENTER );
        ibGuideline.setScaleType( ImageView.ScaleType.FIT_CENTER );
        ibCameraSwitcher.setScaleType( ImageView.ScaleType.FIT_CENTER );
        ibIntervalWatch.setScaleType( ImageView.ScaleType.FIT_CENTER );

        llSubContainer.addView( ibFlashlight );
        llSubContainer.addView( ibGuideline );
        llSubContainer.addView( ibCameraSwitcher );
        llSubContainer.addView( ibIntervalWatch );

        mainContainer.addView( llSubContainer );

        llSubContainer.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
        llSubContainer.setOrientation( LinearLayout.VERTICAL );
        llSubContainer.setGravity( Gravity.BOTTOM | Gravity.RIGHT );
        llSubContainer.setX( optionMenuSize );
        llSubContainer.setPadding( 0, 0, 0, modeMenuSize );
    }

    private void initShutterButton() {
        ibShutter = new ImageButton( context );
        ibShutter.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );
        ibShutter.setImageDrawable( getResources().getDrawable( R.drawable.xml_btn_shutter ) );
        ibShutter.setLayoutParams( new LayoutParams( modeMenuSize, modeMenuSize ) );
        ibShutter.setScaleType( ImageView.ScaleType.FIT_CENTER );

        this.addView( ibShutter );
        LayoutParams lp = ( LayoutParams ) ibShutter.getLayoutParams();
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        ibShutter.setLayoutParams( lp );
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        SWIPE_MAX_DISTANCE = this.getWidth() / 4;
        SWIPE_MIN_DISTANCE = SWIPE_MAX_DISTANCE / 3;
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        int x = ( int ) event.getX();

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                startTrackPoint = x;
            case MotionEvent.ACTION_MOVE:
                if ( isMovingLeftToRight( x ) ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        showingModeMenu( x );
                    } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                        hidingOptionMenu( x );
                    }
                } else if ( isMovingRightToLeft( x ) ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        showingOptionMenu( x );
                    } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                        hidingModeMenu( x );
                    }
                }

                return true;
            case MotionEvent.ACTION_UP:
                stopTrackPoint = ( int ) event.getX();
                if ( isOnFlingLeftToRight() ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        animateShowModeMenu();
                    } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                        animateHideOptionMenu();
                    }
                } else {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        animateHideModeMenu();
                    } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                        animateShowOptionMenu();
                    }
                }
                if ( isOnFlingRightToLeft() ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        animateShowOptionMenu();
                    } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                        animateHideModeMenu();
                    }
                } else {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        animateHideOptionMenu();
                    } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                        animateShowModeMenu();
                    }
                }
        }

        return true;
    }

    private boolean isMovingRightToLeft( int x ) {
        return startTrackPoint > x ? true : false;
    }

    private boolean isMovingLeftToRight( int x ) {
        return startTrackPoint < x ? true : false;
    }

    private boolean isOnFlingRightToLeft() {
        return startTrackPoint - stopTrackPoint >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private boolean isOnFlingLeftToRight() {
        return stopTrackPoint - startTrackPoint >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private void showingModeMenu( int x ) {
        int distance = x - startTrackPoint;
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        mainContainer.setAlpha( ratioDistance );
        llMainContainer.setX( ratioDistance * modeMenuSize - modeMenuSize );

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( 1.0f - ratioDistance );

        llSubContainer.setVisibility( View.GONE );
        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Showing" );
    }

    private void hidingModeMenu( int x ) {
        int distance = startTrackPoint - x;
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        mainContainer.setAlpha( 1.0f - ratioDistance );
        llMainContainer.setX( ratioDistance * -modeMenuSize );

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( ratioDistance );

        llSubContainer.setVisibility( View.GONE );
        llMainContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Mode Hiding" );
    }

    private void animateShowModeMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( mainContainer, "alpha", mainContainer.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleModeMenu = true;

        ibShutter.setVisibility( View.GONE );
        ibShutter.setAlpha( 0.0f );
        ibShutter.setEnabled( false );

        HarrisUtil.jlog( "Mode Shown" );
    }

    private void animateHideModeMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llMainContainer, "translationX", llMainContainer.getX(), -modeMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( mainContainer, "alpha", mainContainer.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleModeMenu = false;

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( 1.0f );
        ibShutter.setEnabled( true );

        HarrisUtil.jlog( "Mode Hided" );
    }

    private void showingOptionMenu( int x ) {
        int distance = startTrackPoint - x;
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        mainContainer.setAlpha( ratioDistance );
        llSubContainer.setX( optionMenuSize - ( ratioDistance * optionMenuSize ) );

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( 1.0f - ratioDistance );

        llMainContainer.setVisibility( View.GONE );
        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Showing" );
    }

    private void hidingOptionMenu( int x ) {
        int distance = x - startTrackPoint;
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = ( float ) distance / ( float ) SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        mainContainer.setAlpha( 1.0f - ratioDistance );
        llSubContainer.setX( ratioDistance * optionMenuSize );

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( ratioDistance );

        llMainContainer.setVisibility( View.GONE );
        llSubContainer.setVisibility( View.VISIBLE );

        HarrisUtil.jlog( "Option Hiding" );
    }

    private void animateShowOptionMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(), 0.0f );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( mainContainer, "alpha", mainContainer.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleOptionsMenu = true;

        ibShutter.setVisibility( View.GONE );
        ibShutter.setAlpha( 0.0f );
        ibShutter.setEnabled( false );

        HarrisUtil.jlog( "Option Shown" );
    }

    private void animateHideOptionMenu() {
        ObjectAnimator aniTranslate = ObjectAnimator.ofFloat( llSubContainer, "translationX", llSubContainer.getX(),
                optionMenuSize );
        aniTranslate.setDuration( 300 );
        aniTranslate.setInterpolator( new AccelerateDecelerateInterpolator() );
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( mainContainer, "alpha", mainContainer.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniTranslate.start();
        aniAlpha.start();
        isVisibleOptionsMenu = false;

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( 1.0f );
        ibShutter.setEnabled( true );

        HarrisUtil.jlog( "Option Hided" );
    }

    public void showModeMenu() {
        animateShowModeMenu();
    }

    public void hideModeMenu() {
        animateHideModeMenu();
    }

    public void showOptionMenu() {
        animateShowOptionMenu();
    }

    public void hideOptionMenu() {
        animateHideOptionMenu();
    }

    public boolean isVisibleModeMenu() {
        return isVisibleModeMenu;
    }

    public boolean isVisibleOptionsMenu() {
        return isVisibleOptionsMenu;
    }

    public void setOnClickModeMenu( OnClickListener listener ) {
        ibCameraMode.setOnClickListener( listener );
        ibGalleryMode.setOnClickListener( listener );
        ibSettings.setOnClickListener( listener );
    }

    public void setOnClickOptionMenu( OnClickListener listener ) {
        ibFlashlight.setOnClickListener( listener );
        ibGuideline.setOnClickListener( listener );
        ibCameraSwitcher.setOnClickListener( listener );
        ibIntervalWatch.setOnClickListener( listener );
    }

    public void setOnClickShutter( OnClickListener listener ) {
        ibShutter.setOnClickListener( listener );
    }

    public void setEnableModeMenu( boolean isEnable ) {
        ibCameraMode.setEnabled( isEnable );
        ibGalleryMode.setEnabled( isEnable );
        ibSettings.setEnabled( isEnable );
    }

    public void setEnableShutter( boolean isEnable ) {
        ibShutter.setEnabled( isEnable );
    }
}
