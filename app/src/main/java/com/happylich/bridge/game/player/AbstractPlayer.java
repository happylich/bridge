package com.happylich.bridge.game.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.happylich.bridge.engine.util.Position;
import com.happylich.bridge.game.Scene.Call;
import com.happylich.bridge.game.Scene.Table;
import com.happylich.bridge.game.res.CardImage;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wangt on 2017/11/16.
 */

/**
 * TODO:玩家的绘制工作与Game.stage无关！
 */
public abstract class AbstractPlayer {

    protected Context context;

    // 储存游戏的阶段
    protected int stage;

    // 储存玩家的座位0-S 1-W 2-N 3-E
    public int position;

    // 储存玩家绘制信息
    protected int width, height;
    protected int top, left;

    // 本地玩家用来获取叫牌值
    public Call call;
    // 本地玩家用来获取出牌值
    public Table table;

    // 玩家持有的牌
    protected ArrayList<Integer> cards;

    // 当前选中的牌(table)
    protected int selectCardIndex = -1;
    protected int selectCard = -1;


    // 玩家状态（根据状态采取不同的绘制策略）
    // 000表示叫牌阶段：玩家+叫牌
    // 100表示调整座位：
    // 200：玩家+桌面
    // 200：不绘制
    // 201：下方，向上
    // 202：下方，向下
    // 211：上方，向上
    // 212：上方，向下
    // 221：左方，向上
    // 222：左方，向下
    // 231：右方，向上
    // 232：右方，向下
    // 3表示结算：结算

    /**
     * 构造函数
     */
    public AbstractPlayer() {
    }

    /**
     * 设置玩家持有的call副本
     * @param call
     */
    public void setCall(Call call) {
        this.call = call;
    }

    /**
     * 设置玩家持有的table副本
     * @param table
     */
    public void setTable(Table table) {
        this.table = table;
    }

    /**
     * 设置绘图模式
     * @param stage
     */
    public void setStage(int stage) {
        this.stage = stage;
    }

    /**
     * 设置绘图基准点
     * @param position
     */
    public void setPosition(int[] position) {
        this.left = position[0];
        this.top = position[1];
    }

    /**
     * 设置宽高
     */
    public void setWidthHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 玩家初始化手牌
     */
    public void setCards(ArrayList<Integer> cards) {
        this.cards = cards;
    }

    /**
     * 玩家出牌
     * @param cardNumber
     */
    public int removeCard(int cardNumber) {
        return this.cards.remove(cardNumber);
    }

    /**
     * 获得玩家手牌
     * @return
     */
    public ArrayList getCards() {
        return cards;
    }


    /**
     * 叫牌函数
     * 叫牌函数返回0-35，0-34表示有效叫牌值，35表示pass
     */
    abstract public boolean callCard();

    /**
     * 打牌函数
     */
    abstract public boolean dropCard();

    /**
     * 绘图函数
     * @param canvas
     */
    public void draw(Canvas canvas, Paint paint,  Rect rect) {
        switch(stage) {
            case 200:
                // do-nothing
                break;
            case 201:
                paintBottomUp(canvas, paint, rect);
                break;
            case 202:
                break;
            case 211:
                paintLeftUp(canvas, paint, rect);
                break;
            case 212:
                break;
            case 221:
                paintTopUp(canvas, paint, rect);
                break;
            case 222:
                paintTopDown(canvas, paint, rect);
                break;
            case 231:
                paintRightUp(canvas, paint, rect);
                break;
            case 232:
                break;
                // do-nothing
        }
    }

    /**
     * 南家触摸事件
     * @param x
     * @param y
     * @return
     */
    public int touchBottom(int x, int y) {
        Position position;
        Position positionSelected1;
        Position positionSelected2;

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = (1440 - (cards.size() - 1) * 90 - 180) / 2;
        int top = this.top;

        for (int i=0; i<cards.size(); i++) {

            if (selectCard != -1) {
                // 如果已经选中牌了，则出牌或者重新选牌
                if (i < cards.size() - 1) {
                    // 不是最后一张牌
                    position = new Position(top, left + i * 90,
                            top + 240, left + 90 + i * 90);
                } else {
                    // 是最后一张牌
                    position = new Position(top, left + i * 90,
                            top + 240, left + 180 + i * 90);
                }
                position.resieze((float)this.width / (float)1440);

                positionSelected1 = new Position(top - 120, left + selectCardIndex * 90,
                        top, left + 180 + selectCardIndex * 90);
                positionSelected1.resieze((float)this.width / (float)1440);

                positionSelected2 = new Position(top, left + selectCardIndex * 90,
                        top + 120, left + 90 + selectCardIndex * 90);
                positionSelected2.resieze((float)this.width / (float)1440);

                if (Position.inPosition(x, y, positionSelected1) || Position.inPosition(x, y, positionSelected2)) {
                    // 出牌
                    Log.v(this.getClass().getName(), "出牌");
                    table.dropCard(this.position, cards.remove(selectCardIndex));
                    selectCardIndex = -1;
                    selectCard = -1;
                    return 2;
                } else if (Position.inPosition(x, y, position)) {
                    // 换牌
                    Log.v(this.getClass().getName(), "换牌");
                    selectCardIndex = i;
                    selectCard = cards.get(i);
                    return 1;
                }
                Log.v(this.getClass().getName(), "既不出牌也不换牌");
            } else {
                // 如果没有选中牌，则选牌
                if (i < cards.size() - 1) {
                    position = new Position(top, left + i * 90,
                            top + 240, left + 90 + i * 90);
                } else {
                    position = new Position(top, left + i * 90,
                            top + 240, left + 180 + i * 90);
                }
                position.resieze((float)this.width / (float)1440);
                if (Position.inPosition(x, y, position)) {
                    // 选中牌
                    Log.v(this.getClass().getName(), "选中牌");
                    selectCardIndex = i;
                    selectCard = cards.get(i);
                    return 1;
                }
            }
        }
        Log.v(this.getClass().getName(), "什么都不做");
        return 0;
    }

    /**
     * 北家触摸事件
     * @param x
     * @param y
     * @return
     */
    public int touchTop(int x, int y) {
        Position position;
        Rect des = new Rect();

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = (1440 - (cards.size() - 1) * 80 - 180) / 2;
        int top = this.top;

        Log.v(this.getClass().getName(), "touch-top:" + String.valueOf(this.top));
        Log.v(this.getClass().getName(), "touch-width:" + String.valueOf(this.width));

        // 绘制纸牌（底部玩家）
        for (int i=0; i<cards.size(); i++) {
            if (selectCard != -1) {
                position = new Position(top + 120, left + i * 90,
                        top + 360, left + i * 90);
                position.resieze((float)this.width / (float)1440);
                if (Position.inPosition(x, y, position)) {
                    // 出牌
                    Log.v(this.getClass().getName(), "出牌");
                    return 2;
                }
            } else {
                position = new Position(top, left + i * 80,
                        top + 240, left + 180 + i * 80);
                position.resieze((float)this.width / (float)1440);
                if (Position.inPosition(x, y, position)) {
                    // 选中牌
                    Log.v(this.getClass().getName(), "选中牌");
                    selectCard = i;
                    return 1;
                }
            }
        }
        Log.v(this.getClass().getName(), "什么都不做");
        return 0;
    }


    /**
     * 南家绘制
     * @param canvas
     */
    private void paintBottomUp(Canvas canvas, Paint paint, Rect des) {
        Bitmap Image;
        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = (1440 - (cards.size() - 1) * 90 - 180) / 2;
        int top = this.top;


//        Log.v(this.getClass().getName(), "paint-top:" + String.valueOf(this.top));
//        Log.v(this.getClass().getName(), "paint-width:" + String.valueOf(this.width));

        // 绘制纸牌（底部玩家）
        for (int i=0; i<cards.size(); i++) {
            Date d = new Date();
//            Log.v(this.getClass().getName(), String.valueOf(CardImage.cardBitmapImages.size()));
            Image = CardImage.cardBitmapImages.get(cards.get(i));
//            Image = BitmapFactory.decodeResource(context.getResources(), CardImage.cardImages[cards.get(i)]);
//            if ((selectCard != -1) && (cards[i]/13 == cards[selectCard]/13)) {
//            Log.v(this.getClass().getName(), "加载图片  " + String.valueOf((new Date().getTime() - d.getTime())));

            if ((selectCard != -1) && (cards.get(i) == selectCard)) {
                des.set(left + i * 90, top - 120, left + 180 + i * 90, top + 120);
            } else {
                des.set(left + i * 90, top, left + 180 + i * 90, top + 240);
            }
            canvas.drawBitmap(Image,null, des, paint);
//            Log.v(this.getClass().getName(), "绘制图片  " + String.valueOf((new Date().getTime() - d.getTime())));
            Image = null;
        }
    }

    /**
     * 北家绘制（明）
     * @param canvas
     */
    private void paintTopUp(Canvas canvas, Paint paint, Rect des) {
        Bitmap Image;

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = (1440 - (cards.size() - 1) * 80 - 180) / 2;
        int top = this.top;

//        Log.v(this.getClass().getName(), "paint-top:" + String.valueOf(this.top));
//        Log.v(this.getClass().getName(), "paint-width:" + String.valueOf(this.width));


        // 绘制纸牌（底部玩家）
        for (int i=0; i<cards.size(); i++) {
            if ((selectCard != -1) && (cards.get(i)/13 == cards.get(selectCard)/13)) {

//                Image = CardImage.decodeSampledBitmapFromResource(context.getResources(), CardImage.cardImages[cards.get(i)], 180, 240);
                Image = CardImage.cardBitmapImages.get(cards.get(i));
//                Image = BitmapFactory.decodeResource(context.getResources(), CardImage.cardImages[cards.get(i)]);
                des.set(left + i * 80, top + 120, left + 180 + i * 80, top + 360);
                canvas.drawBitmap(Image,null, des, paint);
                Image = null;
            } else {
                Image = CardImage.decodeSampledBitmapFromResource(context.getResources(), CardImage.cardImages[cards.get(i)], 180, 240);
//                Image = BitmapFactory.decodeResource(context.getResources(), CardImage.cardImages[cards.get(i)]);
                des.set(left + i * 80, top, left + 180 + i * 80, top + 240);
                canvas.drawBitmap(Image,null, des, paint);
                Image = null;
            }
        }
    }

    /**
     * 北家绘制（暗）
     * @param canvas
     */
    private void paintTopDown(Canvas canvas, Paint paint, Rect des) {
        Bitmap Image;

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = (1440 - (cards.size() - 1) * 80 - 180) / 2;

        // 绘制纸牌（底部玩家）
        for (int i=0; i<cards.size(); i++) {

//            Image = CardImage.decodeSampledBitmapFromResource(context.getResources(), CardImage.cardImages[cards.get(i)], 180, 240);
            Image = CardImage.cardBitmapImages.get(cards.get(i));
//            Image = BitmapFactory.decodeResource(context.getResources(), CardImage.backImage);
            des.set(left + i * 80, top, left + 180 + i * 80, top + 240);
            canvas.drawBitmap(Image,null, des, paint);
            Image = null;
        }
    }

    /**
     * 西家绘制（明）
     * @param canvas
     */
    private void paintLeftUp(Canvas canvas, Paint paint, Rect des) {
        Bitmap Image;

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = this.left;
        int top = this.top;

        int sflag = 0;
        int hflag = 0;
        int dflag = 0;
        int cflag = 0;

        int sFull = -1;
        int hFull = -1;
        int dFull = -1;
        int cFull = -1;

        for (int i=0; i<cards.size(); i++) {

//            Image = CardImage.decodeSampledBitmapFromResource(context.getResources(), CardImage.cardImages[cards.get(i)], 180, 240);
            Image = CardImage.cardBitmapImages.get(cards.get(i));
//            Image = BitmapFactory.decodeResource(context.getResources(), CardImage.cardImages[cards.get(i)]);
            if (cards.get(i) >= 39 && cards.get(i) <= 51) {
                sFull++;
                sflag = 1;
            } else if (cards.get(i) >= 26 && cards.get(i) <= 38) {
                hFull++;
                hflag = 1;
            } else if (cards.get(i) >= 13 && cards.get(i) <= 25) {
                dFull++;
                dflag = 1;
            } else if (cards.get(i) >= 0 && cards.get(i) <= 12) {
                cFull++;
                cflag = 1;
            }

            if (cards.get(i) >= 39 && cards.get(i) <= 51) {
                des.set(left + 60 * sFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left + 180 + 60 * sFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);

            } else if (cards.get(i) >= 26 && cards.get(i) <= 38) {
                des.set(left + 60 * hFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left + 180 + 60 * hFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);

            } else if (cards.get(i) >= 13 && cards.get(i) <= 25) {
                des.set(left + 60 * dFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left + 180 + 60 * dFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);

            } else if (cards.get(i) >= 0 && cards.get(i) <= 12) {
                des.set(left + 60 * cFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left + 180 + 60 * cFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);

            }
            canvas.drawBitmap(Image,null, des, paint);
            Image = null;
        }
    }

    /**
     * 东家绘制（明）
     * @param canvas
     */
    private void paintRightUp(Canvas canvas, Paint paint, Rect des) {
        Bitmap Image;

        // 虽然规定了left，但是并不采用，实际情况下还是根据width重新绘制
        int left = this.left;
        int top = this.top;

        int sflag = 0;
        int hflag = 0;
        int dflag = 0;
        int cflag = 0;

        int sFull = -1;
        int hFull = -1;
        int dFull = -1;
        int cFull = -1;

        for (int i=0; i<cards.size(); i++) {
            if (cards.get(i) >= 39 && cards.get(i) <= 51) {
                sFull++;
            } else if (cards.get(i) >= 26 && cards.get(i) <= 38) {
                hFull++;
            } else if (cards.get(i) >= 13 && cards.get(i) <= 25) {
                dFull++;
            } else if (cards.get(i) >= 0 && cards.get(i) <= 12) {
                cFull++;
            }
        }

        for (int i=0; i<cards.size(); i++) {
            if (cards.get(i) >= 39 && cards.get(i) <= 51) {
                sflag = 1;
            } else if (cards.get(i) >= 26 && cards.get(i) <= 38) {
                hflag = 1;
            } else if (cards.get(i) >= 13 && cards.get(i) <= 25) {
                dflag = 1;
            } else if (cards.get(i) >= 0 && cards.get(i) <= 12) {
                cflag = 1;
            }

//            Image = CardImage.decodeSampledBitmapFromResource(context.getResources(), CardImage.cardImages[cards.get(i)], 180, 240);
            Image = CardImage.cardBitmapImages.get(cards.get(i));
//            Image = BitmapFactory.decodeResource(context.getResources(), CardImage.cardImages[cards.get(i)]);
            if (cards.get(i) >= 39 && cards.get(i) <= 51) {
                des.set(left - 180 - 60 * sFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left - 60 * sFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);
                sFull--;
            } else if (cards.get(i) >= 26 && cards.get(i) <= 38) {
                des.set(left - 180 - 60 * hFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left - 60 * hFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);
                hFull--;
            } else if (cards.get(i) >= 13 && cards.get(i) <= 25) {
                des.set(left - 180 - 60 * dFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left - 60 * dFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);
                dFull--;
            } else if (cards.get(i) >= 0 && cards.get(i) <= 12) {
                des.set(left - 180 - 60 * cFull, top + (sflag + hflag + dflag + cflag) * 180,
                        left - 60 * cFull, top + 240 + (sflag + hflag + dflag + cflag) * 180);
                cFull--;
            }
            canvas.drawBitmap(Image,null, des, paint);
            Image = null;
        }
    }


    /**
     * 获得大牌点
     * @return
     */
    public int getPoints() {
        int point = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) % 13 >= 9) {
                point = point + ((cards.get(i) / 13) - 8);
            }
        }
        return point;
    }

    /**
     * 获得牌点调整值
     * @param color
     */
    public int getPointsExt(int color) {
        int point = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) / 13 == color) {
                point++;
            }
        }
        switch (point) {
            case 0:
                return 5;
            case 1:
                return 3;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 是否是均型牌
     * @return
     */
    public int isBalance() {
        int[] numbers = {0, 0, 0, 0};
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) / 13 == 0) {
                numbers[0]++;
            } else if (cards.get(i) / 13 == 1) {
                numbers[1]++;
            } else if (cards.get(i) / 13 == 2) {
                numbers[2]++;
            } else {
                numbers[3]++;
            }
        }

        // 写一个冒泡循环
        for (int i = 4; i > 0; i++) {
            for (int j = 0; j < i; j++) {
                if (numbers[j] < numbers[j+1]) {
                    int temp = numbers[j];
                    numbers[j] = numbers[j+1];
                    numbers[j+1] = temp;
                }
            }
        }

        if (numbers[0] == 4 && numbers[1] == 3 && numbers[2] == 3 && numbers[3] == 3) {
            return 0;
        }
        if (numbers[0] == 4 && numbers[1] == 4 && numbers[2] == 3 && numbers[3] == 2) {
            return 0;
        }
        if (numbers[0] == 5 && numbers[1] == 3 && numbers[2] == 3 && numbers[3] == 2) {
            return 0;
        }
        if (numbers[0] == 5 && numbers[1] == 4 && numbers[2] == 2 && numbers[3] == 2) {
            return 1;
        }
        if (numbers[0] == 6 && numbers[1] == 3 && numbers[2] == 2 && numbers[3] == 2) {
            return 1;
        }
        return 2;
    }
}
