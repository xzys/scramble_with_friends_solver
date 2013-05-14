/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scramblewithfriendssolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import processing.core.PVector;

/** A runnable thread that solves scramble boards by brute force
 *
 * @author SACHIN
 */
public class BoardSolver implements Runnable {

    private char[][] board;
    private int[][] bonuses;
    private HashSet<String> dictionary;

    //open to outide
    public HashMap<String, Integer> answers;
    public HashMap<String, PVector[]> paths;
    
    public PVector[] cpath;//current path
    public String cword;
    public boolean running;

    public BoardSolver(char[][] board, int[][] bonuses, HashSet<String> dictionary) {
        this.board = board;
        this.bonuses = bonuses;
        this.dictionary = dictionary;
        this.answers = new HashMap<String, Integer>();//word and score
        this.paths = new HashMap<String, PVector[]>();
        this.running = false;
    }

    public void run() {
        System.out.println("Solver started:");
        running = true;
        for(int x=0;x < board.length;x++) {
            for(int y=0;y < board[x].length;y++) {
                ArrayList<PVector> path = new ArrayList<PVector>();
                path.add(new PVector(x, y));
                createStr(String.valueOf(board[x][y]), path);
            }
        }
        System.out.println("Finished.");
        running = false;
    }

    //recursive, check str every iteration
    private void createStr(String word, ArrayList<PVector> path) {
        cpath = path.toArray(new PVector[path.size()]);
        cword = word;
        //System.out.println("Checking word: " + word);
        if(dictionary.contains(word)) {//check if it's a word
            int wordScore = calcScore(word, path);

            synchronized (answers) {           
                if((answers.get(word) == null) || (wordScore > answers.get(word))) {//keep word with higher score
                    if(word.length() > 3) {
                        answers.put(word, wordScore);
                        paths.put(word, path.toArray(new PVector[path.size()]));
                    }
                }
            }
        }

        //find all possible directions you can go in and go in all of them until blocked
        for(int xadd=-1;xadd <= 1;xadd++) {//all 8 directions
            for(int yadd=-1;yadd <= 1;yadd++) {
                if(!(xadd == 0 && yadd == 0)) {
                    PVector p = path.get(path.size()-1).get();//get last PVector in path
                    p.x += xadd;//new vector
                    p.y += yadd;
                    if((p.x >= 0) && (p.x < board.length) && (p.y >= 0) && (p.y < board[0].length)) {
                        if(!path.contains(p)) {//check you dont go over a PVector already covered, aded equals to PVector
                            path.add(p);
                            createStr(word + board[(int) p.x][(int) p.y], path);
                            path.remove(path.size() - 1);//remove last one
                        }
                    }
                }
            }
        }

        return;//means path can go no other way
    }

    private int calcScore(String word, ArrayList<PVector> path) {
        int score = 0;
        int multiplier = 1;
        for(PVector p : path) { 
            int lscore = 1;//letter PVectors
            String l = String.valueOf(board[(int) p.x][(int) p.y]);
            if(l.matches("[dlnu]"))        lscore = 2;
            if(l.matches("[ghy]"))         lscore = 3;
            if(l.matches("[bcfmpw]"))      lscore = 4;
            if(l.matches("[kv]"))          lscore = 5;
            if(l.matches("[x]"))           lscore = 8;
            if(l.matches("[jqz]"))         lscore = 10;
            if(bonuses[(int) p.x][(int) p.y] == 2) lscore *= 2;//bonuses
            if(bonuses[(int) p.x][(int) p.y] == 3) lscore *= 3;
            if(bonuses[(int) p.x][(int) p.y] == 4) multiplier *= 2;//bonuses add up
            if(bonuses[(int) p.x][(int) p.y] == 6) multiplier *= 3;

            score += lscore;
        }
        score *= multiplier;
        //length PVectors
        if((word.length() == 3) || (word.length() == 4))score += 1;
        if((word.length() == 5)) score += 2;
        if((word.length() == 6)) score += 3;
        if((word.length() == 7)) score += 5;
        if((word.length() >= 8)) score += 11;    
        return score;
    }

    //public HashMap<String, Integer> getAnswers() { return answers; }
    //public HashMap<String, PVector[]> getPaths() { return paths; }

}