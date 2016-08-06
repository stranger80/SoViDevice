package org.stranger80.sovicontroller.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

/*
File:              RoundKnobButton
Version:           1.0.0
Release Date:      November, 2013
License:           GPL v2
Description:	   A round knob button to control volume and toggle between two states

****************************************************************************
Copyright (C) 2013 Radu Motisan  <radu.motisan@gmail.com>

http://www.pocketmagic.net

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
****************************************************************************/

public class RoundKnobButton extends RelativeLayout implements OnGestureListener {

    private GestureDetector 	gestureDetector;
    private float 				mAngleDown , mAngleUp;
    private ImageView			ivRotor;
    private Bitmap 				bmpRotorOn , bmpRotorOff;
    private boolean 			mState = false;
    private int                 percentage = 0;

    public static final String TAG = "RoundKnobButton";

    public interface RoundKnobButtonListener {
        public void onStateChange(boolean newstate) ;
        public void onRotate(int percentage);
    }

    private RoundKnobButtonListener eventListener;

    /**
     * Set the event listener object.
     *
     * @param l
     */
    public void setListener(RoundKnobButtonListener l) {
        this.eventListener = l;
    }

    public void setState(boolean state) {
        mState = state;
        ivRotor.setImageBitmap(state ? bmpRotorOn : bmpRotorOff);
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
        this.setRotorPercentage(this.percentage);
    }

    public RoundKnobButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.initializeControl(context,
                attributeSet.getAttributeResourceValue("http://org.stranger80", "background", 0),
                attributeSet.getAttributeResourceValue("http://org.stranger80", "rotoron", 0),
                attributeSet.getAttributeResourceValue("http://org.stranger80", "rotoroff", 0)
        );


    }

    private void initializeControl(Context context, int back, int rotoron, int rotoroff)
    {
        // create stator
        ImageView ivBack = new ImageView(context);
        ivBack.setImageResource(back);
        RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivBack, lp_ivBack);

        if( rotoron != 0 ) {
            // load rotor images
            bmpRotorOn = BitmapFactory.decodeResource(context.getResources(), rotoron);
            bmpRotorOff = BitmapFactory.decodeResource(context.getResources(), rotoroff);

            // create rotor
            ivRotor = new ImageView(context);
            RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
            ivRotor.setImageBitmap(mState?bmpRotorOn:bmpRotorOff);
            addView(ivRotor, lp_ivKnob);
        }

        // set initial state
        setRotorPercentage(10);
        setState(mState);

        // enable gesture detector
        gestureDetector = new GestureDetector(getContext(), this);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        setRotorPercentage(this.percentage);
    }

    /**
     * math..
     * @param x
     * @param y
     * @return
     */
    private float cartesianToPolar(float x, float y) {
        return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) return true;
        else return super.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent event) {
        float x = event.getX() / ((float) getWidth());
        float y = event.getY() / ((float) getHeight());
        mAngleDown = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX() / ((float) getWidth());
        float y = e.getY() / ((float) getHeight());
        mAngleUp = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction

        // if we click up the same place where we clicked down, it's just a button press
        if (! Float.isNaN(mAngleDown) && ! Float.isNaN(mAngleUp) && Math.abs(mAngleUp-mAngleDown) < 10) {
            setState(!mState);
            if (eventListener != null) eventListener.onStateChange(mState);
        }
        return true;
    }

    public void setRotorPosAngle(float deg) {

        if (deg >= 210 || deg <= 150) {
            if (deg > 180) deg = deg - 360;
            Matrix matrix=new Matrix();
            ivRotor.setScaleType(ScaleType.MATRIX);

            if(getWidth() > getHeight()) {
                float scaleFactor = ((float)getHeight())/((float)bmpRotorOn.getHeight());
                matrix.postScale(scaleFactor, scaleFactor);

                int translate = (getWidth()-getHeight())/2;
                matrix.postTranslate(translate, 0);
            }
            else
            {
                float scaleFactor = ((float)getWidth())/((float)bmpRotorOn.getWidth());
                matrix.postScale(scaleFactor, scaleFactor);

                int translate = (getHeight()-getWidth())/2;
                matrix.postTranslate(0, translate);
            }

            matrix.postRotate((float) deg, getWidth()/2, getHeight()/2);
            ivRotor.setImageMatrix(matrix);
        }
    }

    private void setRotorPercentage(int percentage) {
        int posDegree = percentage * 3 - 150;
        if (posDegree < 0) posDegree = 360 + posDegree;
        setRotorPosAngle(posDegree);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float x = e2.getX() / ((float) getWidth());
        float y = e2.getY() / ((float) getHeight());
        float rotDegrees = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction

        if (! Float.isNaN(rotDegrees)) {
            // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
            float posDegrees = rotDegrees;
            if (rotDegrees < 0) posDegrees = 360 + rotDegrees;

            // deny full rotation, start start and stop point, and get a linear scale
            if (posDegrees > 210 || posDegrees < 150) {
                // rotate our imageview
                setRotorPosAngle(posDegrees);
                // get a linear scale
                float scaleDegrees = rotDegrees + 150; // given the current parameters, we go from 0 to 300
                // get position percentage
                this.percentage = (int) (scaleDegrees / 3);
                if (eventListener != null) {
                    eventListener.onRotate(this.percentage);
                }
                return true; //consumed
            } else
                return false;
        } else
            return false; // not consumed
    }

    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) { return false; }

    public void onLongPress(MotionEvent e) {	}





}
