package com.view.harriscam;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.main.harriscam.R;
import com.main.harriscam.util.HarrisUtil;

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
    private LeftSlideMenuView leftSlideMenuView;
    private RightSlideMenuView rightSlideMenuView;
    private ImageButton ibShutter;
    // Listner
    private View.OnClickListener listenerModeMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            leftSlideMenuView.setEnableModeMenu( true );
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
        mainContainer = ( FrameLayout ) ( ( ViewGroup ) getChildAt( 0 ) ).getChildAt( 0 );
        leftSlideMenuView = ( LeftSlideMenuView ) mainContainer.findViewById( R.id.leftSlideMenu );
        rightSlideMenuView = ( RightSlideMenuView ) mainContainer.findViewById( R.id.rightSlideMenu );
        ibShutter = ( ImageButton ) ( ( ViewGroup ) getChildAt( 0 ) ).getChildAt( 1 );

        leftSlideMenuView.setOnClickModeMenu( listenerModeMenu );
        rightSlideMenuView.setOnClickOptionMenu( listenerOptionMenu );
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
                leftSlideMenuView.showingModeMenu( x - startTrackPointX, SWIPE_MAX_DISTANCE );
            } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                rightSlideMenuView.hidingOptionMenu( x - startTrackPointX, SWIPE_MAX_DISTANCE );
            }
        } else if ( isMovingRightToLeft( x ) ) {
            if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                rightSlideMenuView.showingOptionMenu( startTrackPointX - x, SWIPE_MAX_DISTANCE );
            } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                leftSlideMenuView.hidingModeMenu( startTrackPointX - x, SWIPE_MAX_DISTANCE );
            }
        }
    }

    private void animatePointUp() {
        if ( isOnFlingLeftToRight() ) {
            if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                leftSlideMenuView.showModeMenu();
                isVisibleModeMenu = true;
            } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                rightSlideMenuView.hideOptionMenu();
                isVisibleOptionsMenu = false;
            }
        } else {
            if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                leftSlideMenuView.hideModeMenu();
                isVisibleModeMenu = false;
            } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                rightSlideMenuView.showOptionMenu();
                isVisibleOptionsMenu = true;
            }
        }
        if ( isOnFlingRightToLeft() ) {
            if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                rightSlideMenuView.showOptionMenu();
                isVisibleOptionsMenu = true;
            } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                leftSlideMenuView.hideModeMenu();
                isVisibleModeMenu = false;
            }
        } else {
            if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                rightSlideMenuView.hideOptionMenu();
                isVisibleOptionsMenu = false;
            } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                leftSlideMenuView.showModeMenu();
                isVisibleModeMenu = true;
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

    public void setOnClickShutter( OnClickListener listener ) {
        ibShutter.setOnClickListener( listener );
    }

    public void setEnableShutter( boolean isEnable ) {
        ibShutter.setEnabled( isEnable );
    }

    public void hideModeMenu() {
        leftSlideMenuView.hideModeMenu();
    }

    public void hideOptionMenu() {
        rightSlideMenuView.hideOptionMenu();
    }

    public boolean isVisibleModeMenu() {
        return isVisibleModeMenu;
    }

    public boolean isVisibleOptionsMenu() {
        return isVisibleOptionsMenu;
    }
}