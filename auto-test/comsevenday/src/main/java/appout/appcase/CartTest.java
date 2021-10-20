package appout.appcase;

import appout.appcase.data.CartProvider;
import org.testng.annotations.Test;

/**
 * @author 7DGroup
 * @Title: CartTest
 * @Description: 测试类 extends BestRuner
 * @date 2019/11/23 / 19:57
 */

public class CartTest  {

    CartProvider cartProvider = new CartProvider();

    @Test(description = "点击首页百宝箱", testName = "点击百宝箱", priority = 1)
    public void T002() {
//        cartProvider.clickHome(driver);
//        cartProvider.clickbaibao(driver);
        System.out.println("调试");
    }

}
