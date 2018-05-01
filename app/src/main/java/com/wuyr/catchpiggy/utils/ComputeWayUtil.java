package com.wuyr.catchpiggy.utils;

import com.wuyr.catchpiggy.customize.views.Item;
import com.wuyr.catchpiggy.models.WayData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Created by wuyr on 17-10-16 下午11:54.
 */

/**
 * 亡猪补牢模式计算小猪逃跑路线的工具类
 */
public class ComputeWayUtil {

    private static final int STATE_WALKED = 3;//状态标记(已走过)

    /**
     * 以currentPos为中心点,向周围6个方向查找空闲的位置(广度优先遍历)
     * @param items 格子状态
     * @param ignorePos 需要忽略的格子
     * @param currentPos 起始的格子（以这个格子为起点向四周查找）
     * @return 空闲的格子
     */
    public static WayData findNextUnSelected(int[][] items, List<WayData> ignorePos, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            //复制数组(因为要对数组元素值进行修改，且不能影响原来的)
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);//当前pos先入队
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;//状态标记(已走过)
        while (!way.isEmpty()) {//队列不为空
            WayData header = way.poll();//队头出队
            List<WayData> directions = getCanArrivePosUnchecked(pattern, header);//获取周围6个方向的位置(不包括越界的)
            //遍历周边的位置
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                //判断该位置是否空闲,如果是空闲则直接返回,如果不是空闲,则入队,下次以它为中心,寻找周边的元素
                if (!currentPos.equals(direction) && items[direction.y][direction.x] == Item.STATE_UNSELECTED
                        && !(ignorePos != null && ignorePos.contains(direction))) {
                    return direction;
                } else {
                    way.offer(direction);
                }
            }
        }
        //队列直至为空还没返回,则找不到了
        return null;
    }

    /**
     经典模式小猪找出路
     */
    public static WayData findWay(int level, int[][] items, WayData currentPos, List<WayData> data) {
        //经典模式第10关之后,小猪变聪明
        if (level < 0 || level > 10) {
            List<WayData> list = findWay2(items, currentPos);
            if (list != null && list.size() >= 2) {
                return list.get(1);
            }
        }
        //第10关之前,向周围没有被拦住(可以直线一直走)的方向走
        WayData result = null;
        for (WayData tmp : data) {
            if (!tmp.isBlock) {
                result = tmp;
                break;
            }
        }
        return result;
    }

    /**
     * 当前pos先入队
     * 进入循环
     * 队头出队
     * 根据当前pos拿到nextPos[]，遍历
     * 从header开始添加，每次创建新的list对象储存
     * 每次检查是否已到达边界
     * 入队当前遍历的pos
     * 添加新的全部，开始下一轮
     */
    public static List<WayData> findWay2(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }

        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;

        //广度优先遍历(同上)
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            return list2;
                        }
                    }
                }
                way.offer(direction);

            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        return null;
    }

    /**
     * 判断当前位置是否能放置树桩或小猪(如果在一个封闭的圈子里面,则连小猪当前位置也要计算)例如:(0表示树头 .表示小猪)
     * * * * * * *
     *  * 0 0 0 * *
     * * 0 . . 0 *
     *  * 0 * 0 * *
     * * * 0 0 * *
     *  * * * * * *
     * 计算出来空闲的结果是1,也即是可以放置,如果再多一个小猪在里面,则不可放置
     * @param items 格子状态
     * @param occupiedPos 小猪们的所在位置
     * @param currentPos 起点
     * @param result 空闲的格子
     * @return 圈子内能否放置
     */
    public static boolean isCurrentPositionCanSet(int[][] items, WayData[] occupiedPos, WayData currentPos, List<WayData> result) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            //复制数组(因为要对数组元素值进行修改，且不能影响原来的)
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        for (WayData tmp : occupiedPos) {
            if (tmp != null) {
                //先将小猪们占用的位置标记未未选中
                pattern[tmp.y][tmp.x] = Item.STATE_UNSELECTED;
            }
        }
        //以currentPos为起点
        way.offer(currentPos);
        //标记状态(已走过)
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        if (items[currentPos.y][currentPos.x] != Item.STATE_SELECTED) {
            //如果起点也是空闲状态，则算他一个
            result.add(currentPos);
        }
        //开始广度优先遍历
        while (!way.isEmpty()) {
            //队头出队
            WayData header = way.poll();
            //寻找周围6个方向可以到达的位置(不包括越界的,标记过的,不是空闲的)也就是空闲的格子
            List<WayData> directions = getCanArrivePos(pattern, header);
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                //将这些位置添加进去
                result.add(direction);
                way.offer(direction);
            }
        }
        int count = 0;
        //重点来了
        //现在result里面保存的位置，都是忽略了小猪的坐标的，所以现在要重新计算一下
        //遍历小猪们当前所在位置，是否在result中，如果在，记录一下
        for (WayData tmp : occupiedPos) {
            if (tmp != null && result.contains(tmp)) {
                count++;
            }
        }
        //最后，如果空闲格子内的小猪数 < 总的空闲格子数，则认为这个圈内还能放得下，反之
        return count < result.size();
    }

    /**
     广度遍历优先(同上)
     */
    public static List<WayData> findWay4(int[][] items, WayData[] ignorePositions, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        for (WayData tmp : ignorePositions) {
            if (tmp != null) {
                pattern[tmp.y][tmp.x] = Item.STATE_UNSELECTED;
            }
        }
        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            return list2;
                        }
                    }
                }
                way.offer(direction);

            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        return footprints.isEmpty() ? null : footprints.get(footprints.size() - 1);
    }

    /**
     广度遍历优先(同上) (不优选结果,保留全部能够到达边缘的走法)
     */
    public static List<WayData> findWay3(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        List<List<WayData>> result = new ArrayList<>();
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            result.add(list2);
                        }
                    }
                }
                way.offer(direction);
            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        Collections.shuffle(result);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     检查是不是在边界
     */
    public static boolean isEdge(int verticalCount, int horizontalCount, WayData direction) {
        return direction.x == 0 || direction.x == horizontalCount - 1 || direction.y == 0 || direction.y == verticalCount - 1;
    }

    /**
     检查是不是在边界
     */
    private static boolean isEdge2(int verticalCount, int horizontalCount, List<WayData> list2) {
        return list2.get(list2.size() - 1).y == 0
                || list2.get(list2.size() - 1).x == 0
                || list2.get(list2.size() - 1).y == verticalCount - 1
                || list2.get(list2.size() - 1).x == horizontalCount - 1;
    }

    /**
     检查src是否跟list中最后一个元素的位置是一样的
     */
    private static boolean canLinks(WayData src, List<WayData> list) {
        boolean isCanLinks = false;
        if (!list.isEmpty()) {
            WayData lastItem = list.get(list.size() - 1);
            if (lastItem.y == src.y && lastItem.x == src.x) {
                isCanLinks = true;
            }
        }
        return isCanLinks;
    }

    /**
     寻找周围6个方向可以到达的位置(不包括越界的,标记过的,不是空闲的)
     */
    public static List<WayData> getCanArrivePos(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        List<WayData> result = new ArrayList<>();
        int offset = currentPos.y % 2 == 0 ? 0 : 1, offset2 = currentPos.y % 2 == 0 ? 1 : 0;
        for (int i = 0; i < 6; i++) {
            WayData tmp = getNextPosition(currentPos, offset, offset2, i);
            if ((tmp.x > -1 && tmp.x < horizontalCount) && (tmp.y > -1 && tmp.y < verticalCount)) {
                if (items[tmp.y][tmp.x] != Item.STATE_SELECTED && items[tmp.y][tmp.x] != Item.STATE_OCCUPIED && items[tmp.y][tmp.x] != STATE_WALKED) {
                    result.add(tmp);
                    items[tmp.y][tmp.x] = STATE_WALKED;
                }
            }
        }
        Collections.shuffle(result);
        return result;
    }

    /**
     寻找周围6个方向可以到达的位置(不包括越界的)
     */
    private static List<WayData> getCanArrivePosUnchecked(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        List<WayData> result = new ArrayList<>();
        int offset = currentPos.y % 2 == 0 ? 0 : 1, offset2 = currentPos.y % 2 == 0 ? 1 : 0;
        for (int i = 0; i < 6; i++) {
            WayData tmp = getNextPosition(currentPos, offset, offset2, i);
            if ((tmp.x > -1 && tmp.x < horizontalCount) && (tmp.y > -1 && tmp.y < verticalCount)) {
                result.add(tmp);
                items[tmp.y][tmp.x] = STATE_WALKED;
            }
        }
        Collections.shuffle(result);
        return result;
    }

    /**
     检查是否有出路
     */
    public static boolean isHasExit(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            for (WayData tmp : directions) {
                if (isEdge(verticalCount, horizontalCount, tmp)) {
                    return true;
                }
                way.offer(tmp);
            }
        }
        return false;
    }

    public static void printItemStatus(int[][] items) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        StringBuilder s = new StringBuilder();
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            for (int horizontal = 0; horizontal < horizontalCount; horizontal++) {
                s.append(items[vertical][horizontal] == Item.STATE_SELECTED ? "▆" :
                        items[vertical][horizontal] == Item.STATE_OCCUPIED ? "0" : ".").append("  ");
            }
            LogUtil.print((vertical % 2 == 0 ? "  " : "") + s);
            s = new StringBuilder();
        }
        LogUtil.print("-\n\n-");
    }

    /**
     根据当前方向获取对应的位置
     */
    private static WayData getNextPosition(WayData currentPos, int offset, int offset2, int direction) {
        WayData result = new WayData(currentPos.x, currentPos.y);
        switch (direction) {
            case 0:
                //左
                result.x -= 1;
                break;
            case 1:
                //左上
                result.x -= offset;
                result.y -= 1;
                break;
            case 2:
                //左下
                result.x -= offset;
                result.y += 1;
                break;
            case 3:
                //右
                result.x += 1;
                break;
            case 4:
                //右上
                result.x += offset2;
                result.y -= 1;
                break;
            case 5:
                //右下
                result.x += offset2;
                result.y += 1;
                break;
        }
        return result;
    }

}