package scramblewithfriendssolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.*;


public class ScrambleWithFriendsSolver extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[] { "scramblewithfriendssolver.ScrambleWithFriendsSolver" });
    }
    
    //need to make these global because switch doesn't like it when you initialize them inside the statment
    int boardsize = 4;
    char[][] board = new char[boardsize][boardsize];
    int[][] bonuses = new int[boardsize][boardsize];
    HashSet<String> dictionary = new HashSet(172820);
    BoardSolver solver;
    ArrayList<String> words_found = new ArrayList<String>();
    PGraphics gb;//graphics board

    //keyboard 
    boolean entered = false;//enter
    boolean randomize = false;//space
    String buffer = "";
    int state = 0;

    public void setup() {
        size(board.length*50, board.length*50);
        textFont(createFont("Arial", 14));

        //frameRate(5);//if you want to see current paths slower
        gb = createGraphics(board.length*50, board.length*50);//board buffer
    }

    public void draw() {
        background(255);
        //drawBoard();
        switch(state) {
            case 0://setup
                drawBoard();
                if(dictionary.size() == 0) {//so it doesnt repeat
                    System.out.println("Dictionary loading...");//fill dictionary
                    fillHashSet("enable1.txt", dictionary);
                    System.out.println("Dictionary loaded: " + dictionary.size() + " lines");
                }
                System.out.println("\nENTER for next sqaure, SPACE for random letters.\n" +
                                   "Add 2, 3, 4, or 6 after letter for Bonuses.\n\n" +
                                   "Enter " + board.length + "x" + board[0].length + " grid letter by letter:");

                image(gb, 0, 0);            
                state++;
                break;
            case 1:            
                if(randomize) {
                    entered = true;//uppercase starts at 65, lowercase 97
                    buffer += Character.toString(((char) (97 + (int) random(26))));
                }

                //wait for input, main loop
                int bl = board.length;            
                if(entered && buffer.length() != 0) {//parse into bonuses and board
                    buffer = buffer.trim();//avoid spaev errors
                    System.out.print(buffer);//dont need to but might as well
                    Pattern p = Pattern.compile("[0-9]+");//find bonuses
                    Matcher m = p.matcher(buffer);

                    for(int i=0;i < board.length*board[0].length;i++) {//find blank
                        if(board[i/bl][i%bl] == 0) {
                            if(m.find()) bonuses[i%bl][i/bl] = Integer.parseInt(m.group(0));//if there is a bonus
                            buffer = buffer.replaceAll("[0-9]", "");

                            //if not lowercase character, or null, doesn't fill
                            if(buffer.length() != 0 && buffer.charAt(0) >= 97 && buffer.charAt(0) <= 122)
                                board[i/bl][i%bl] = buffer.toCharArray()[0];//first character
                            else bonuses[i/bl][i%bl] = 0;//remove number too 

                            buffer = "";
                            entered = false;                        
                            drawTile(board[i/bl][i%bl], bonuses[i%bl][i/bl], i%bl, i/bl);                      
                            break;
                        }
                    }
                }
                image(gb, 0, 0);

                if (board[board.length-1][board.length-1] != 0) {//input entered
                    randomize = false;//button life over           
                    System.out.println("\nInterpretted board: ");
                    for(int i=0;i < board.length;i++) System.out.println(Arrays.toString(board[i]));
                    System.out.println("Interpretted bonuses: ");
                    for(int i=0;i < bonuses.length;i++) System.out.println(Arrays.toString(bonuses[i]));
                    System.out.println("");

                    //start solver
                    solver = new BoardSolver(board, bonuses, dictionary);
                    Thread th = new Thread(solver);            
                    th.start();

                    state++;
                }
                break;
            case 2://check for new words

                synchronized (solver.answers) {
                    for(String word : solver.answers.keySet()) {
                        if(!words_found.contains(word)) {
                            System.out.print("Found word: " + word);
                            words_found.add(word);

                            for(int i=0;i < 16 - word.length();i++) System.out.print(" ");

                            System.out.print(solver.answers.get(word));//score
                            System.out.println("\t\tstarting at: " + solver.paths.get(word)[0].x + ", " + solver.paths.get(word)[0].y);

                            drawPath(solver.paths.get(word));
                        }
                    }
                }

                image(gb, 0, 0);            
                drawCurrentPath(solver.cpath);//just draw on top
                fill(80);
                text(solver.cword, 5, height-5);

                if(!solver.running) {
                  state++;
                  entered = false;
                }           
                break;
            case 3:
                //exit();
                image(gb, 0, 0);
                if(entered) {//reset
                    entered = false;
                    System.out.println("Clearing data...\nRestart.\n\n");
                    board = new char[4][4];
                    bonuses = new int[4][4];
                    gb = createGraphics(width, height);//board buffer
                    state = 0;
                }
                break;
        }
    }

    public void keyReleased() {
        if(keyCode == 32) randomize = true;
        if(keyCode == 10) entered = true;
        else buffer += key;
        //System.out.println(keyCode);
        //System.out.println(buffer);
    }

    private void fillHashSet(String filename, HashSet<String> hs) {//stolen from somewhere
        BufferedReader reader = createReader(filename);
        //BufferedReader reader = new BufferedReader(new FileReader(filename));
        try {
            String line;//buffer
            while((line = reader.readLine()) != null) {
                hs.add(line);
            } 
            reader.close();
        } catch (IOException e) {   
            e.printStackTrace();
        }
    }

    //in the future maybe, you could put these in a GUI class that also contains PGraphcis
    private void drawBoard() {
        gb.beginDraw();
        gb.stroke(0);
        gb.strokeWeight(1);
        for(int i=1;i < board.length;i++) {
            gb.line(i*50, 0, i*50, board.length*50);
            gb.line(0, i*50, board.length*50, i*50);
        }
        gb.noFill();//outline
        gb.rect(0,0,board.length*50-1, board.length*50-1); 
        gb.endDraw();
    }

    public void drawTile(char c, int b, int row, int col) {
        gb.beginDraw();  
        gb.fill(255);//reset
        if(b == 2) gb.fill(0, 0, 150, 50);
        else if(b == 3) gb.fill(150, 0, 0, 50);
        else if(b == 4) gb.fill(0, 0, 255, 100); 
        else if(b == 6) gb.fill(255, 0, 0, 100);
        if(b != 0) gb.rect(row*50, col*50, 50, 50);

        gb.fill(0);
        gb.text(c, 25 + row*50 - 2, 25 + col*50 + 2);//2 for adjustment
        //gb.text(Character.toUpperCase(c), 25 + row*50, 25 + col*50);
        gb.endDraw();
    }

    private void drawPath(PVector[] path) {
        gb.beginDraw();
        gb.pushMatrix();
        gb.strokeWeight(3);
        gb.stroke(random(255), random(255), random(255), 70);
        gb.translate(25, 25);
        for(int i=1;i < path.length;i++) {
            //need to reverse it because of row/col non-sense; diff in array/xy
            gb.line(path[i-1].y*50, path[i-1].x*50, path[i].y*50, path[i].x*50);
        }
        gb.popMatrix();
        gb.endDraw();
    }
    //to screen, not to image
    private void drawCurrentPath(PVector[] path) {
        pushMatrix();
        strokeWeight(2);
        stroke(24, 143, 196, 100);
        translate(25, 25);
        for(int i=1;i < path.length;i++) {
            //line(path[i-1].x*50, path[i-1].y*50, path[i].x*50, path[i].y*50);
            line(path[i-1].y*50, path[i-1].x*50, path[i].y*50, path[i].x*50);
        }
        popMatrix();
    }    
    
}