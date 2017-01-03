package me.jessyan.mvparms.demo.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by luoliwen on 16/4/28.
 * 完成对数据库的CRUD的操作
 */
public class ZhihuDao {
    private Context context;
    private Dao<Zhihu, Integer> userDao;
    private DataBaseHelper helper;
    private static ZhihuDao sDBUtis;
    public ZhihuDao(Context context) {
        this.context = context;
        helper = DataBaseHelper.getInstance(context);
        try {
            userDao = helper.getDao(Zhihu.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized ZhihuDao getDB(Context context) {
        if (sDBUtis == null)
            sDBUtis = new ZhihuDao(context);
        return sDBUtis;
    }

    public void addUser(Zhihu user) {
        try {
            List<Zhihu> list = new ArrayList<>();
             if(listAll() == null){
                 return;
             }
            list = listAll();
            if(listAll().size() > 200){
                deleteMulUser(list);
            }
            userDao.create(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * select * from user_info
     *
     * @return
     */
    public List<Zhihu> listAll() {
        try {
            return userDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteMulUser(List<Zhihu> users) {
        try {
            userDao.delete(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void insertHasRead(String key, int value) {
        Zhihu user = new Zhihu();
        user.setKey(key);
        user.setIs_read(value);
        addUser(user);
    }
    public boolean isRead(String key, int value) {
        boolean isRead = false;
        for(Zhihu zhihu : queryBuilder(key)){
            if(zhihu.getIs_read() == value){
                isRead = true;
            }
        }
        return isRead;
    }


    /**
     * 查询条件 一
     * select * from user_info where  (age> 23 and name like ? ) and XXXX
     *
     * @return
     */
    public List<Zhihu> queryBuilder(String key) {
        List<Zhihu> list = new ArrayList<>();
        QueryBuilder<Zhihu, Integer> queryBuilder = userDao.queryBuilder();
        //声明的是一个where 条件
        try {
            list = userDao.queryBuilder().where().eq("key", key).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
