package com.example.chumhoo.mysudoku;

import java.util.Random;

import static java.lang.Math.abs;

import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Math.abs;

/**
 * Created by chumhoo on 16/10/3.
 */

public class Sudoku
{
    private int[][] puzzle;
    private int[][] answer;
    private int[][] myAnswer;
    private int unSolved;
    Random rand;

    public Sudoku()
    {
        puzzle = new int[9][9];
        answer = new int[9][9];
        myAnswer = new int[9][9];
        rand = new Random();
    }


    public boolean checkPass()
    {
        if (unSolved == 0) {
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++) {
                    if (!check(myAnswer[i][j], myAnswer, i, j))
                        return false;
                }
            return true;
        }
        else return false;
    }

    public int getMyAnswer(int row, int col)
    {
        return myAnswer[row][col];
    }

    public int getPuzzle(int row, int col)
    {
        return puzzle[row][col];
    }

    public int getAnswer(int row, int col)
    {
        return answer[row][col];
    }
    public void fillIn(int row, int col, int num)
    {
        myAnswer[row][col] = num;
        if (num > 0) unSolved--;
        else if (num == 0) unSolved++;
    }

    public int[][] generate(int hintNum)
    {
        puzzle = new int[9][9];
        answer = new int[9][9];
        myAnswer = new int[9][9];

        newTable(puzzle, 0, 0);
        copyTable(puzzle, answer);

        getCover(puzzle, hintNum);
//        System.out.println("The puzzle is:");
//        print(puzzle);

        copyTable(puzzle, myAnswer);
        unSolved = 81 - hintNum;
        return puzzle;
    }

    //num from 0-8, pass from 0-8
    private boolean newTable(int[][] table, int num, int pass)
    {
        if (pass >= 9)
        {
            num++;
            pass = 0;
        }
        if (num == 9 && pass == 0)
        {
            return true;
        }
        int[] seq = nonRepeatSeq(9);
        int row, col, i;
        int chances = availableGrid(table, pass);
        for (i = 0; chances > 0; i++)
        {
            row = pass/3*3 + seq[i] / 3;
            col = pass%3*3 + seq[i] % 3;

            if (table[row][col] == 0)
                chances--;
            if (table[row][col] == 0 && check(num + 1, table, row, col))
            {
                table[row][col] = num + 1;
                if (newTable(table, num, pass+1))
                {
                    return true;
                }
                else
                {
                    table[row][col] = 0;  //undo
                }
            }
        }

        if (chances == 0) return false;
        return true;
    }

    private boolean copyTable(int[][] src, int[][] des)
    {
        if (src.length != des.length || src[0].length != des[0].length)
        {
            System.out.println("Copy failed!");
            return false;
        }
        for (int i = 0; i < src.length; i++)
        {
            for (int j = 0; j < src[i].length; j++)
                des[i][j] = src[i][j];
        }
        return true;
    }

    private boolean check(int value, int[][] table, int row, int col)
    {
        //check through the row and column
        for (int i = 0; i < 9; i++) {
            if (i != col && table[row][i] != 0) {
                if (table[row][i] == value)
                    return false;
            }
            if (i != row && table[i][col] != 0) {
                if (table[i][col] == value)
                    return false;
            }
        }

        //check through the small grids
        int tRow, tCol;
        for (int i = 0; i < 9; i++) {
            tRow = row / 3 * 3 + i / 3;
            tCol = col / 3 * 3 + i % 3;
            if (!(row == tRow && col == tCol) && table[tRow][tCol] != 0)
            {
                if (table[tRow][tCol] == value)
                    return false;
            }
        }
        return true;
    }

    //find out how many grids are available in the small sudoku
    private int availableGrid(int[][] table, int pass)
    {
        int ret = 0;
        int row = pass / 3 * 3;
        int col = pass % 3 * 3;
        for (int i = 0; i < 9; i++)
        {
            if (table[pass/3*3 + i/3][pass%3*3 + i%3] == 0) ret++;
        }
        return ret;
    }

    private void print(int [][] table)
    {
        for (int i = 0; i < 81; i++)
        {
            System.out.print(table[i / 9][i % 9] + " ");
            if (i % 9 == 2 || i % 9 == 5) System.out.print("| ");
            if (i % 9 == 8) System.out.println();
            if (i == 26 || i == 53) System.out.println("----------------------");
        }
        System.out.println();
        System.out.println();
    }

    public int[] nonRepeatSeq(int num)
    {
        int[] sequence = new int[num];
        int newRandom;
        boolean exist;
        for (int i = 0; i < num; i++)
        {
            exist = false;
            newRandom = abs(rand.nextInt() % num);
            for (int j = 0; j < i; j++)
                if (sequence[j] == newRandom)
                {
                    exist = true;
                    i--;
                    break;
                }
            if (!exist) {
                sequence[i] = newRandom;
            }
        }
        return sequence;
    }

    private void getCover(int[][] table, int hintNum)
    {
        int coverNum = 81 - hintNum;
        int startCol, startRow, ranCol, ranRow;
        int[] nonRepeat = nonRepeatSeq(9);
        for (int i = 0; i < coverNum; i++)
        {
            startCol = nonRepeat[i%9] % 3 * 3;
            startRow = nonRepeat[i%9] % 9 / 3 * 3;
            ranCol = abs(rand.nextInt() % 3);
            ranRow = abs(rand.nextInt() % 3);
            while (table[startRow + ranCol][startCol + ranRow] == 0) {
                ranCol = abs(rand.nextInt() % 3);
                ranRow = abs(rand.nextInt() % 3);
            }
            table[startRow + ranCol][startCol + ranRow] = 0;
        }
    }


    private CharSequence copyToCharSequence(int[][] table)
    {
        String str = new String();
        for (int i = 0; i < table.length; i++)
        {
            for (int j = 0; j < table[i].length; j++) {
                str += new String(table[i][j] + "  ");
                if (j % 3  == 2 && j != 8) str += new String("|  ");
            }
            str += new String("\n");
            if (i % 3 == 2 && i != 8) str += new String("--------------------------------------\n");
        }
        CharSequence text = str;
        return text;
    }

    public void solve()
    {
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        if (solveStep(puzzle, 0, 0)) System.out.println("Solved!");
        else System.out.println("Unsolved!");
        endTime = System.currentTimeMillis();
        System.out.println("Cost time : " + 1.0 * (endTime - startTime) / 1000 + "s");

        print(puzzle);
    }

    public boolean solveStep(int[][] table, int num, int pass)
    {
        //num from 0-8, pass from 0-8
        if (pass >= 9)
        {
            num++;
            pass = 0;
        }
        if (num == 9 && pass == 0)
        {
            return true;
        }

        int trow = pass / 3 * 3;
        int tcol = pass % 3 * 3;
        for (int i = 0; i < 9; i++)
        {
            if (table[trow + i / 3][tcol + i % 3] == num + 1)
            {
                return solveStep(table, num, pass + 1);
            }
        }

        int[] seq = nonRepeatSeq(9);
        int row, col, i;
        int chances = availableGrid(table, pass);
        //寻找小九宫格内可用的空格
        for (i = 0; chances > 0; i++)
        {
            row = pass/3*3 + seq[i] / 3;
            col = pass%3*3 + seq[i] % 3;

            if (table[row][col] == 0)
                chances--;
            else
                continue;
            if (table[row][col] == 0 && check(num + 1, table, row, col))
            {
                table[row][col] = num + 1;
                if (solveStep(table, num, pass+1))
                {
                    return true;
                }
                else
                {
                    table[row][col] = 0;  //undo
                }
            }
        }

        //找不到可用的空格
        if (chances == 0) return false;
        return true;
    }

    public boolean solved(int[][] table) {
        int count = 0;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (table[i][j] == 0)
                    count++;
        return count <= 0;
    }
}