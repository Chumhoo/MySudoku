package com.example.chumhoo.mysudoku;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by chumhoo on 16/10/2.
 */
public class BufferUtil {
    public static ByteBuffer arr2ByteBuffer(float[] arr){
        //分配字节缓冲区空间,存放顶点坐标
        //float 为4个字节
        ByteBuffer ibb=ByteBuffer.allocateDirect(arr.length*4);
        //设置顺序（本地数据）
        ibb.order(ByteOrder.nativeOrder());
        //放置顶点坐标数组
        FloatBuffer fbb=ibb.asFloatBuffer();
        fbb.put(arr);
        //定位指针位置,从该位置开始读取顶点数据
        ibb.position(0);
        return ibb;
    }

    public static ByteBuffer list2ByteBuffer(List<Float> list){
        ByteBuffer ibb=ByteBuffer.allocateDirect(list.size()*4);
        ibb.order(ByteOrder.nativeOrder());

        FloatBuffer fbb=ibb.asFloatBuffer();

        for (Float f : list) {
            fbb.put(f);
        }
        ibb.position(0);
        return ibb;
    }

    public static ByteBuffer arr2ByteBuffer(byte[] arr){
        //分配字节缓冲区空间,存放顶点坐标
        ByteBuffer ibb=ByteBuffer.allocateDirect(arr.length);
        //设置顺序（本地数据）
        ibb.order(ByteOrder.nativeOrder());
        //放置顶点坐标数组
        ibb.put(arr);
        //定位指针位置,从该位置开始读取顶点数据
        ibb.position(0);
        return ibb;
    }

    public static FloatBuffer arr2FloatBuffer(float[] arr){
        FloatBuffer fbb =FloatBuffer.allocate(arr.length);
        fbb.order();
        fbb.put(arr);
        fbb.position(0);
        return fbb;
    }
}