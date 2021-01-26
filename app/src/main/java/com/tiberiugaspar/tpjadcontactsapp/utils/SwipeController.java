package com.tiberiugaspar.tpjadcontactsapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import androidx.recyclerview.widget.RecyclerView;

import com.tiberiugaspar.tpjadcontactsapp.R;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

/**
 * Simple enum holding the 3 possible states of the buttons:
 * GONE - the buttons are not displayed;
 * LEFT_VISIBLE - the calling button is visible
 * RIGHT_VISIBLE - the SMS button is visible
 */
enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

/**
 * Extends the base {@link Callback} class
 *
 * <p>This class is used to control the swipe gestures over the items of a RecyclerView</p>
 * <p>***Inspired by FanFataL's approach
 * available @ https://github.com/FanFataL/swipe-controller-demo</p>
 */
public class SwipeController extends Callback {

    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private static final float buttonWidth = 160;

    private RectF buttonInstance;

    private final SwipeControllerActions buttonsActions;

    private RecyclerView.ViewHolder currentItemViewHolder = null;

    private final Context context;

    /**
     * @param context the context in which the SwipeControlled has to be applied
     * @param buttonsActions the actions to be executed on pressing the buttons
     */
    public SwipeController(Context context, SwipeControllerActions buttonsActions) {
        this.context = context;
        this.buttonsActions = buttonsActions;
    }

    /**
     * @param recyclerView the recyclerView container which holds the items
     * @param viewHolder the viewHolder that holds all the views of each item
     * @return the movementFlags that we need in order to intercept the swipe gestures:
     *              -ItemTouchHelper.START (equivalent of ItemTouchHelper.LEFT)
     *              -ItemTouchHelper.END (equivalent of ItemTouchHelper.RIGHT) - with respect to
     *                  RightToLeft orientation
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    /**
     * Required override, no-op;
     *
     * @param recyclerView -
     * @param viewHolder -
     * @param target -
     * @return - no-op - return false.
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    /**
     * Required override - no-op
     *
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    /**
     * @param flags movement flags
     * @param layoutDirection the direction of swipe (to Left or to Right)
     * @return - the absolute direction of the swipe gesture
     */
    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        //if the swipe is a swipe back action (to return to default state, we return code 0
        // to remove the item decorations
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        //else we return the super of the method to handle the swipe accordingly
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    /**
     * @param c the canvas used to draw the buttons
     * @param recyclerView recyclerView
     * @param viewHolder viewHolder
     * @param dX dX coordinate
     * @param dY dY coordinate
     * @param actionState - flag for SWIPE gesture
     * @param isCurrentlyActive - boolean to check whether the view is active or not
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //if the actionState flag is a SWIPE flag we go further
        if (actionState == ACTION_STATE_SWIPE) {
            //if the buttons are not gone -> are displayed
            if (buttonShowedState != ButtonsState.GONE) {
                //set the dX coordinate accordingly for the left or right button
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE)
                    dX = Math.max(dX, buttonWidth);
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE)
                    dX = Math.min(dX, -buttonWidth);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            } else {
                //if the buttons are gone, we set the touchListeners
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        //we draw the buttons
        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        //and we set the currentItemViewHolder to be the current viewHolder for this given position
        currentItemViewHolder = viewHolder;
    }

    /**
     * Setting the touchListeners to view items
     *
     * @param c                 - canvas
     * @param recyclerView      -
     * @param viewHolder        -
     * @param dX                - dX coordinate
     * @param dY                - dY coordinate
     * @param actionState       - actionFlags
     * @param isCurrentlyActive - boolean isCurrentlyActive
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(Canvas c,
                                  RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  float dX, float dY,
                                  int actionState, boolean isCurrentlyActive) {

        //set a touchListener for the entire recyclerView to handle external events
        // and to reset the button states
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP;

            //if an outside movement detected, hide the buttons
            if (swipeBack) {
                if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE;
                else if (dX > buttonWidth) buttonShowedState = ButtonsState.LEFT_VISIBLE;

                if (buttonShowedState != ButtonsState.GONE) {
                    setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    setItemsClickable(recyclerView, false);
                }
            }
            return false;
        });
    }

    /**
     * Implemented just as FanFataL's implementation
     *
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     * @see SwipeController javadoc
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchDownListener(final Canvas c,
                                      final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder,
                                      final float dX, final float dY,
                                      final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    /**
     * Implemented just as FanFataL's implementation
     *
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     * @see SwipeController javadoc
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchUpListener(final Canvas c,
                                    final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder,
                                    final float dX, final float dY,
                                    final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    setItemsClickable(recyclerView, true);
                    swipeBack = false;
                    if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {
                        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                            buttonsActions.onLeftClicked(viewHolder.getAdapterPosition());
                        } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                            buttonsActions.onRightClicked(viewHolder.getAdapterPosition());
                        }
                    }
                    buttonShowedState = ButtonsState.GONE;
                    currentItemViewHolder = null;
                }
                return false;
            }
        });
    }

    /**
     * @param recyclerView the recyclerView that holds the viewHolders
     * @param isClickable  - boolean value to set all the items clickable or not
     */
    private void setItemsClickable(RecyclerView recyclerView,
                                   boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    /**
     * @param c          the Canvas object to draw the icons
     * @param viewHolder - the current viewHolder where the drawing is requested
     */
    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        //set the button size without the padding and the rounded corners
        float buttonWidthWithoutPadding = buttonWidth - 20;
        float corners = 8;

        //get the view from viewHolder
        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        //create the buttons as RectF objects, and set their positions and bitmap images
        RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
        p.setColor(Color.WHITE);
        c.drawRoundRect(leftButton, corners, corners, p);
        drawBitmap(R.drawable.ic_call_green, c, leftButton);
        RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        c.drawRoundRect(rightButton, corners, corners, p);
        drawBitmap(R.drawable.ic_sms_blue, c, rightButton);

        buttonInstance = null;
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton;
        } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton;
        }
    }

    /**
     * @param drawableRes the drawable resource file to be drawn
     * @param canvas      the canvas where the drawable resource should be drawn
     * @param button      the button rectangle to adjust the draw to the button sizes
     */
    private void drawBitmap(int drawableRes, Canvas canvas, RectF button) {

        //get drawable from resource
        Drawable drawable = context.getDrawable(drawableRes);

        //set drawable's bounds to fit rect's sizes
        Rect rect = new Rect();
        button.round(rect);
        drawable.setBounds(rect);

        //draw the drawable on canvas
        drawable.draw(canvas);

        //set drawable width and height
        float imgWidth = drawable.getIntrinsicWidth();
        float imgHeight = drawable.getIntrinsicHeight();

        //create a Bitmap object with corresponding sizes and ARGB_4444 configuration
        // (each color represented as a 4-bit color)
        Bitmap bitmap
                = Bitmap.createBitmap((int) imgHeight,
                (int) imgWidth,
                Bitmap.Config.ARGB_4444);

        //draw the bitmap on canvas
        canvas.drawBitmap(bitmap,
                button.centerX() - (imgWidth / 2),
                button.centerY() + (imgHeight / 2), null);
    }

    /**
     * Drawing method, accessible from outside of the {@link SwipeController} class
     *
     * @param c the canvas where the buttons should be drawn
     */
    public void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder);
        }
    }
}
