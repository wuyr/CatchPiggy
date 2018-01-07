package com.wuyr.catchpiggy.utils;

import com.wuyr.catchpiggy.customize.views.Item;
import com.wuyr.catchpiggy.models.MissionData;

/**
 * Created by wuyr on 17-10-23 下午5:40.
 */

public class LevelUtil {

    public static int CLASSIC_MODE_MAX_LEVEL = 50;
    public static int PIGSTY_MODE_MAX_LEVEL = 51;

    public static MissionData getMissionData(int level) {
        MissionData missionData = new MissionData();
        switch (level) {
            case 1:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 1;
                missionData.propDelay = 1500;
                break;

            case 2:
                missionData.speed = 550L;
                missionData.mustCaughtCount = 2;
                missionData.propDelay = 1500;
                break;

            case 3:
                missionData.speed = 600L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 4:
                missionData.speed = 600L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 5:
                missionData.speed = 600L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 6:
                missionData.speed = 600L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 7:
                missionData.speed = 590L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 8:
                missionData.speed = 570L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 9:
                missionData.speed = 580L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 10:
                missionData.speed = 570L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 11:
                missionData.speed = 560L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 12:
                missionData.speed = 570L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 13:
                missionData.speed = 560L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 14:
                missionData.speed = 550L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 15:
                missionData.speed = 550L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 16:
                missionData.speed = 550L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 17:
                missionData.speed = 550L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 18:
                missionData.speed = 530L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 19:
                missionData.speed = 535L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 20:
                missionData.speed = 540L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 21:
                missionData.speed = 540L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 22:
                missionData.speed = 540L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 23:
                missionData.speed = 530L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 24:
                missionData.speed = 530L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 25:
                missionData.speed = 530L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 26:
                missionData.speed = 540L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 27:
                missionData.speed = 530L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 28:
                missionData.speed = 520L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 29:
                missionData.speed = 520L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 30:
                missionData.speed = 520L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 31:
                missionData.speed = 520L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 32:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 33:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 34:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 35:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1700;
                break;

            case 36:
                missionData.speed = 500L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 2000;
                break;

            case 37:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 3;
                missionData.propDelay = 1500;
                break;

            case 38:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 1500;
                break;

            case 39:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 1500;
                break;

            case 40:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 1500;
                break;

            case 41:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 2000;
                break;

            case 42:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 2300;
                break;

            case 43:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 2500;
                break;

            case 44:
                missionData.speed = 490L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 2500;
                break;

            case 45:
                missionData.speed = 480L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 2500;
                break;

            case 46:
                missionData.speed = 480L;
                missionData.mustCaughtCount =5;
                missionData.propDelay = 2500;
                break;

            case 47:
                missionData.speed = 480L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 2500;
                break;

            case 48:
                missionData.speed = 470L;
                missionData.mustCaughtCount = 4;
                missionData.propDelay = 2500;
                break;

            case 49:
                missionData.speed = 470L;
                missionData.mustCaughtCount = 5;
                missionData.propDelay = 2500;
                break;

            case 50:
                missionData.speed = 470L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 3000;
                break;

            case 51:
                missionData.speed = 450L;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 3200;
                break;
            default:
                missionData.speed = 500;
                missionData.mustCaughtCount = 6;
                missionData.propDelay = 2000;
        }
        return missionData;
    }

    public static int[][] getDefaultFencePosition(int verticalCount, int horizontalCount, int level) {
        int[][] data = new int[verticalCount][horizontalCount];
        switch (level) {
            case 1:
                data[2][2] = data[1][2] = data[0][1] = data[2][1] = data[1][1] = data[2][0] = data[2][6] = data[1][7] = data[2][7] = data[1][8] = data[0][7] = data[2][8] = data[0][7] = data[6][1] = data[6][2] = data[6][0] = data[8][1] = data[7][2] = data[7][1] = data[6][6] = data[6][7] = data[6][8] = data[7][7] = data[7][8] = data[8][7] = data[2][4] = data[4][2] = data[6][4] = data[4][6] = Item.STATE_SELECTED;
                break;

            case 2:
                data[1][7] = data[1][5] = data[3][3] = data[4][8] = data[5][8] = data[7][6] = data[6][3] = data[7][2] = data[7][3] = data[8][2] = Item.STATE_SELECTED;
                break;

            case 3:
                data[6][6] = data[7][7] = data[6][7] = data[1][6] = data[1][5] = data[6][1] = data[7][2] = data[6][2] = data[3][1] = data[3][2] = Item.STATE_SELECTED;
                break;

            case 4:
                data[1][5] = data[1][4] = data[5][5] = data[7][7] = data[8][7] = data[7][8] = data[7][6] = data[6][2] = data[5][8] = Item.STATE_SELECTED;
                break;

            case 5:
                data[0][5] = data[1][5] = data[2][4] = data[7][6] = data[7][5] = data[7][4] = data[8][3] = data[2][1] = data[3][1] = Item.STATE_SELECTED;
                break;

            case 6:
                data[4][6] = data[3][6] = data[4][5] = data[2][5] = data[1][6] = data[1][7] = data[6][5] = data[6][6] = data[6][7] = data[5][8] = Item.STATE_SELECTED;
                break;

            case 7:
                data[5][3] = data[5][2] = data[5][1] = data[6][1] = data[7][2] = data[7][3] = data[7][4] = data[7][5] = data[0][0] = data[1][1] = data[1][0] = data[1][1] = Item.STATE_SELECTED;
                break;

            case 8:
                data[8][0] = data[7][2] = data[6][0] = data[0][0] = data[1][2] = data[2][0] = data[0][8] = data[0][6] = data[2][7] = Item.STATE_SELECTED;
                break;

            case 9:
                data[3][4] = data[3][5] = data[5][5] = data[6][5] = Item.STATE_SELECTED;
                break;

            case 10:
                break;

            case 11:
                data[1][6] = data[2][6] = data[1][3] = data[1][5] = data[7][6] = data[6][6] = data[7][5] = data[3][2] = data[4][1] = data[4][2] = data[5][2] = data[4][6] = data[6][3] = data[5][5] = data[3][4] = Item.STATE_SELECTED;
                break;

            case 12:
                data[4][5] = data[4][6] = data[7][7] = data[6][7] = data[2][2] = data[3][2] = data[3][3] = data[6][6] = data[5][7] = data[6][1] = data[6][2] = data[8][4] = data[1][6] = data[0][4] = data[0][5] = data[1][7] = Item.STATE_SELECTED;
                break;

            case 13:
                data[4][1] = data[2][6] = data[1][3] = data[6][5] = data[6][4] = data[1][4] = data[3][7] = data[3][6] = data[4][7] = data[5][7] = data[4][6] = Item.STATE_SELECTED;
                break;

            case 14:
                data[4][2] = data[4][1] = data[4][0] = data[4][6] = data[6][4] = data[7][4] = data[7][5] = data[8][4] = data[0][4] = data[1][4] = data[1][5] = data[2][4] = data[1][8] = data[8][7] = data[6][7] = data[6][1] = data[2][1] = Item.STATE_SELECTED;
                break;

            case 15:
                data[5][8] = data[4][7] = data[5][7] = data[5][6] = data[2][2] = data[2][1] = data[2][0] = data[3][1] = data[0][5] = data[0][6] = data[8][2] = data[8][1] = data[0][7] = data[8][3] = data[7][7] = Item.STATE_SELECTED;
                break;

            case 16:
                data[3][6] = data[5][7] = data[5][6] = data[4][6] = data[3][2] = data[4][1] = data[3][1] = data[1][4] = data[8][3] = data[7][6] = Item.STATE_SELECTED;
                break;

            case 17:
                data[8][8] = data[7][7] = data[8][7] = data[7][8] = data[6][7] = data[8][6] = data[0][8] = data[0][7] = data[2][7] = data[1][1] = data[1][0] = data[2][1] = data[6][1] = data[8][3] = data[6][3] = Item.STATE_SELECTED;
                break;

            case 18:
                data[3][2] = data[3][1] = data[6][1] = data[6][0] = data[5][1] = data[4][1] = data[4][7] = data[5][7] = Item.STATE_SELECTED;
                break;

            case 19:
                data[7][6] = data[8][5] = data[6][6] = data[8][4] = data[6][2] = data[5][3] = data[5][2] = data[6][3] = data[1][2] = data[1][6] = data[1][7] = Item.STATE_SELECTED;
                break;

            case 20:
                data[6][5] = data[7][6] = data[8][6] = data[6][6] = data[6][7] = data[0][1] = data[1][1] = data[1][0] = data[1][7] = data[0][6] = data[1][8] = data[5][2] = data[6][1] = data[8][0] = data[7][1] = Item.STATE_SELECTED;
                break;

            case 21:
                data[1][2] = data[8][5] = data[7][6] = data[7][7] = data[7][8] = data[6][7] = data[5][8] = data[1][6] = data[2][5] = data[1][3] = data[2][3] = data[2][4] = Item.STATE_SELECTED;
                break;

            case 22:
                data[6][2] = data[5][3] = data[6][3] = data[7][3] = data[8][2] = data[5][2] = data[5][1] = data[5][0] = data[0][6] = data[1][6] = data[2][5] = data[2][6] = data[2][7] = data[6][7] = Item.STATE_SELECTED;
                break;

            case 23:
                data[3][3] = data[3][2] = data[4][2] = data[5][4] = data[5][5] = Item.STATE_SELECTED;
                break;

            case 24:
                data[3][7] = data[4][7] = data[5][7] = data[2][6] = data[6][6] = data[6][5] = data[2][5] = Item.STATE_SELECTED;
                break;

            case 25:
                data[5][3] = data[6][3] = data[7][4] = data[8][4] = data[7][5] = data[5][6] = data[1][4] = data[2][3] = data[0][4] = Item.STATE_SELECTED;
                break;

            case 26:
                data[1][4] = data[3][5] = data[2][4] = data[2][3] = data[4][2] = data[3][3] = data[4][5] = Item.STATE_SELECTED;
                break;

            case 27:
                data[4][6] = data[4][7] = data[4][8] = data[0][4] = data[0][5] = data[8][4] = Item.STATE_SELECTED;
                break;

            case 28:
                data[8][4] = data[1][5] = data[1][4] = data[7][4] = data[6][4] = data[7][5] = data[8][5] = data[8][3] = data[3][8] = data[5][1] = Item.STATE_SELECTED;
                break;

            case 29:
                data[7][6] = data[6][6] = data[8][7] = data[8][6] = data[0][6] = data[2][6] = data[2][7] = data[1][6] = data[1][3] = data[6][1] = data[7][1] = data[7][0] = data[8][1] = Item.STATE_SELECTED;
                break;

            case 30:
                data[6][1] = data[7][1] = data[7][0] = data[8][1] = data[1][2] = data[2][2] = data[3][3] = data[6][6] = data[7][7] = data[8][7] = Item.STATE_SELECTED;
                break;

            case 31:
                data[0][7] = data[1][7] = data[1][6] = data[7][5] = data[8][4] = data[7][6] = data[5][2] = data[8][5] = data[8][6] = data[8][7] = data[8][8] = Item.STATE_SELECTED;
                break;

            case 32:
                data[3][7] = data[5][4] = data[5][5] = data[2][4] = data[3][2] = data[6][4] = data[7][4] = Item.STATE_SELECTED;
                break;

            case 33:
                data[5][2] = data[6][1] = data[7][1] = data[1][6] = data[3][7] = data[2][6] = Item.STATE_SELECTED;
                break;

            case 34:
                data[6][6] = data[7][7] = data[8][5] = data[5][2] = data[1][3] = data[1][4] = Item.STATE_SELECTED;
                break;

            case 35:
                data[3][5] = data[2][5] = data[5][4] = data[6][3] = data[4][8] = data[4][0] = data[0][0] = Item.STATE_SELECTED;
                break;

            case 36:
                data[8][0] = data[8][8] = data[8][7] = data[0][8] = data[1][8] = data[0][0] = data[0][1] = data[7][1] = data[8][4] = Item.STATE_SELECTED;
                break;

            case 37:
                data[5][5] = data[5][6] = data[4][6] = data[6][4] = data[7][5] = data[8][5] = data[6][5] = data[7][6] = data[8][6] = data[6][6] = data[7][7] = data[8][7] = data[2][0] = data[2][1] = data[3][2] = data[5][7] = data[6][7] = data[7][8] = data[8][8] = Item.STATE_SELECTED;
                break;

            case 38:
                data[5][3] = data[6][2] = data[7][2] = data[6][1] = data[5][2] = data[0][5] = data[0][6] = Item.STATE_SELECTED;
                break;

            case 39:
                data[6][6] = data[6][7] = data[7][7] = data[0][3] = data[1][3] = data[0][2] = Item.STATE_SELECTED;
                break;

            case 40:
                data[5][3] = data[5][2] = data[5][4] = data[6][3] = data[7][3] = data[1][6] = data[2][5] = Item.STATE_SELECTED;
                break;

            case 41:
                data[8][3] = data[7][4] = data[6][4] = data[6][5] = data[7][6] = data[8][6] = data[0][7] = data[1][7] = data[2][6] = data[2][7] = data[2][8] = Item.STATE_SELECTED;
                break;

            case 42:
                data[2][0] = data[2][1] = data[3][2] = data[4][1] = data[5][1] = data[5][0] = data[7][5] = data[8][5] = data[8][6] = data[8][7] = data[7][8] = data[6][8] = data[5][8] = Item.STATE_SELECTED;
                break;

            case 43:
                data[3][4] = data[4][3] = data[5][4] = data[1][6] = data[7][7] = Item.STATE_SELECTED;
                break;

            case 44:
                data[4][2] = data[5][3] = data[5][4] = data[5][5] = data[5][6] = data[6][4] = data[7][5] = data[7][4] = Item.STATE_SELECTED;
                break;

            case 45:
                data[8][0] = data[7][1] = data[6][1] = data[5][2] = data[1][0] = data[2][0] = data[7][7] = data[1][6] = Item.STATE_SELECTED;
                break;

            case 46:
                data[1][7] = data[1][6] = data[2][7] = data[1][2] = data[2][1] = data[1][3] = Item.STATE_SELECTED;
                break;

            case 47:
                data[3][5] = data[4][5] = data[4][6] = data[4][7] = data[7][2] = data[7][1] = data[8][1] = Item.STATE_SELECTED;
                break;

            case 48:
                data[8][3] = data[3][1] = data[0][5] = data[5][7] = Item.STATE_SELECTED;
                break;

            case 49:
                data[7][6] = data[7][2] = data[7][3] = data[1][2] = data[2][1] = Item.STATE_SELECTED;
                break;

            case 50:
                data[1][5] = data[1][4] = data[1][3] = data[2][2] = data[2][1] = data[2][0] = data[8][2] = Item.STATE_SELECTED;
                break;

            default:
                break;
        }
        return data;
    }
}
