package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;



/**
 * Created by xizheye on 8/26/16.
 */
public class Solution {

    public static final String tagCod = "Cod";
    public static final String tagHaddock = "Haddock";
    public static final String tagChips = "Chips";
	public static final int orderTimeLimits = 600;

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

        FoodType type;
        String name;

        long cookTime;

        CookableObject(){
        }
    }

    abstract class Fish extends CookableObject {
        public abstract void cook();
    }

    public class Cod extends Fish {
    	public static final int cookTime = 80;
        public void cook() {
        }

        Cod () {
            this.setName("Cod");
            this.setType(FoodType.COD);
            this.setCookTime(80);
        }
    }

    public class Haddock extends Fish {
    	public static final int cookTime = 90;
        public void cook() {
        }

        Haddock () {
            this.setType(FoodType.HADDOCK);
            this.setName("Haddock");
            this.setCookTime(90);
        }
    }

    public class Chips extends CookableObject {
    	public static final int cookTime = 120;
    	

		public void cook() {
        }

        Chips () {
            this.setType(FoodType.CHIPS);
            this.setName("Chip");
            this.setCookTime(120);
        }

    }


    public class Order {
    	
        HashMap<FryerType, ArrayList<SubOrder>> itemList = new HashMap<FryerType, ArrayList<SubOrder>>();
        OrderStatus status;
        TimeStamp orderTime;
        TimeStamp startTime;


		TimeStamp serveTime;
        int duration;
        int orderNo;
        
        int itemCnt = 0;
    	int chipDuration = 0, fishDuration = 0;
        boolean isServed = false;

        public synchronized void decreItemCnt() {
        	this.itemCnt--;
        }
        
        public synchronized int getItemCnt() {
        	return this.itemCnt;
        }
        
        public int getOrderNo() {
			return orderNo;
		}

		public void setOrderNo(int orderNo) {
			this.orderNo = orderNo;
		}


		public Order (int orderNo, TimeStamp orderTime) {
			this.orderNo = orderNo;
			this.orderTime = orderTime;
		}
		
		public Order (int orderNo, TimeStamp orderTime, TimeStamp startTime, HashMap<String, Integer> subOrderMap) {
        	
        	this.orderNo = orderNo;
        	this.orderTime = orderTime;
        	this.startTime = startTime;
        	createSubOrders(subOrderMap);
        	calcDuaration();
        	calcServeTime(duration);
			adjustStartTime();
        	ArrayList<SubOrder> subOrderChipsList = this.itemList.containsKey(FryerType.CHIPSFRYER) ? this.itemList.get(FryerType.CHIPSFRYER) : new ArrayList<SubOrder>();
        	ArrayList<SubOrder> subOrderFishsList = this.itemList.containsKey(FryerType.FISHFRYER) ? this.itemList.get(FryerType.FISHFRYER) : new ArrayList<SubOrder>();
			this.itemCnt = subOrderChipsList.size() + subOrderFishsList.size();
        }
        

        public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public TimeStamp getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(TimeStamp orderTime) {
            this.orderTime = orderTime;
        }

        public TimeStamp getStartTime() {
			return startTime;
		}


		public void setStartTime(TimeStamp startTime) {
			this.startTime = startTime;
		}
		
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

        public TimeStamp getServeTime() {
        	return this.serveTime;
        }
        

        private void createFishSubOrders(HashMap<String, Integer> subOrderMap) {
            int codNum = subOrderMap.containsKey(Solution.tagCod) ? subOrderMap.get(Solution.tagCod) : 0;
            TimeStamp fishStamp = new TimeStamp(startTime != null ? startTime : orderTime);
            int prevDuration = 0;
            if (codNum > 0) {
                while (codNum >= FishFryer.totalStoveNum) {
                    fishStamp.increment(prevDuration);
                    SubOrder tmpSub = constructSubOrder(FryerType.FISHFRYER, FishFryer.totalStoveNum, FoodType.COD, fishStamp, 1);
                    if (this.itemList.containsKey(FryerType.FISHFRYER)) {
                        this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                    }
                    else {
                        ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                        arr.add(tmpSub);
                        this.itemList.put(FryerType.FISHFRYER, arr);
                    }
                    codNum -= FishFryer.totalStoveNum;
                    prevDuration = Cod.cookTime;
                }
            }

            int haddockNum = subOrderMap.containsKey(Solution.tagHaddock) ? subOrderMap.get(Solution.tagHaddock) : 0;
            if (haddockNum > 0) {
                while (haddockNum >= FishFryer.totalStoveNum) {
                    fishStamp.increment(prevDuration);
                    SubOrder tmpSub = constructSubOrder(FryerType.FISHFRYER, FishFryer.totalStoveNum, FoodType.HADDOCK, fishStamp, 1);
                    if (this.itemList.containsKey(FryerType.FISHFRYER)) {
                        this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                    }
                    else {
                        ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                        arr.add(tmpSub);
                        this.itemList.put(FryerType.FISHFRYER, arr);
                    }

                    haddockNum -= FishFryer.totalStoveNum;
                    prevDuration = Haddock.cookTime;
                }
            }

            if (codNum + haddockNum > FishFryer.totalStoveNum) {
                fishStamp.increment(prevDuration);
                SubOrder tmpSub = new SubOrder(this, fishStamp, FryerType.FISHFRYER);
                appendSubOrder(codNum, tmpSub, FoodType.COD);
                appendSubOrder(FishFryer.totalStoveNum - codNum, tmpSub, FoodType.HADDOCK);
                if (this.itemList.containsKey(FryerType.FISHFRYER)) {
                    this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                }
                else {
                    ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                    arr.add(tmpSub);
                    this.itemList.put(FryerType.FISHFRYER, arr);
                }
                haddockNum -= FishFryer.totalStoveNum - codNum;
                codNum = 0;

                this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                prevDuration = tmpSub.getDuration();
            }

            if (codNum > 0 || haddockNum > 0) {
                fishStamp.increment(prevDuration);
                SubOrder tmpSub = new SubOrder(this, fishStamp, FryerType.FISHFRYER);
                appendSubOrder(codNum, tmpSub, FoodType.COD);
                appendSubOrder(haddockNum, tmpSub, FoodType.HADDOCK);

                if (this.itemList.containsKey(FryerType.FISHFRYER)) {
                    this.itemList.get(FryerType.FISHFRYER).add(tmpSub);
                }
                else {
                    ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                    arr.add(tmpSub);
                    this.itemList.put(FryerType.FISHFRYER, arr);
                }
                codNum = 0;
                haddockNum = 0;
            }
        }

        private void createChipsSubOrders(HashMap<String, Integer> subOrderMap) {
            int prevDuration = 0;
            TimeStamp chipsStamp = new TimeStamp(startTime != null ? startTime : orderTime);
            int chipsNum = subOrderMap.containsKey(Solution.tagChips) ? subOrderMap.get(Solution.tagChips) : 0;
            while (chipsNum >= ChipFryer.totalPortionNum) {
                chipsStamp.increment(prevDuration);
                SubOrder tmpSub = constructSubOrder(FryerType.CHIPSFRYER, 1, FoodType.CHIPS, chipsStamp, ChipFryer.totalPortionNum);
                chipsNum -= ChipFryer.totalPortionNum;
                if (this.itemList.containsKey(FryerType.CHIPSFRYER)) {
                    this.itemList.get(FryerType.CHIPSFRYER).add(tmpSub);
                }
                else {
                    ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                    arr.add(tmpSub);
                    this.itemList.put(FryerType.CHIPSFRYER, arr);
                }
                prevDuration = Chips.cookTime;
            }

            if (chipsNum > 0) {
                chipsStamp.increment(prevDuration);
                SubOrder tmpSub = constructSubOrder(FryerType.CHIPSFRYER, 1, FoodType.CHIPS, chipsStamp, chipsNum);
                if (this.itemList.containsKey(FryerType.CHIPSFRYER)) {
                    this.itemList.get(FryerType.CHIPSFRYER).add(tmpSub);
                }
                else {
                    ArrayList<SubOrder> arr = new ArrayList<SubOrder>();
                    arr.add(tmpSub);
                    this.itemList.put(FryerType.CHIPSFRYER, arr);
                }
                chipsNum = 0;
            }
        }


        private void createSubOrders(HashMap<String, Integer> subOrderMap) {
            createFishSubOrders(subOrderMap);
            createChipsSubOrders(subOrderMap);
        }

        SubOrder constructSubOrder(FryerType fryerType, int totalSum, FoodType foodType, TimeStamp timeStamp, int itemNum) {
            SubOrder tmpSub = new SubOrder(this, timeStamp, fryerType);
            ArrayList<CookableObject> objs = new ArrayList<CookableObject>();
            for (int i = 0; i < totalSum; i++) {
                if (foodType == FoodType.HADDOCK) {
                    CookableObject obj = new Haddock();
                    objs.add(obj);
                }
                else if (foodType == FoodType.COD) {
                    CookableObject obj = new Cod();
                    objs.add(obj);
                }
                else if (foodType == FoodType.CHIPS) {
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
        
        void calcDuaration() {
        	ArrayList<SubOrder> subOrderChipsList = this.itemList.containsKey(FryerType.CHIPSFRYER) ? this.itemList.get(FryerType.CHIPSFRYER) : new ArrayList<SubOrder>();
        	ArrayList<SubOrder> subOrderFishsList = this.itemList.containsKey(FryerType.FISHFRYER) ? this.itemList.get(FryerType.FISHFRYER) : new ArrayList<SubOrder>();
        	for (SubOrder subOrder : subOrderChipsList) {
        		chipDuration += subOrder.getDuration();
        	}
        	
        	for (SubOrder subOrder : subOrderFishsList) {
        		fishDuration += subOrder.getDuration();
        	}
        	
        	this.duration = Math.max(chipDuration, fishDuration);
        }
        
        void calcServeTime(int duration) {
			this.serveTime = new TimeStamp(startTime == null ? orderTime : startTime);
			this.serveTime.increment(duration);        	
        }
        
        void adjustStartTime() {
        	FryerType adjustFryType = (this.chipDuration != this.duration ? FryerType.CHIPSFRYER : FryerType.FISHFRYER);
        	int increment = (adjustFryType == FryerType.CHIPSFRYER ? this.duration - this.chipDuration : this.duration - this.fishDuration );
        	
        	if (this.getItemList().containsKey(adjustFryType)) {
        		ArrayList<SubOrder> subArr = this.getItemList().get(adjustFryType);
	        	for (int i = 0; i < subArr.size(); i++) {
	        		if (i == 0) {
	        			subArr.get(i).delayTime = increment;
	        		}
	        		subArr.get(i).beginTime.increment(increment);
	        	}
        	}
        }
    }

    public class SubOrder {
        ArrayList<CookableObject> subItems = new ArrayList<CookableObject>();
        OrderStatus status;
        TimeStamp beginTime;
        Order order;
        FryerType fryerType;
        int delayTime = 0;
        
        public SubOrder(Order order, TimeStamp beginTime, FryerType fryerType) {
        	this.order = order;
        	this.beginTime = new TimeStamp(beginTime.hours, beginTime.minutes, beginTime.seconds);
        	this.fryerType = fryerType;
        }
        
        public TimeStamp getBeginTime() {
			return beginTime;
		}

		public void setBeginTime(TimeStamp beginTime) {
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
        	String output = new String();
        	if (this.fryerType == FryerType.CHIPSFRYER) {
        		int size = this.subItems.size();
        		if (size > 0) {
        			output = "at " + this.beginTime.toString() + ", " + "Begin Cooking " + 
        		String.valueOf(this.subItems.get(0).getNums()) + " " + tagChips;
        		}
        	}
        	else {
        		int codCnt = 0, haddockCnt = 0;
        		for (CookableObject obj : this.getSubItems()) {
        			if (obj instanceof Cod) {
        				codCnt ++;
        			}
        			else if (obj instanceof Haddock) {
        				haddockCnt ++;
        			}
        		}
        		
        		if (codCnt > 0 && haddockCnt > 0) {
            		output = "at " + this.beginTime.toString() + ", " + "Begin Cooking " + 
                    		String.valueOf(codCnt) + " " + tagCod + " " +
            				String.valueOf(haddockCnt) + " " + tagHaddock;
        		}
        		else if (codCnt > 0) {
        			output = "at " + this.beginTime.toString() + ", " + "Begin Cooking " + 
                    		String.valueOf(codCnt) + " " + tagCod;
        		}
        		else if (haddockCnt > 0) {
        			output = "at " + this.beginTime.toString() + ", " + "Begin Cooking " + 
                    		String.valueOf(haddockCnt) + " " + tagHaddock;
        		}
        	}
        	
        	return output;
        	
        }
        
        public int getDuration() {
        	boolean hasHaddock = false;
        	for (CookableObject obj : this.subItems) {
        		if (fryerType == FryerType.FISHFRYER) {
        			if (obj instanceof Haddock) {
            			hasHaddock = true;
        			}
        		}	
        	}
        	
        	if (fryerType == FryerType.FISHFRYER) {
            	return hasHaddock ? Haddock.cookTime : Cod.cookTime;
        	}
        	else {
        		return Chips.cookTime;
        	}
        }
    }

    public abstract class Fryer implements Runnable{
        public boolean isRunning;
        Queue<SubOrder> orderList = new LinkedList<SubOrder>();
        public Object lck = new Object();

        Fryer() {
            this.isRunning = true;
        }

        public abstract void fry(ArrayList<CookableObject> itemList);
        
        public void addSubOrder(ArrayList<SubOrder> subOrders) {
        	for (SubOrder order : subOrders) {
        		this.orderList.add(order);
        	}
        	
        	synchronized (lck) {
            	lck.notify();
			}
        } 
        
        public void stop() {
            this.isRunning = false;
            synchronized (lck) {
                this.lck.notify();
            }
        }

        
        abstract boolean canProcessOrder(int size);
        
        @Override
        public void run() {
            while (this.isRunning) {

                synchronized (lck) {
                    try {
                        lck.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (this.orderList.size() > 0) {
                    SubOrder subOrder = orderList.poll();

                    if (subOrder != null && canProcessOrder(subOrder.getSubItems().size())) {
                    	try {
                    		Thread.sleep(subOrder.delayTime);
                            System.out.println(subOrder.toString());
                            fry(subOrder.getSubItems());
                            subOrder.order.decreItemCnt();
                    	
							Thread.sleep(subOrder.getDuration());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	                	synchronized (subOrder.order) {
	                		if (subOrder.order.getItemCnt() == 0) {
	                        	subOrder.order.notify();
							}
	                    }
                    }
                }
            }

        }
    }

    public class FishFryer extends Fryer{
        public static final int totalStoveNum = 4;
        int curAvailableStoveNum = 4;

        FishFryer () {
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

        public void fry(ArrayList<CookableObject> itemList) {
            if (canProcessOrder(itemList.size())) {

                for (CookableObject f: itemList) {
                    f.cook();
                }
            }
        }

    }

    public class ChipFryer extends Fryer {
        public static final int totalPortionNum = 4;
        int curAvailablePortionNum = 4;

        boolean isRunning;
        ChipFryer () {
            this.isRunning = true;

        }

        public synchronized int getCurAvailableStoveNum() {
            return curAvailablePortionNum;
        }

        public synchronized void setCurAvailableStoveNum(int curAvailableStoveNum) {
            this.curAvailablePortionNum = curAvailableStoveNum;
        }
        
        public synchronized boolean isInUse() {
            return curAvailablePortionNum == 0;
        }

        public synchronized void setInUse(boolean inUse) {
            if (inUse) {
            	curAvailablePortionNum = 0;
            }
            else {
            	curAvailablePortionNum = totalPortionNum;
            }
        }

        public synchronized boolean isRunning() {
            return isRunning;
        }

        public synchronized void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void fry(ArrayList<CookableObject> itemList) {
            setInUse(true);

            for (CookableObject obj : itemList) {
                obj.cook();
            }
            setInUse(false);
        }

		boolean canProcessOrder(int size) {
			return this.curAvailablePortionNum >= size;
		}

    }


    public class Processor implements Runnable{
        ChipFryer chipFryer;
        FishFryer fishFryer;

        Queue<Order> orderQueue;
        boolean isRunning;
        Thread chipThread;
        Thread fishThread;

        Order processingOrder;
        
        Queue<String> orderStrList;
        
        OrderParser orderParser;
        
        Processor () {
            chipFryer = new ChipFryer();
            fishFryer = new FishFryer();
            chipThread = new Thread(chipFryer);
            fishThread = new Thread(fishFryer);

            orderQueue = new LinkedList<Order>();
            orderStrList = new LinkedList<String>();
            orderParser = new OrderParser();
        }

        void start() {
            isRunning = true;
            processingOrder = null;
            chipThread.start();
            fishThread.start();
        }
        
        void stop() {
        	isRunning = false;
        	try {
        		chipFryer.stop();
        		fishFryer.stop();
				chipThread.join();
	        	fishThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        

        void addQueue(String rawStr) {
        	orderStrList.add(rawStr);
        }
        
		@Override
		public void run() {
            while (isRunning) {
            	
                while (orderStrList.size() > 0) {
                    String curOrderStr = orderStrList.peek();
                    
                    Order curOrder = orderParser.parser(curOrderStr, processingOrder); 
                    
                    if (processingOrder != null) {
	                    synchronized (processingOrder) {
	                    	while (processingOrder != null && processingOrder.getItemCnt() > 0) {
	                    		try {
	    							processingOrder.wait();
	    						} catch (InterruptedException e) {
	    							e.printStackTrace();
	    						}
	                    		
	                    	}		
						}
                    }

                	
                	if (processingOrder != null) {
                        if (!processingOrder.isServed) {
                            processingOrder.isServed = true;
                            System.out.println("at " + processingOrder.getServeTime().toString() + ", Serve Order #" + String.valueOf(processingOrder.orderNo));
                        }
                    	if (shouldRejected(curOrder, processingOrder)) {
                        	System.out.println("at " + curOrder.getOrderTime().toString() + ", " + 
                        "Order #" + String.valueOf(curOrder.getOrderNo()) +" rejected");
                        	
                        	orderStrList.poll();
                        	continue;
                        } 
                	}

                	processingOrder = curOrder;
                	processingOrder.setStatus(OrderStatus.PROCESSING);
                	
                	System.out.println("at " + curOrder.getOrderTime().toString() + ", Order #" + String.valueOf(curOrder.orderNo) + " Accepted");
                	
                	ArrayList<SubOrder> chipsOrders = processingOrder.getItemList().containsKey(FryerType.CHIPSFRYER) ? processingOrder.getItemList().get(FryerType.CHIPSFRYER) : new ArrayList<SubOrder>();
                	ArrayList<SubOrder> fishOrders = processingOrder.getItemList().containsKey(FryerType.FISHFRYER) ? processingOrder.getItemList().get(FryerType.FISHFRYER) : new ArrayList<SubOrder>();

                	if (chipsOrders.size() > 0) {
                		this.chipFryer.addSubOrder(chipsOrders);
                	}
                	if (fishOrders.size() > 0) {
                		this.fishFryer.addSubOrder(fishOrders);
                	}
                	
                	orderStrList.poll();
                }
                
            }			
		}
		
		boolean shouldRejected(Order curOrder, Order processingOrder) {
			return (curOrder.getServeTime().toValue() - curOrder.getOrderTime().toValue()) > orderTimeLimits;
		}
    }
    
    class OrderParser {
    	public Order parser(String rawInputStr, Order processingOrder) {
    		// Order #1, 12:00:00, 2 Cod, 4 Haddock, 3 Chips

    		// parse orderNo
    		String[] strlist = rawInputStr.split(",");
    		char orderCh = strlist[0].charAt(strlist[0].indexOf('#')+1);
    		int orderNo = Character.getNumericValue(orderCh);
    		
    		// parse time
    		strlist[1] = strlist[1].trim();
    		TimeStamp timeStamp = new TimeStamp();
    		timeStamp.StringToTimestamp(strlist[1]);
    		
    		// parse food
    		HashMap<String, Integer> subOrderMap = new HashMap<String, Integer>();
    		
    		for (int i = 2; i < strlist.length; i++) {
    			strlist[i] = strlist[i].trim();
    			String[] tmplist = strlist[i].split(" ");
    			String foodStr = tmplist[1];
    			int cnt = Integer.parseInt(tmplist[0]);
    			subOrderMap.put(foodStr, cnt);
    		}
    		
    		TimeStamp serveTime = processingOrder != null ? processingOrder.getServeTime() : timeStamp;
    		Order order = new Order(orderNo, timeStamp, serveTime, subOrderMap);
    		return order;
    	}
    	
    }
    
    class TimeStamp {
    	int hours = 0;
    	int minutes = 0;
    	int seconds = 0;
    	
    	public TimeStamp() {
		}
    	
    	public TimeStamp(int hours, int minutes, int seconds) {
    		this.hours = hours;
    		this.minutes = minutes;
    		this.seconds = seconds;
    	}
    	
    	public TimeStamp(TimeStamp stmp) {
    		this.hours = stmp.hours;
    		this.minutes = stmp.minutes;
    		this.seconds = stmp.seconds;
    	}
    	
    	@Override
    	public String toString() {
    		String hoursStr = (this.hours < 10 ? "0" : "") + Integer.toString(this.hours);
    		String minuteStr = (this.minutes < 10 ? "0" : "") + Integer.toString(this.minutes);
    		String secondsStr = (this.seconds < 10 ? "0" : "") + Integer.toString(this.seconds);
    		return hoursStr + ":" + minuteStr + ":" + secondsStr; 
    	}
    	
    	public long toValue() {
    		return 3600*hours + 60*minutes + seconds;
    	}
    	
    	public void StringToTimestamp(String date) {
    		String[] timelist = date.split(":");
    		this.hours = Integer.parseInt(timelist[0]);
    		this.minutes = Integer.parseInt(timelist[1]);
    		this.seconds = Integer.parseInt(timelist[2]);
   	
    	}
    	
    	public void increment(int secs) {
    		int carry = (secs + this.seconds)/60;
    		this.seconds = (secs + this.seconds)%60;
    		
    		int newMin = carry + this.minutes;
    		this.minutes = (newMin)%60;
    		carry = (newMin)/60;
    		
    		this.hours = carry + this.hours;
    		if (this.hours > 23) {
    			this.hours = 0;
    		}
    	}
    }
    
    public static void main(String args[] ) throws Exception {
    	String order1 = "Order #1, 12:00:00, 2 Cod, 4 Haddock, 3 Chips";
    	String order2 = "Order #2, 12:00:30, 1 Haddock, 1 Chips";
    	String order3 = "Order #3, 12:01:00, 21 Chips";
    	String order4 = "Order #1, 12:00:00, 8 Chips";
    	String order5 = "Order #2, 12:00:00, 8 Chips";
    	String order6 = "Order #3, 12:00:00, 8 Chips";
    	String order7 = "Order #4, 12:00:00, 8 Chips";
    	String order8 = "Order #5, 12:02:00, 8 Chips";
    	String order9 = "Order #6, 12:02:00, 8 Chips";
    	String order10 = "Order #7, 12:08:00, 8 Chips";
    	String order11 = "Order #8, 12:08:00, 8 Chips";



    	List<String> strList = new ArrayList<String>();
//        strList.add(order1);
//        strList.add(order2);
//        strList.add(order3);
    	strList.add(order4);
    	strList.add(order5);
    	strList.add(order6);
    	strList.add(order7);
    	strList.add(order8);
    	strList.add(order9);
    	strList.add(order10);
    	strList.add(order11);

    	Solution solut = new Solution();
    	Processor processor = solut.new Processor();
    	Thread t = new Thread(processor);
    	t.start();
    	processor.start();

    	for (String s : strList) {
    		processor.addQueue(s);
    	}
    	
//    	Solution solut = new Solution();
//    	Processor processor = solut.new Processor();
//
//    	Thread t = new Thread(processor);
//    	t.start();
//        processor.start();
//
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        String s;
//        while ((s = reader.readLine()) != null) {
//            processor.addQueue(s);
//        }
    	
    	Thread.sleep(10000);
    	processor.stop();
    	t.join();
    	
    	
    }
    
}
