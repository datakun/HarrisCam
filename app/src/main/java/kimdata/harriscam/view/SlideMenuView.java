package kimdata.harriscam.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.main.harriscam.HarrisUtil;
import com.main.harriscam.R;

public class SlideMenuView extends FrameLayout {
    // Constants
    private static final int MODE_MENU_SIZE = 96;
    private static final int OPTION_MENU_SIZE = 64;
    private static final String TAG = "junu";
    private static int SWIPE_MAX_DISTANCE;
    private static int SWIPE_MIN_DISTANCE;
    private static int SWIPE_POSSIBLE_DISTANCE;

    private int startTrackPointX;
    private int stopTrackPointX;
    private int startTrackPointY;
    private boolean isVisibleModeMenu;
    private boolean isVisibleOptionsMenu;
    private boolean isPossibleTracking;
    private boolean isDisabledTracking;
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
        isPossibleTracking = false;

        LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.layout_slide_menu_view, this, true );

        initializeOfMenu();

        final TypedArray a = getContext().obtainStyledAttributes( attrs, R.styleable.SlideMenuView, defStyle, 0 );

        isVisibleModeMenu = a.getBoolean( R.styleable.SlideMenuView_isVisibleMode, false );
        isVisibleOptionsMenu = a.getBoolean( R.styleable.SlideMenuView_isVisibleOptions, false );

        a.recycle();
    }

    private void initializeOfMenu() {
        mainContainer = ( FrameLayout ) ( ( FrameLayout ) getChildAt( 0 ) ).getChildAt( 0 );
        llMainContainer = ( LinearLayout ) mainContainer.findViewById( R.id.llMainContainer );
        ibCameraMode = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraMode );
        ibGalleryMode = ( ImageButton ) mainContainer.findViewById( R.id.ibGalleryMode );
        ibSettings = ( ImageButton ) mainContainer.findViewById( R.id.ibSettings );
        ibCameraMode.setEnabled( false );
        llMainContainer.setX( -modeMenuSize );

        llSubContainer = ( LinearLayout ) mainContainer.findViewById( R.id.llSubContainer );
        ibFlashlight = ( ImageButton ) mainContainer.findViewById( R.id.ibFlashlight );
        ibGuideline = ( ImageButton ) mainContainer.findViewById( R.id.ibGuideline );
        ibCameraSwitcher = ( ImageButton ) mainContainer.findViewById( R.id.ibCameraSwitcher );
        ibIntervalWatch = ( ImageButton ) mainContainer.findViewById( R.id.ibIntervalWatch );
        llSubContainer.setX( optionMenuSize );

        ibShutter = ( ImageButton ) ( ( FrameLayout ) getChildAt( 0 ) ).getChildAt( 1 );

        setOnClickModeMenu( listenerModeMenu );
        setOnClickOptionMenu( listenerOptionMenu );
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        SWIPE_MAX_DISTANCE = this.getWidth() / 4;
        SWIPE_MIN_DISTANCE = SWIPE_MAX_DISTANCE / 3;
        SWIPE_POSSIBLE_DISTANCE = SWIPE_MAX_DISTANCE / 4;
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        int x = ( int ) event.getX();
        int y = ( int ) event.getY();

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                startTrackPointX = x;
                startTrackPointY = y;

                break;
            case MotionEvent.ACTION_MOVE:
                if ( isDisabledTracking ) {
                    return false;
                }

                if ( isPossibleTracking == false ) {
                    if ( isSwipePossible( event ) == false ) {
                        isDisabledTracking = true;
                        return false;
                    } else {
                        return true;
                    }
                }

                animatePointMove( x );

                break;
            case MotionEvent.ACTION_UP:
                isDisabledTracking = false;
                if ( isPossibleTracking ) {
                    isPossibleTracking = false;
                    stopTrackPointX = x;
                    animatePointUp();
                }

                break;
        }

        return true;
    }

    private void animatePointMove( int x ) {
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
    }

    private void animatePointUp() {
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

    private boolean isSwipePossible( MotionEvent event ) {
        if ( Math.abs( startTrackPointY - event.getY() ) > SWIPE_POSSIBLE_DISTANCE ) {
            isPossibleTracking = false;
            return false;
        }

        if ( Math.abs( startTrackPointX - event.getX() ) > SWIPE_POSSIBLE_DISTANCE ) {
            startTrackPointX = ( int ) event.getX();
            isPossibleTracking = true;
            return true;
        }

        return true;
    }

    private boolean isMovingRightToLeft( int x ) {
        return startTrackPointX > x ? true : false;
    }

    private boolean isMovingLeftToRight( int x ) {
        return startTrackPointX < x ? true : false;
    }

    private boolean isOnFlingRightToLeft() {
        return startTrackPointX - stopTrackPointX >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private boolean isOnFlingLeftToRight() {
        return stopTrackPointX - startTrackPointX >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private void showingModeMenu( int x ) {
        int distance = x - startTrackPointX;
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
        int distance = startTrackPointX - x;
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

        llMainContainer.setVisibility( View.VISIBLE );
        llSubContainer.setVisibility( View.GONE );

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

        llMainContainer.setVisibility( View.VISIBLE );
        llSubContainer.setVisibility( View.GONE );

        ibShutter.setVisibility( View.VISIBLE );
        ibShutter.setAlpha( 1.0f );
        ibShutter.setEnabled( true );

        HarrisUtil.jlog( "Mode Hided" );
    }

    private void showingOptionMenu( int x ) {
        int distance = startTrackPointX - x;
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
        int distance = x - startTrackPointX;
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

        llMainContainer.setVisibility( View.GONE );
        llSubContainer.setVisibility( View.VISIBLE );

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

        llMainContainer.setVisibility( View.GONE );
        llSubContainer.setVisibility( View.VISIBLE );

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