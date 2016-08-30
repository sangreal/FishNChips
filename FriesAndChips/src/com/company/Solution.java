package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xizheye on 8/26/16.
 */
public class Solution {

    public static final String tagCod = "Cod";
    public static final String tagHaddock = "Haddock";
    public static final String tagChips = "Chips";

    public Solution() {}

    public enum FoodType {
        COD(0),
        HADDOCK(1),
        CHIPS(2);

        private final int value;
        public int value() {
            return value;
        }

        FoodType(int i) {
            this.value = i;
        }
    }

    public enum FoodStatus {
        RAW(0),
        PREPARED(1),
        COOKED(2);

        private final int value;
        public int value() {
            return value;
        }

        FoodStatus(int i) {
            this.value = i;
        }
    }

    public enum OrderStatus {
        PENDING(0),
        PROCESSING(1),
        DONE(2);

        private final int value;
        public int value() {
            return value;
        }

        OrderStatus(int i) {
            this.value = i;
        }
    }

    public enum FryerType {
        FISHFRYER(0),
        CHIPSFRYER(1);

        private final int value;
        public int value() {
            return value;
        }

        FryerType(int i) {
            this.value = i;
        }
    }

    public abstract class CookableObject {
        public abstract void cook();

        public FoodType getType() {
            return type;
        }

        public void setType(FoodType type) {
            this.type = type;
        }

        public FoodStatus getStatus() {
            return status;
        }

        public void setStatus(FoodStatus status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCookTime() {
            return cookTime;
        }

        public void setCookTime(long cookTime) {
            this.cookTime = cookTime;
        }

        public int getNums() {
            return nums;
        }

        public void setNums(int nums) {
            this.nums = nums;
        }

        int nums;

        public FoodStatus status;

        FoodType type;
        String name;

        long cookTime;

        CookableObject(){
            status = FoodStatus.PREPARED;
        }
    }

    abstract class Fish extends CookableObject {
        public abstract void cook();
    }

    public class Cod extends Fish {
        public void cook() {
            System.out.println("Cod is being cooked....");
        }

        Cod () {
            this.setName("Cod");
            this.setStatus(FoodStatus.PREPARED);
            this.setType(FoodType.COD);
            this.setCookTime(80);
        }
    }

    public class Haddock extends Fish {
        public void cook() {
            System.out.println("Haddock is being cooked....");
        }

        Haddock () {
            this.setType(FoodType.HADDOCK);
            this.setName("Haddock");
            this.setStatus(FoodStatus.PREPARED);
            this.setCookTime(90);
        }
    }

    public class Chips extends CookableObject {
        public void cook() {
            System.out.println("Chips begin to be fried....");
            try {
                Thread.sleep(this.getCookTime()*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Chips fried completed");
        }

        Chips () {
            this.setType(FoodType.CHIPS);
            this.setName("Chip");
            this.setStatus(FoodStatus.PREPARED);
            this.setCookTime(120);
        }

    }


    public class Order {
        HashMap<FryerType, ArrayList<SubOrder>> itemList;
        OrderStatus status;

        public long getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(long orderTime) {
            this.orderTime = orderTime;
        }

        long orderTime;

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }

        public HashMap<FryerType, ArrayList<SubOrder>> getItemList() {
            return itemList;
        }

        public void setItemList(HashMap<FryerType, ArrayList<SubOrder>> itemList) {
            this.itemList = itemList;
        }

        private void createSubOrders(HashMap<String, Integer> subOrderMap) {

            int codNum = subOrderMap.get(Solution.tagCod);
            if (codNum > 0) {
                while (codNum >= FishFryer.totalStoveNum) {
                    SubOrder tmpSub = constructSubOrder(FishFryer.totalStoveNum, FoodType.COD, 1);
                    this.itemList.get(FryerType.FISHFRYER).add(tmpSub);

                    codNum -= FishFryer.totalStoveNum;
                }
            }

            int haddockNum = subOrderMap.get(Solution.tagHaddock);
            if (haddockNum > 0) {
                while (haddockNum >= FishFryer.totalStoveNum) {
                    SubOrder tmpSub = constructSubOrder(FishFryer.totalStoveNum, FoodType.HADDOCK, 1);
                    this.itemList.get(FryerType.FISHFRYER).add(tmpSub);

                    haddockNum -= FishFryer.totalStoveNum;
                }
            }

            if (codNum + haddockNum > FishFryer.totalStoveNum) {
                SubOrder tmpSub = new SubOrder();
                appendSubOrder(codNum, tmpSub, FoodType.COD);
                appendSubOrder(FishFryer.totalStoveNum - codNum, tmpSub, FoodType.HADDOCK);
                this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                haddockNum -= FishFryer.totalStoveNum - codNum;
                codNum = 0;

                this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
            }

            if (codNum > 0 || haddockNum > 0) {
                SubOrder tmpSub = new SubOrder();
                appendSubOrder(codNum, tmpSub, FoodType.COD);
                appendSubOrder(haddockNum, tmpSub, FoodType.HADDOCK);

                this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                codNum = 0;
                haddockNum = 0;
            }


            int chipsNum = subOrderMap.get(Solution.tagChips);
            while (chipsNum >= ChipFryer.totalPortionNum) {
                SubOrder subOrder = constructSubOrder(1, FoodType.CHIPS, ChipFryer.totalPortionNum);
                chipsNum -= ChipFryer.totalPortionNum;
                this.itemList.get(FryerType.CHIPSFRYER).add(subOrder);
            }

            if (chipsNum > 0) {
                SubOrder subOrder = constructSubOrder(1, FoodType.CHIPS, chipsNum);
                this.itemList.get(FryerType.CHIPSFRYER).add(subOrder);
                chipsNum = 0;
            }

        }

        SubOrder constructSubOrder(int totalSum, FoodType type, int itemNum) {
            SubOrder tmpSub = new SubOrder();
            ArrayList<CookableObject> objs = new ArrayList<CookableObject>();
            for (int i = 0; i < totalSum; i++) {
                if (type == FoodType.HADDOCK) {
                    CookableObject obj = new Haddock();
                    objs.add(obj);
                }
                else if (type == FoodType.COD) {
                    CookableObject obj = new Cod();
                    objs.add(obj);
                }
                else if (type == FoodType.CHIPS) {
                    CookableObject obj = new Chips();
                    obj.setNums(itemNum);
                    objs.add(obj);
                }
            }
            tmpSub.setSubItems(objs);

            return tmpSub;
        }

        void appendSubOrder(int totalSum, SubOrder subOrder, FoodType type) {
            ArrayList<CookableObject> objs = subOrder.getSubItems();
            for (int i = 0; i < totalSum; i++) {
                if (type == FoodType.HADDOCK) {
                    CookableObject obj = new Haddock();
                    objs.add(obj);
                }
                else if (type == FoodType.COD) {
                    CookableObject obj = new Cod();
                    objs.add(obj);
                }
                else if (type == FoodType.CHIPS) {
                    CookableObject obj = new Chips();
                    objs.add(obj);
                }
            }

            subOrder.setSubItems(objs);
        }
    }

    public class SubOrder {
        ArrayList<CookableObject> subItems;
        OrderStatus status;
        long beginTime;
        
        
        public long getBeginTime() {
			return beginTime;
		}

		public void setBeginTime(long beginTime) {
			this.beginTime = beginTime;
		}

		public ArrayList<CookableObject> getSubItems() {
            return subItems;
        }

        public void setSubItems(ArrayList<CookableObject> subItems) {
            this.subItems = subItems;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }

        @Override
        public String toString() {
        	
        }
    }

    abstract class Fryer {
        public boolean isRunning;

        public void start() {
            this.isRunning = true;
        }

        Fryer() {
        	this.isRunning = true;
        }
        
        public abstract void fry(ArrayList<CookableObject> itemList);

        public void stop() {
            this.isRunning = false;
        }
    }

    class FishFryer extends Fryer implements Runnable{
        public static final int totalStoveNum = 4;
        int curAvailableStoveNum = 4;

        ExecutorService service;
        Queue<SubOrder> orderList;

        FishFryer () {
            service = Executors.newFixedThreadPool(totalStoveNum);
        }

        public synchronized int getCurAvailableStoveNum() {
            return curAvailableStoveNum;
        }

        public synchronized void setCurAvailableStoveNum(int curAvailableStoveNum) {
            this.curAvailableStoveNum = curAvailableStoveNum;
        }

        boolean canProcessOrder(int size) {
            return curAvailableStoveNum >= size;
        }

        @Override
        public void run() {
            while (this.isRunning) {
                while (orderList.size() > 0) {
                    SubOrder order = orderList.poll();
                    System.out.println("Begin to Cook");
                    fry(order.getSubItems());
                }
            }
        }

        public void fry(ArrayList<CookableObject> itemList) {
            if (canProcessOrder(itemList.size())) {

                for (CookableObject f: itemList
                     ) {
                    service.execute(new FishWorkerThread(f));
                }
            }
        }


        class FishWorkerThread extends WorkerThread {

            private CookableObject cookObj;
            public FishWorkerThread(CookableObject object){
                super(object);
            }

            @Override
            public void processCommand() {
                setCurAvailableStoveNum(curAvailableStoveNum-1);
                cookObj.cook();
                setCurAvailableStoveNum(curAvailableStoveNum+1);
            }
        }
    }

    public class ChipFryer extends Fryer implements Runnable {
        public static final int totalPortionNum = 4;

        boolean isInUse;
        boolean isRunning;
        ChipFryer () {
            this.isInUse = false;
            this.isRunning = true;

        }

        Queue<SubOrder> chipQueue;

        public synchronized boolean isInUse() {
            return isInUse;
        }

        public synchronized void setInUse(boolean inUse) {
            isInUse = inUse;
        }

        public synchronized boolean isRunning() {
            return isRunning;
        }

        public synchronized void setRunning(boolean running) {
            isRunning = running;
        }

        void fry(SubOrder order) {

            setInUse(true);
            for (int i = 0; i < order.getSubItems().size(); ++i) {
                order.getSubItems().get(i).cook();
            }
            setInUse(false);
        }

        @Override
        public void fry(ArrayList<CookableObject> itemList) {
            setInUse(true);
            for (int i = 0; i < itemList.size(); ++i) {
                Chips chip = (Chips) itemList.get(i);
                System.out.println("Begin Cooking " + chip.getNums() + " Chips");
                itemList.get(i).cook();
            }
            setInUse(false);
        }

        @Override
        public void run() {
            while (this.isRunning) {
                if (!this.isInUse && chipQueue.size() > 0) {
                    SubOrder order = chipQueue.poll();
                    fry(order.getSubItems());
                }
            }
        }

    }

    class WorkerThread implements Runnable {

        private CookableObject cookObj;
        public WorkerThread(CookableObject object){
            this.cookObj = object;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+"  "+ cookObj.getName());
            processCommand();
            System.out.println(Thread.currentThread().getName()+" End.");
        }

        public void processCommand() {}
    }


    class Processor {
        ChipFryer chipFryer;
        FishFryer fishFryer;

        Queue<Order> orderQueue;

        boolean isRunning;

        Processor () {
            chipFryer = new ChipFryer();
            fishFryer = new FishFryer();

            orderQueue = new LinkedList<Order>();
            isRunning = true;
        }

        public void execute (Order order) {
            while (isRunning) {
                while (orderQueue.size() > 0) {
                    Order curOrder = orderQueue.poll();
                }
            }
        }
    }
}
