package com.example.chumhoo.mysudoku;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.*;
import static java.lang.StrictMath.cos;

/**
 * Created by chumhoo on 16/10/2.
 */

/*
 * 颜色立方体
 */
public class MyColorCubeRenderer extends MyAbstractRenderer {

    GL10 gl;
    private float cubeCenter[][] = new float[81][3];
    private float cubeR = 0.3f;
    private Resources res;

    private final float MAX_BOUNCE_VALUE = 1.0f;
    private float bounceValue, maxBounceValue = MAX_BOUNCE_VALUE;
    private boolean BOUNCE = false, BOUNCEAWAY = true;


    private final float MAX_POSITION_OFFSET = 10.0f;
    private float positionOffset[] = new float[81];
    private float changingCoords[] = new float[81];
    private float changingSpeeds[] = new float[81];

    private float [] textureCoords={
            0, 1,
            1, 1,
            0, 0,
            1, 0,
    };
    private float[] bkgCoords={
            -40f, -80.0f, -50.0f,
            40f, -80.0f, -50.0f,
            -40f, 80.0f, -50.0f,
            40f, 80.0f, -50.0f
    };

    int[] numId = {
            R.drawable.num0_0, R.drawable.num0_1, R.drawable.num0_2,
            R.drawable.num0_3, R.drawable.num0_4, R.drawable.num0_5,
            R.drawable.num0_6, R.drawable.num0_7, R.drawable.num0_8,
            R.drawable.num0_9,
            R.drawable.num1_0, R.drawable.num1_1, R.drawable.num1_2,
            R.drawable.num1_3, R.drawable.num1_4, R.drawable.num1_5,
            R.drawable.num1_6, R.drawable.num1_7, R.drawable.num1_8,
            R.drawable.num1_9
    };
    int[] surfaceId = {
            R.drawable.surface0_0, R.drawable.surface0_1,
            R.drawable.surface1_0, R.drawable.surface1_1,
    };
    int[] bkgId = {
            R.drawable.bkg_0, R.drawable.bkg_1
    };
    int textureNumCount  = numId.length + surfaceId.length + bkgId.length;

    int[] numIdGen =new int[numId.length]; //存放数字id
    int[] surfaceIdGen = new int[surfaceId.length]; //存放纹理Id
    int[] bkgIdGen = new int[bkgId.length]; //存放背景Id

    float[][] lightPos ={
            {0.0f, 0.0f, 5.0f, 0.0f},
            {eyePos[0], eyePos[1], eyePos[2], 1.0f}
    };

    //环境光
    float[][] ambient_light = {{0.5f, 0.5f, 0.5f, 1.0f}};
    //漫反射光
    float[][] diffuse_light = {{0.5f, 0.5f, 0.5f, 1.0f}};
    //镜面光
    float[][] specular_light = {{0.5f, 0.5f, 0.5f, 1.0f}};


    //立方体颜色
    float[] colors = {
            0f, 0f, 1f, 1.0f,
            0f, 0f, 0f, 1.0f,
            1f, 0f, 1f, 1.0f,
            1f, 0f, 0f, 1.0f,
            0f, 1f, 1f, 1.0f,
            0f, 1f, 0f, 1.0f,
            1f, 1f, 1f, 1.0f,
            1f, 1f, 0f, 1.0f
    };
    float[] blue = {
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f,
            0f, 0f, 0f, 1.0f
    };
    float[] white = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    public MyColorCubeRenderer(int width, int height, Resources res)
    {
        screenWidth = width;
        screenHeight = height;
        this.res = res;

        for (int i = 0; i < 81; i++)
        {
            cubeCenter[i][0] = i % 9 - 4.3f + cubeR * (i%9/3);
            cubeCenter[i][1] = i / 9 - 4.0f + cubeR * (i/9/3);
            cubeCenter[i][2] = -2;
        }
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
        this.gl = gl;
        //设置清屏色（背景）
        gl.glClearColor(0, 0, 0, 1);
        //启用顶点缓冲区
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 颜色缓冲区
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        //启用深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);
        //启用表面剔除 //剔除 ：如果看不见 就告诉openGL 不用绘制 （提高性能）
        gl.glEnable(GL10.GL_CULL_FACE);
        //openGL默认 逆时针为正面
        gl.glFrontFace(GL10.GL_CCW);//逆时针  为正面

        //开启透明
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        loadTexture(gl);
    }
    protected void drawBefore(GL10 gl) {
        //清除颜色缓冲区 |深度缓冲区
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
    }

    protected void draw(GL10 gl) {
        //启用光照
        gl.glEnable(GL10.GL_LIGHTING);

        //重放法线规范化(在转换之后和光照之前，通过一个由模型矩阵计算出来的因子来对法线向量进行缩放)
        gl.glEnable(GL10.GL_RESCALE_NORMAL);
        //设置光源 支持8个光源
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glEnable(GL10.GL_LIGHT1);

        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, BufferUtil.arr2FloatBuffer(ambient_light[0]));
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, BufferUtil.arr2FloatBuffer(diffuse_light[0]));
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, BufferUtil.arr2FloatBuffer(ambient_light[0]));
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, BufferUtil.arr2FloatBuffer(diffuse_light[0]));

        //镜面光 //如果不定义任何材料的镜面反射属性，将不会看到任何镜面光效
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR,BufferUtil.arr2FloatBuffer(specular_light[0]));
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR,BufferUtil.arr2FloatBuffer(specular_light[0]));

        //设置光源位置
        //最后一个只是1.0表示这就是光源的位置，如果是0 表示光源位于无限远 得到平行光
        for (int i = 0; i < 3; i++) lightPos[1][i] = eyePos[i];
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, BufferUtil.arr2FloatBuffer(lightPos[0]));
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, BufferUtil.arr2FloatBuffer(lightPos[1]));

        //设置光源切角
        gl.glLightf(GL10.GL_LIGHT1, GL10.GL_SPOT_CUTOFF, 10);

        //颜色追踪  与物理世界一样 物体什么颜色 反射什么光 (glColor4f 设置的值再与 光的rgb亮度值相乘 为最终的rgb值)
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        gl.glColor4f(1, 1, 1, 1);

        //设置 材料属性
        //镜面光 需要单独设置 材料属性
        float gray[] ={1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, BufferUtil.arr2FloatBuffer(gray));
        //设置材料的镜面指数(亮度，1~128) (好像没什么效果)
        gl.glMaterialx(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 128);

        //禁用背面光照
        gl.glCullFace(GL10.GL_BACK);

        bindTexture(gl, bkgIdGen[theme]);
        Draw.drawRect(gl, bkgCoords, white);

        if (changePositions(gl)) return;

        for (int i = 0; i < 81; i++)
        {
            if (i != cubePicked)
            {
                //绘制已存在的数字
                if (sudoku.getPuzzle(i/9, i%9) != 0) drawCube(cubeCenter[i], white, cubeR, gl, sudoku.getPuzzle(i/9, i%9), theme, 1, false);
                //绘制不存在的数字
                else
                {
                    //帮助提示一个数
                    if (i == help_Pos[0] * 9 + help_Pos[1])
                        drawCube(cubeCenter[i], colors, cubeR, gl, sudoku.getAnswer(i/9, i%9), theme, 0, false);
                    else drawCube(cubeCenter[i], white, cubeR, gl, sudoku.getMyAnswer(i/9, i%9), theme, 0, false);
                }
            }
        }

        //打开小转盘
        if (cubePicked != -1)
        {
            float[] smallCubeCenter = new float[3];
            float maxSpereadDistance = 2.0f;

            smallCubeCenter[0] = cubeCenter[cubePicked][0];
            smallCubeCenter[1] = cubeCenter[cubePicked][1];
            smallCubeCenter[2] =  spreadFactor + cubeCenter[cubePicked][2];

            if (0 == smallCubePicked) drawCube(smallCubeCenter, colors, cubeR * 1.2f, gl, 0, theme, 0, true);
            else drawCube(smallCubeCenter, white, cubeR * 1.2f, gl, 0, theme, 0, true);
            for (float j = 0.0f; j < 9.0f && cubePicked != -1; j++)
            {
                smallCubeCenter[0] = (float)(cos(6.28f * j / 9.0f)) * spreadFactor + cubeCenter[cubePicked][0];
                smallCubeCenter[1] = (float)(sin(6.28f * j / 9.0f)) * spreadFactor + cubeCenter[cubePicked][1];
                smallCubeCenter[2] =  spreadFactor + cubeCenter[cubePicked][2];

                if ((j + 1)== smallCubePicked)
                    drawCube(smallCubeCenter, colors, cubeR * 1.2f, gl, (int)j + 1, theme, 0, false);
                else
                    drawCube(smallCubeCenter, white, cubeR * 1.2f, gl, (int)j + 1, theme, 0, false);
            }
            if (spreadFactor < maxSpereadDistance) spreadFactor += 0.1f;
        }


        if (!ZOOM_IN_MODE) zoomOut();
        else zoomIn();
    }

    private boolean changePositions(GL10 gl)
    {
        boolean allDone = true;
        if ( !CHANGING_POSITION ) return false;
        if (positionOffset[0] == 0.0f)
        {
            for (int i = 0; i < 81; i++) {
                changingSpeeds[i] = (float) random() / 2 + 0.5f;
                changingCoords[i] = 1.0f;
                positionOffset[i] = MAX_POSITION_OFFSET;
            }
        }
        float center[] = new float[3];
        for (int i = 0; i < 81; i++)
        {
            if (positionOffset[i] > -MAX_POSITION_OFFSET) {
                positionOffset[i] -= changingSpeeds[i];
                center[2] = cubeCenter[i][2] + MAX_POSITION_OFFSET - abs(positionOffset[i]);
                allDone = false;
            }
            else center[2] = cubeCenter[i][2];
            center[0] = cubeCenter[i][0];
            center[1] = cubeCenter[i][1];
            drawCube(center, white, cubeR, gl, 0, theme, 1, false);
        }
        if (allDone)
        {
            CHANGING_POSITION = false;
            positionOffset[0] = 0.0f;
            hintNumber = 70 - stage * 5;
            sudoku.generate(hintNumber);
            return false;
        }
        return true;
    }
    private void drawCube(float[] center, float colors[], float cubeRadius, GL10 gl, int number, int _theme, int _surfaceType, boolean showZero)
    {
        //2016.10.4注意！！！！！！！！！
        //采用的绘图方式为GL_TRIANGLE_STRIP！注意顶点顺序为Z字形！

        //绘制纹理
        bindTexture(gl, surfaceIdGen[_surfaceType + _theme * 2]);
        //八个顶点的坐标
        //立方体的中心点
        float front[] = {
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius,
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius
        };

        Draw.drawRect(gl, front, colors);
        float back[] = {
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius
        };
        Draw.drawRect(gl, back, colors);

        float left[] = {
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius,
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius,
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius
        };
        Draw.drawRect(gl, left, colors);
        float right[] = {
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius
        };
        Draw.drawRect(gl, right, colors);
        float top[] = {
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] - cubeRadius,
                center[0] - cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] - cubeRadius, center[2] + cubeRadius
        };
        Draw.drawRect(gl, top, colors);
        float down[] = {
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] + cubeRadius,
                center[0] - cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius,
                center[0] + cubeRadius, center[1] + cubeRadius, center[2] - cubeRadius
        };
        Draw.drawRect(gl, down, colors);

        front[2] += 0.1f; front[5] += 0.1f;front[8] += 0.1f;front[11] += 0.1f;
        //绘制数字
        if (number == 0 && !showZero) return;
        bindTexture(gl, numIdGen[_theme * 10 + number]);
        Draw.drawRect(gl, front, colors);
    }

    public void zoomIn()
    {
        float minDistance = 0.2f, moveStep = 0.2f;
        int cubeCen = cubePicked;
        float moveForwardMax = 0.5f;

        //未拾起任何方块
        if (cubeCen == -1) return;

        for (int i = 0; i < 2; i++) {
            if (abs(eyePos[i] - cubeCenter[cubeCen][i]) > minDistance) {
                if (eyePos[i] > cubeCenter[cubeCen][i]) eyePos[i] -= moveStep;
                if (eyePos[i] < cubeCenter[cubeCen][i]) eyePos[i] += moveStep;
            }
        }
        for (int i = 0; i < 2; i++) {
            if (abs(eyeCoord[i] - cubeCenter[cubeCen][i]) > minDistance) {
                if (eyeCoord[i] > cubeCenter[cubeCen][i]) eyeCoord[i] -= moveStep;
                if (eyeCoord[i] < cubeCenter[cubeCen][i]) eyeCoord[i] += moveStep;
            }
        }
        if (5 - eyePos[2] < moveForwardMax) eyePos[2] -= moveStep;

        maxBounceValue = abs(eyePos[2] - 5.0f);
        bounceValue = 0.0f;
        BOUNCE = false;
        BOUNCEAWAY = true;
    }
    public void zoomOut()
    {
        float minDistance = 0.2f, moveStep = 0.3f;
        float bounceSpeed = abs(maxBounceValue) / 2.0f;
        //限制回弹最小速度
        if (bounceSpeed < 0.01f) bounceSpeed = 0.01f;

        for (int i = 0; i < 2; i++) {
            if (abs(eyePos[i] - 0) > minDistance) {
                if (eyePos[i] > 0) eyePos[i] -= moveStep;
                else eyePos[i] += moveStep;
            }
            if (abs(eyeCoord[i] - 0) > minDistance) {
                if (eyeCoord[i] > 0) eyeCoord[i] -= moveStep;
                else eyeCoord[i] += moveStep;
            }
        }
        if (abs(eyePos[2] - 5) > minDistance && BOUNCE == false) {
            if (eyePos[2] < 5) eyePos[2] += moveStep;
        }
        else
        {
            BOUNCE = true;
            //是否继续回弹
            if (abs(maxBounceValue) > 0.05f) {
                //是否仍未弹到最大幅度
                if ((BOUNCEAWAY && bounceValue < maxBounceValue) || (!BOUNCEAWAY && bounceValue > maxBounceValue)) {
                    //回弹方向
                    if (BOUNCEAWAY) bounceValue += bounceSpeed;
                    else bounceValue -= bounceSpeed;
                    eyePos[2] = 5.0f + bounceValue;
                }
                else
                {
                    if (maxBounceValue > 0) maxBounceValue = -(abs(maxBounceValue) - bounceSpeed);
                    else maxBounceValue = (abs(maxBounceValue) - bounceSpeed);
                    if (BOUNCEAWAY) BOUNCEAWAY = false;
                    else BOUNCEAWAY = true;
                }
            }
            else BOUNCE = false;
        }


        if (flyFactor < 6.0f && lastNumber > 0)
        {
            float newColor[] = new float[8*4];
            for (int i = 0; i < colors.length; i++)
            {
                if (i % 4 == 3) newColor[i] = 1.0f - flyFactor / 6.0f;
                else newColor[i] = colors[i];
            }
            float center[] = { (-cubeCenter[lastCubePicked][0] * flyFactor / 5.0f) + cubeCenter[lastCubePicked][0],
                    (-cubeCenter[lastCubePicked][1] * flyFactor / 5.0f) + cubeCenter[lastCubePicked][1], flyFactor - 2.0f};
            drawCube(center, newColor, cubeR, gl, lastNumber, theme, 0, true);
            flyFactor += 0.17f;
        }

    }

    private void loadTexture(GL10 gl)
    {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glGenTextures(numId.length, numIdGen, 0); //获取纹理id
        gl.glGenTextures(surfaceId.length, surfaceIdGen, 0); //获取纹理id
        gl.glGenTextures(bkgId.length, bkgIdGen, 0); //获取纹理id

        //openGL ES 支持  GL10.GL_CLAMP_TO_EDGE(不重复)、GL10.GL_REPEAT
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT); //超过 s 则是重复出现
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT); //超过 t 则是重复出现

        for (int i = 0; i < numId.length; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, numIdGen[i]);//绑定纹理id 纹理为2d
            //获取 资源
            Bitmap image = BitmapFactory.decodeResource(this.res, numId[i]);

            //参数 说明
            //target 参数用于定义二维纹理；
            //level  如果提供了多种分辨率的纹理图像，可以使用level参数，否则level设置为0；
            //bitmap 位图
            //border 参数表示边框的宽度
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);//加载纹理
        }
        for (int i = 0; i < surfaceId.length; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, surfaceIdGen[i]);//绑定纹理id 纹理为2d
            Bitmap image = BitmapFactory.decodeResource(this.res, surfaceId[i]);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);//加载纹理
        }
        for (int i = 0; i < bkgId.length; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, bkgIdGen[i]);//绑定纹理id 纹理为2d
            Bitmap image = BitmapFactory.decodeResource(this.res, bkgId[i]);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);//加载纹理
        }
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    private void bindTexture(GL10 gl, int textureID)
    {
//        gl.glDisable(GL10.GL_BLEND);

        //启用纹理
        gl.glEnable(GL10.GL_TEXTURE_2D);

        //指定纹理过滤
        //由于提供的纹理图像很少能和最终的屏幕坐标形成对应,大小不同,所以需要设置过滤项目.允许我们进行插值或者匀和,指定放大缩小的函数.
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);//最大 线性
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);//最小 线性

        //纹理环绕
        //在 OpenGl 中是通过指定纹理坐标来将纹理映射到多边形上去的.
        //在纹理坐标系中, 左下角是 (0,0), 右上角是 (1,1).
        //2D 纹理的坐标中通过指定 (s,t) (s为x轴上,t为y轴上, 取值0~1).
        //1D, 3D, 4D纹理坐标系中对应的需要指定 (s), (s,t,r), (s,t, r,q).
        //指定纹理坐标
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, BufferUtil.arr2ByteBuffer(textureCoords));

        //坐标影响 图片展示 方向
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);//绑定纹理id 纹理为2d

//        gl.glEnable(GL10.GL_BLEND);
    }

    private void delTexture(GL10 gl)
    {
        //创建一个纹理对象后, OpenGL为其分配内存, 所以当不再使用一个纹理对象时, 为防止内存泄露, 必须删除.
        gl.glDeleteTextures(numId.length, numIdGen, 0);
        gl.glDeleteTextures(surfaceId.length, surfaceIdGen, 0);
        gl.glDeleteTextures(bkgId.length, bkgIdGen, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }
}