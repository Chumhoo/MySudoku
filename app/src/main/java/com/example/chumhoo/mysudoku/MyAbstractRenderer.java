package com.example.chumhoo.mysudoku;

import android.app.Dialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.*;

/**
 * Created by chumhoo on 16/10/2.
 */

public abstract class MyAbstractRenderer implements GLSurfaceView.Renderer {

    protected int screenWidth, screenHeight;

    //围绕x旋转角度
    private float xRotate = 0f;
    //围绕y旋转角度
    private float yRotate = 0f;
    //宽高比
    protected float ratio;
    //屏幕宽
    protected int width;
    //屏幕高
    protected int height;

    protected float[] eyeCoord = {0, 0, 0};
    protected float[] eyePos = {0, 0, 4};
    protected float[] eyeSensor = {0, 0, 0};

    protected float mouseX, mouseY;

    protected int cubePicked = -1, smallCubePicked = -1;
    protected int lastNumber = -1, lastCubePicked = -1;
    protected float flyFactor = 0.0f;
    protected float spreadFactor = 0.0f;

    protected float preX, preY;

    private int[] undoPointer = new int[81];
    private int[][] undoTable = new int[81][1000];
    private int lastDidPointer = 0;
    private int[] lastDid = new int[1000];

    Sudoku sudoku = new Sudoku();
//    int[][] puzzle = new int[9][9], myAnswer = new int[9][9], answer = new int[9][9];
    int stage = 1;
    final int MAX_LEVELS = 10;
    int hintNumber = 70 - stage * 5;

    int help_Pos[] = {-1, -1};

    long roundBeginTime = System.currentTimeMillis(), roundEndTime;

    protected boolean CHANGING_POSITION = true;

    public void setMouse(float x, float y) { mouseX = x; mouseY = y; }

    public int theme = 0;


    protected boolean ZOOM_IN_MODE = false;
    public void setZoomMode(boolean mode, float x, float y) {
        ZOOM_IN_MODE = mode;
        mouseX = x; mouseY = y;
    }

    public MyAbstractRenderer()
    {
        refreshUndoTable();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
        //设置清屏色（背景）
        gl.glClearColor(0, 0, 0, 1);
        //启用顶点缓冲区
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //为了给每个点都有颜色 启用颜色缓冲区
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);


    }
    //表层size改变
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //openGL基于状态机
        /*单位矩阵
         * [1,0,0,0]
         * [0,1,0,0]
         * [0,0,1,0]
         * [0,0,0,1]
         */
        this.width  =width;
        this.height =height;
        //视口
        gl.glViewport(0, 0, width, height);

        //矩阵模式
        //透视投影：有深度
        //正投影:没有深度
        gl.glMatrixMode(GL10.GL_PROJECTION);//(投影矩阵)
        //原则:在操作矩阵前先加载单位矩阵 然后才干别的事情
        //将当前的用户坐标系的原点移到了屏幕中心：类似于一个复位操作
        gl.glLoadIdentity();

        //平截头体
        //左侧(宽高比)，右侧(宽高比)，下侧,上侧，近平面、远平面
        ratio =(float)width/(float)height;
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 1, 100.0f);
        //正投影
//        gl.glOrthof(-ratio, ratio,-1f, 1f, 3, 7);
    }
    /*
     *绘图
     * 在子线程运行
     */
    public void onDrawFrame(GL10 gl) {
        this.drawBefore(gl);
        //模型视图矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        //eyex,eyey,eyez        放置眼球的坐标
        //centerx,centery,ceterz眼球的观察点
        //upx,upy,upz           指定眼球向上的向量
        GLU.gluLookAt(gl, eyePos[0], eyePos[1], eyePos[2], eyeCoord[0]+eyeSensor[0], eyeCoord[1]+eyeSensor[1], eyeCoord[2]+eyeSensor[2], 0, 1, 0);

        //旋转角度
        gl.glRotatef(xRotate, 1, 0, 0);
        gl.glRotatef(yRotate, 0, 1, 0);
        gl.glTranslatef(0, 0, -2);

        this.draw(gl);
    }

    /*
     * 绘图
     * 参数的基本设置
     */
    public void eyeMove(float x, float y, float level, boolean ADJUST)
    {
        float minValue = 0.1f;
        if (ADJUST)
        {
            eyeSensor[0] = 0.0f;
            eyeSensor[1] = 0.0f;
            return;
        }

        if (abs(eyeSensor[0] - x) > minValue)
        {
            if (eyeSensor[0] > x) {
                eyeSensor[0] -= level;
            }
            else {
                eyeSensor[0] += level;
            }
        }
        if (abs(eyeSensor[1] - y) > minValue)
        {
            if (eyeSensor[1] > y) {
                eyeSensor[1] -= level;
            }
            else {
                eyeSensor[1] += level;
            }
        }

    }

    protected void drawBefore(GL10 gl) {
        //清除颜色缓冲区
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //设置绘图的颜色 只要以后没有设置颜色 就一直用这个颜色
        gl.glColor4f(1f, 0, 0, 1f);

    }
    //子类实现绘图的方法
    protected abstract void draw(GL10 gl);

    //光源 位置x值
    protected float light_x=0;
    //光源 位置y值
    protected float light_y=0;

    public float getLight0_x() {
        return light_x;
    }
    public void setLight0_x(float light0_x) {
        this.light_x = light0_x;
    }
    public float getLight0_y() {
        return light_y;
    }
    public void setLight0_y(float light0_y) {
        this.light_y = light0_y;
    }

    public boolean pickCube(float x, float y)
    {
        int cubeNumX, cubeNumY;
        float maxHeight = 0.79f * screenHeight, minHeight = 0.2f * screenHeight, totalHeight = maxHeight - minHeight;
        float minWidth = 0.04f * screenWidth, maxWidth = 0.96f * screenWidth, totalWidth = maxWidth - minWidth;

        if (y < minHeight || y > maxHeight || x < minWidth || x > maxWidth) return false;

        cubeNumX = (int)((x - minWidth) / totalWidth / (1.0f/9));
        cubeNumY =  8 - (int)((y - minHeight) / totalHeight / (1.0f/9));

        if (sudoku.getPuzzle(cubeNumY, cubeNumX) != 0) return false;

        cubePicked = cubeNumY * 9 + cubeNumX;
        spreadFactor = 0.0f;

        preX = x;
        preY = y;
        return true;
    }
    public int dropCube()
    {
        if (cubePicked != -1 && smallCubePicked != -1)
            sudoku.fillIn(cubePicked/9, cubePicked%9,  smallCubePicked);

        lastNumber = smallCubePicked;
        lastCubePicked = cubePicked;
        flyFactor = 0.0f;

        smallCubePicked = -1;
        cubePicked = -1;

        //  for undo  ///////
        if (lastNumber == -1 || lastCubePicked == -1)
        {
            return -1;
        }
        for (int i = lastDidPointer; lastDid[i] != -1 && i < lastDid.length; i++)
            lastDid[i] = -1;
        lastDid[lastDidPointer++] = lastCubePicked;
        undoTable[lastCubePicked][undoPointer[lastCubePicked]] = lastNumber;
        undoPointer[lastCubePicked]++;
        /////////////

        return lastNumber;

    }


    //返回值服务于震动反馈
    public boolean pickNumber(float x, float y)
    {
        int preNum = smallCubePicked;

        if (cubePicked == -1) return false;
        //手指不动，不改变选取的数字
        if (sqrt(pow(x - preX, 2) + pow(y - preY, 2)) < 100.0f)
        {
            smallCubePicked = sudoku.getMyAnswer(cubePicked/9, cubePicked%9);
            return false;
        }
        //手指在圆盘中间，选取0
        if (smallCubePicked != -1 && sqrt(pow(x - screenWidth/2, 2) + pow(y - screenHeight/2, 2)) < 200.0f)
        {
            smallCubePicked = 0;
            return (preNum != smallCubePicked);
        }

        double temp = (atan((y-screenHeight/2) / (x-screenWidth/2) / 2.0f)) + 3.14f;
        if ((x - screenWidth/2) < 0)
            temp += 3.14f;
        int num = (int)(temp / 6.28f * 9.0f) - 2;
        num = 8 - (num + 6) % 9;

        smallCubePicked = num + 1;
        help_Pos[0] = -1;
        help_Pos[1] = -1;
        return (preNum != smallCubePicked);
    }

    protected void copyArray(int[][] src, int[][] des)
    {
        if (src.length != des.length || src[0].length != des[0].length) return;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
            {
                des[i][j] = src[i][j];
            }
    }

    public void newGame()
    {
        CHANGING_POSITION = true;
        help_Pos[0] = -1;
        help_Pos[1] = -1;
        roundBeginTime = System.currentTimeMillis();
        roundEndTime = roundBeginTime;

        refreshUndoTable();
    }

    public void getHelp()
    {
        int[] randomSequenceX = sudoku.nonRepeatSeq(9);
        int[] randomSequenceY = sudoku.nonRepeatSeq(9);
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if (sudoku.getMyAnswer(randomSequenceX[i], randomSequenceY[j]) != sudoku.getAnswer(randomSequenceX[i], randomSequenceY[j]))
                {
                    help_Pos[0] = randomSequenceX[i];
                    help_Pos[1] = randomSequenceY[j];
                    sudoku.fillIn(help_Pos[0], help_Pos[1], sudoku.getAnswer(help_Pos[0], help_Pos[1]));

                    lastNumber = sudoku.getAnswer(help_Pos[0], help_Pos[1]);
                    lastCubePicked = help_Pos[0] * 9 + help_Pos[1];
                    //  for undo  ///////
                    if (lastNumber == -1 || lastCubePicked == -1)
                    {
                        return;
                    }
                    for (int k = lastDidPointer; lastDid[k] != -1 && k < lastDid.length; k++)
                        lastDid[k] = -1;
                    lastDid[lastDidPointer++] = lastCubePicked;
                    undoTable[lastCubePicked][undoPointer[lastCubePicked]] = lastNumber;
                    undoPointer[lastCubePicked]++;
                    /////////////
                    return;
                }
            }
        }
    }

    public boolean checkPass()
    {
        if (!sudoku.checkPass()) return false;

        else {
            hintNumber = 81 - stage * 5;
            if (roundEndTime == roundBeginTime) {
                stage++;
                roundEndTime = System.currentTimeMillis();
            }
            return true;
        }
    }


    public boolean undo()
    {
        int pointer = 0;
        if (lastDidPointer > 0) pointer = lastDid[lastDidPointer-1];
        else return false;
        undoPointer[pointer] -= 1;
        lastDidPointer -= 1;
        sudoku.fillIn(pointer/9, pointer%9, undoTable[pointer][undoPointer[pointer]-1]);

        help_Pos[0] = -1;
        help_Pos[1] = -1;
        return true;
    }

    public boolean redo()
    {
        int pointer = 0;
        if (lastDid[lastDidPointer] >= 0) pointer = lastDid[lastDidPointer];
        else return false;
        sudoku.fillIn(pointer/9, pointer%9, undoTable[pointer][undoPointer[pointer]]);
        undoPointer[pointer] += 1;
        lastDidPointer += 1;
        return true;
    }


    void refreshUndoTable()
    {
        for (int i = 0; i < undoPointer.length; i++) undoPointer[i] = 1;
        for (int i = 0; i < lastDid.length; i++) lastDid[i] = -1;
        for (int i = 0; i < undoTable.length; i++)
            for (int j = 0; j < undoTable[i].length; j++)
                undoTable[i][j] = 0;
        lastDidPointer = 0;
    }



}