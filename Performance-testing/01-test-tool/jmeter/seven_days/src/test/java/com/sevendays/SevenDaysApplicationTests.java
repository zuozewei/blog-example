package com.sevendays;
import com.sevendays.pojo.ItemTable;
import com.sevendays.pojo.UserTable;
import com.sevendays.service.ItemTableService;
import com.sevendays.service.UserService;
//import org.junit.jupiter.api.Test;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runner.RunWith;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class SevenDaysApplicationTests {
    @Autowired
    UserService userService;

    @Autowired
    ItemTableService itemTableService;


    /**
     * 测试一把看看代码
     */
    @Test
    void contextLoads() {

        List<UserTable> allList = userService.findAllList();
        for (int i = 0; i < allList.size(); i++) {
            System.out.println(allList.get(i));
            if (i == 10) {
                break;
            }
        }

    }

    @Test
    void contextLoadsq() {
        String user = "600128";
        List<ItemTable> itemInfo = itemTableService.findItemInfo(user);
        itemInfo.forEach(n->{
            System.out.println(n);
        });

    }



}
