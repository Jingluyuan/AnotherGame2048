package com.game.anothergame2048;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jingluyuan on 6/24/18.
 */

public class Game2048Layout extends RelativeLayout {

    private int mColumn = 4;
    private Game2048Item[] game2048Items;
    private int mMargin = 10;
    private int mPadding;
    private GestureDetector mGestureDetector;
    private boolean isMergeHappen = true;
    private boolean isMoveHappen = true;
    private boolean isFirst = true;
    private int mScore;
    private OnGame2048Listener mGame2048Listener;

    public Game2048Layout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,mMargin,getResources().getDisplayMetrics());  // dp to px
        mPadding = Math.min(getPaddingTop(),getPaddingBottom());
        mGestureDetector = new GestureDetector(context,new MyGestureDetector());
    }

    public Game2048Layout(Context context) {
        this(context, null);
    }

    public Game2048Layout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }



    private boolean once;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        int length = Math.min(getMeasuredHeight(),getMeasuredWidth());

        int childWidth = (length-mPadding*2 -mMargin*(mColumn-1))/mColumn;

        if(!once)
        {
            if (game2048Items == null)
            {
                game2048Items = new Game2048Item[mColumn*mColumn];
            }

            for (int i=0;i<game2048Items.length;i++)
            {
                Game2048Item item = new Game2048Item(getContext());
                game2048Items[i] = item;
                item.setId(i+1);                                                           // set ID form each item view due to the dynamic adding
                RelativeLayout.LayoutParams lp = new LayoutParams(childWidth,childWidth);  // set item location
                if ((i+1)%mColumn != 0)
                {
                    lp.rightMargin = mMargin;
                }
                if (i%mColumn != 0)
                {
                    lp.addRule(RelativeLayout.RIGHT_OF,game2048Items[i-1].getId());
                }
                if ((i+1)>mColumn)
                {
                    lp.topMargin = mMargin;
                    lp.addRule(RelativeLayout.BELOW,game2048Items[i-mColumn].getId());
                }
                addView(item,lp);
            }
            generateNum();
        }
        once = true;

        setMeasuredDimension(length,length);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private enum ACTION{
        LEFT,RIGHT,UP,DOWN
    }
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener
    {
        final int FLING_MIN_DISTANCE = 50;


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float move_x, float move_y)
        {
            float x = e2.getX() - e1.getX();
            float y = e2.getY() - e1.getY();

            if (x>FLING_MIN_DISTANCE&& Math.abs(move_x)>Math.abs(move_y))
            {
                action(ACTION.RIGHT);
            } else if (x< -FLING_MIN_DISTANCE&& Math.abs(move_x)>Math.abs(move_y))
            {
                action(ACTION.LEFT);
            }
            else if (y>FLING_MIN_DISTANCE&& Math.abs(move_x)<Math.abs(move_y))
            {
                action(ACTION.DOWN);
            }
            else if (y< -FLING_MIN_DISTANCE&& Math.abs(move_x)<Math.abs(move_y))
            {
                action(ACTION.UP);
            }
            return true;
        }

        private void action(ACTION action)
        {
            for (int i=0;i<mColumn;i++)
            {
                List<Game2048Item> row = new ArrayList<Game2048Item>();

                for (int j=0;j<mColumn;j++)
                {
                    int rowIndex = getRowIndexByAction(action, i, j);
                    int colIndex = getColIndexByAction(action, i, j);
                    int index = 4*rowIndex+colIndex;
                    Game2048Item item = game2048Items[index];
                    if (item.getmNumber() != 0)
                    {
                        row.add(item);
                    }
                }

                for (int j=0;j<mColumn&&j<row.size();j++)
                {
                    int rowIndex = getRowIndexByAction(action, i, j);
                    int colIndex = getColIndexByAction(action, i, j);
                    int index = 4*rowIndex+colIndex;
                    Game2048Item item = game2048Items[index];
                    if (item.getmNumber() != row.get(j).getmNumber())
                    {
                        isMoveHappen = true;
                    }
                }

                mergeItem(row);

                for (int j=0;j<mColumn;j++)
                {
                    int rowIndex = getRowIndexByAction(action, i, j);
                    int colIndex = getColIndexByAction(action, i, j);
                    int index = 4*rowIndex+colIndex;
                    if (row.size()>j)
                    {
                        game2048Items[index].setmNumber(row.get(j).getmNumber());
                    }else
                    {
                        game2048Items[index].setmNumber(0);
                    }
                }


            }
            generateNum();
        }

        private void mergeItem(List<Game2048Item> row)
        {
            if (row.size()<2)
            {
                return;
            }

            for (int j=0;j<row.size()-1;j++)
            {
                Game2048Item item1 = row.get(j);
                Game2048Item item2 = row.get(j+1);

                if (item1.getmNumber() == item2.getmNumber())
                {
                    isMergeHappen = true;
                    int val = item1.getmNumber()+item2.getmNumber();
                    item1.setmNumber(val);

                    mScore += val;

                    if (mGame2048Listener != null)
                    {
                        mGame2048Listener.onScoreChange(mScore);
                    }

                    for (int k=j+1;k<row.size()-1;k++)
                    {
                        row.get(k).setmNumber(row.get(k+1).getmNumber());
                    }

                    row.get(row.size()-1).setmNumber(0);
                    return;

                }
            }
        }
    }

    public void generateNum()
    {
        if (checkOver())
        {

                mGame2048Listener.onGameOver();

            return;
        }

        if (isFirst)
        {
            Random random = new Random();
            int next = random.nextInt(16);
            Game2048Item item = game2048Items[next];

            while (item.getmNumber() != 0)
            {
                next = random.nextInt(16);
                item = game2048Items[next];
            }

            item.setmNumber(Math.random() > 0.75 ? 4 : 2);

            isMergeHappen = isMoveHappen = false;

            isFirst = false;

        }

        if (!isFull())
        {
            if (isMoveHappen || isMergeHappen)
            {
                Random random = new Random();
                int next = random.nextInt(16);
                Game2048Item item = game2048Items[next];

                while (item.getmNumber() != 0)
                {
                    next = random.nextInt(16);
                    item = game2048Items[next];
                }

                item.setmNumber(Math.random() > 0.75 ? 4 : 2);

                isMergeHappen = isMoveHappen = false;
            }

        }
    }

    private boolean checkOver()
    {
        if (!isFull())
        {
            return false;
        }

        for (int i=0;i<mColumn;i++)
        {
            for (int j=0;j<mColumn;j++)
            {
                int index = i*mColumn+j;
                Game2048Item item = game2048Items[index];

                if ((index+1)%mColumn != 0)
                {
                    Game2048Item itemRight = game2048Items[index+1];
                    if (item.getmNumber() == itemRight.getmNumber())
                        return false;
                }

                if ((index+mColumn) < mColumn*mColumn)
                {
                    Game2048Item itemBottom = game2048Items[index+mColumn];
                    if (item.getmNumber() == itemBottom.getmNumber())
                        return false;
                }

                if (index%mColumn !=0)
                {
                    Game2048Item itemLeft = game2048Items[index-1];
                    if (item.getmNumber() == itemLeft.getmNumber())
                        return false;
                }

                if (index+1 > mColumn)
                {
                    Game2048Item itemUp = game2048Items[index-mColumn];
                    if (item.getmNumber() == itemUp.getmNumber())
                        return false;
                }
            }
        }
        return true;

    }

    private boolean isFull()
    {

        for (int i = 0; i < game2048Items.length; i++)
        {
            if (game2048Items[i].getmNumber() == 0)
            {
                return false;
            }
        }
        return true;
    }

    public interface OnGame2048Listener{
        void onScoreChange(int score);

        void onGameOver();
    }

    public void setOnGame2048Listener(OnGame2048Listener onGame2048Listener)
    {
        this.mGame2048Listener = onGame2048Listener;
    }

    public void restart()
    {
        for (int i=0;i<mColumn;i++)
        {
            for (int j=0;j<mColumn;j++)
            {
                int index = getIndex(i,j);
                Game2048Item item = game2048Items[index];
                item.setmNumber(0);
            }
        }
        mScore =0;
        mGame2048Listener.onScoreChange(0);
        isFirst = true;
        generateNum();
    }

    private int getIndex(int i, int j) {
        int index = mColumn*i+j;
        return index;
    }


    private int getRowIndexByAction(ACTION action, int i, int j) {
        int rowIndex = -1;

        switch (action) {
            case LEFT:
            case RIGHT:
                rowIndex = i;
                break;
            case UP:
                rowIndex = j;
                break;
            case DOWN:
                rowIndex = mColumn - 1 - j;
                break;
        }
        return rowIndex;

    }

    private int getColIndexByAction(ACTION action, int i, int j) {
        int colIndex = -1;
        switch (action) {
            case LEFT:
                colIndex = j;
                break;
            case RIGHT:
                colIndex = mColumn - 1 - j;
                break;
            case UP:
            case DOWN:
                colIndex = i;
                break;
        }
        return colIndex;
    }
}
