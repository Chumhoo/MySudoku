package com.example.chumhoo.mysudoku;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chumhoo on 16/10/2.
 */

public class Draw {

    /*
     * 绘制 球
     */
    static public void drawSphere(GL10 gl, float r, int tiers, int blocks){
        float tiersAnger =(float) (Math.PI / tiers);      // 一个层的角度差
        float blockAngle =(float) ((Math.PI *2) / blocks);// 一个圆每个的角度差

        List<Float> pos =new ArrayList<Float>();
        for(int i =0;i <tiers; i++){
            //一次画两个圆层
            float alpha0 =(float) ((-Math.PI/2) +(i *tiersAnger)) ;
            float alpha1 =(float) ((-Math.PI/2) +((i +1) *tiersAnger)) ;
            //y 值
            float y0 =(float) (Math.sin(alpha0) *r);
            float y1 =(float) (Math.sin(alpha1) *r);
            //圆半径
            float r0 =Math.abs((float) (Math.cos(alpha0) *r));
            float r1 =Math.abs((float) (Math.cos(alpha1) *r));

            for(int j=0;j <=blocks; j++){
                //圆坐标
                float x0 = (float) (Math.cos(j *blockAngle) *r0);
                float z0 = (float) (Math.sin(j *blockAngle) *r0);

                float x1 = (float) (Math.cos(j *blockAngle) *r1);
                float z1 = (float) (Math.sin(j *blockAngle) *r1);

                pos.add(x0);
                pos.add(y0);
                pos.add(z0);
                pos.add(x1);
                pos.add(y1);
                pos.add(z1);
            }
        }

        ByteBuffer pBB =BufferUtil.list2ByteBuffer(pos);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, pBB);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, pos.size()/3);
    }

    /*
     * 绘制矩形
     */
    public static void drawRect(GL10 gl ,float[] pos, float[] colors){
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, BufferUtil.arr2ByteBuffer(colors));
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, BufferUtil.arr2ByteBuffer(pos));
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, pos.length/3);
    }
}