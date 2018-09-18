package com.github.wycm;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class QingKeOfGeetest {
    private static String BASE_PATH = "";
    //开始遍历处距离左边的距离
    private static final int GEETEST_WIDTH_START_POSTION = 57;

    private static ChromeDriver driver = null;
    //文档截图后图片大小
    private static Point imageFullScreenSize = null;
    //html 大小
    private static Point htmlFullScreenSize = null;
    static {
    	System.setProperty("webdriver.chrome.driver", "D:\\Google\\Chrome\\chromedriver.exe");
        driver = new ChromeDriver();
    }
    //chrome://settings/content/cookies
    public static void main(String[] args) {
        for(int i = 0; i < 20000; i++){
            try {
                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
                driver.get("http://gytv.qingk.cn/activity-vote_item-info/urturovwqecfsbtwbspdeqepbvvvqrwb/caea8e03cd5c42eeb4e2fadc3578bc48/ba1fb321bc204f8cb98c7ba4053612d0/075336fb36d04f00b76a7aa5b96e05c9?from=timeline&isappinstalled=0");
                Thread.sleep(2 * 1000);
                driver.findElement(By.className("voteButton")).click();
                Thread.sleep(3 * 1000);
                Actions actions = new Actions(driver);
                BufferedImage image = null;
                int n = 0;
                do {
                	try {
                    	image = getImageEle(driver.findElement(By.className("geetest_canvas_slice")));
                    	if(image==null) break;
                        ImageIO.write(image, "png",  new File(BASE_PATH + "slider.png"));
                        //设置原图可见
                        driver.executeScript("document.getElementsByClassName(\"geetest_canvas_fullbg\")[0].setAttribute('style', 'display: block')\n");
                        //图二
                        image = getImageEle(driver.findElement(By.className("geetest_canvas_slice")));
                        ImageIO.write(image, "png",  new File(BASE_PATH + "original.png"));
                        //隐藏原图
                        driver.executeScript("document.getElementsByClassName(\"geetest_canvas_fullbg\")[0].setAttribute('style', 'display: none')\n");

                        WebElement element = driver.findElement(By.className("geetest_slider_button"));
                        actions.clickAndHold(element).perform();
                        int moveDistance = calcMoveDistince();
                        List<MoveEntity> list = getMoveEntity(moveDistance);
                        for(MoveEntity moveEntity : list){
                            actions.moveByOffset(moveEntity.getX(), moveEntity.getY()).perform();
                            Thread.sleep(moveEntity.getSleepTime());
                        }
                        actions.release(element).perform();
                    	n++;
					} catch (Exception e) {
						break;
					}
                    Thread.sleep(1000);
				} while (n<5);
                Thread.sleep(3 * 1000);
                System.out.println("刷票:-----------------------"+(i+1)+"次");
                driver.getSessionStorage().clear();
                driver.getLocalStorage().clear();
            } catch (Exception e) {
            }
        }
        driver.quit();
    }
    private static BufferedImage getImageEle(WebElement ele) {
        try {
            byte[] fullPage = driver.getScreenshotAs(OutputType.BYTES);
            BufferedImage fullImg = ImageIO.read(new ByteArrayInputStream(fullPage));
            if (imageFullScreenSize == null){
                imageFullScreenSize = new Point(fullImg.getWidth(), fullImg.getHeight());
            }
            WebElement element = driver.findElement(By.tagName("body"));

            if(htmlFullScreenSize == null){
                htmlFullScreenSize = new Point(element.getSize().getWidth(), element.getSize().getHeight());
            }
            Point point = ele.getLocation();
            int eleWidth = (int)(ele.getSize().getWidth() / (float)element.getSize().width * (float)fullImg.getWidth());
            int eleHeight = (int) (ele.getSize().getHeight() / (float)element.getSize().height * (float)fullImg.getHeight());
            BufferedImage eleScreenshot = fullImg.getSubimage((int)(point.getX() / (float)element.getSize().width * (float)fullImg.getWidth()), (int)(point.getY() / (float)element.getSize().height * (float)fullImg.getHeight()), eleWidth, eleHeight);
            return eleScreenshot;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<MoveEntity> getMoveEntity(int distance){
        List<MoveEntity> list = new ArrayList<>();
        int i = 0;
        do {
            MoveEntity moveEntity = new MoveEntity();
            int r = RandomUtils.nextInt(5, 8);
            moveEntity.setX(r);
            moveEntity.setY(RandomUtils.nextInt(0, 1)==1?RandomUtils.nextInt(0, 2):0-RandomUtils.nextInt(0, 2));
            int s = 0;
            if(i/Double.valueOf(distance)>0.05){
            	if(i/Double.valueOf(distance)<0.85){
            		s = RandomUtils.nextInt(2, 5);
            	}else {
            		s = RandomUtils.nextInt(10, 15);
				}
            }else{
        		s = RandomUtils.nextInt(20, 30);
            }
            moveEntity.setSleepTime(s);
            list.add(moveEntity);
        	i = i + r;
		} while (i <= distance+5);
		boolean cc= i>distance;
		for (int j = 0; j < Math.abs(distance-i); ) {
			int r = RandomUtils.nextInt(1, 3);
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(cc?-r:r);
            moveEntity.setY(0);
            moveEntity.setSleepTime(RandomUtils.nextInt(100, 200));
            list.add(moveEntity);
            j = j+r;
		}
        return list;
    }

    static class MoveEntity{
        private int x;
        private int y;
        private int sleepTime;//毫秒

        public MoveEntity(){

        }

        public MoveEntity(int x, int y, int sleepTime) {
            this.x = x;
            this.y = y;
            this.sleepTime = sleepTime;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getSleepTime() {
            return sleepTime;
        }

        public void setSleepTime(int sleepTime) {
            this.sleepTime = sleepTime;
        }
    }
    private static int calcMoveDistince() {
        //小方块距离左边界距离
        int START_DISTANCE = 6;
        int startWidth = (int)(GEETEST_WIDTH_START_POSTION * (imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x);
        START_DISTANCE = (int)(START_DISTANCE * (imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x);
        try {
            BufferedImage geetest1 = ImageIO.read(new File(BASE_PATH + "original.png"));
            BufferedImage geetest2 = ImageIO.read(new File(BASE_PATH + "slider.png"));
            for (int i = startWidth; i < geetest1.getWidth(); i++){
                for(int j = 0; j < geetest1.getHeight(); j++){
                    int[] fullRgb = new int[3];
                    fullRgb[0] = (geetest1.getRGB(i, j)  & 0xff0000) >> 16;
                    fullRgb[1] = (geetest1.getRGB(i, j)  & 0xff00) >> 8;
                    fullRgb[2] = (geetest1.getRGB(i, j)  & 0xff);

                    int[] bgRgb = new int[3];
                    bgRgb[0] = (geetest2.getRGB(i, j)  & 0xff0000) >> 16;
                    bgRgb[1] = (geetest2.getRGB(i, j)  & 0xff00) >> 8;
                    bgRgb[2] = (geetest2.getRGB(i, j)  & 0xff);
                    if(difference(fullRgb, bgRgb) > 255){
                        int moveDistance = (int)((i - START_DISTANCE) / ((imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x));
                        return moveDistance;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("计算移动具体失败");
    }
    private static int difference(int[] a, int[] b){
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
    }
}
