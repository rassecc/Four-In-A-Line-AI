import java.util.Scanner;

/**
 * Created by Cesar on 3/10/2017.
 */

public class Main {
    public static void main(String[] args) {
        Plan plan;
        String move;
        AlphaBeta AI;
        int size = 8;

        boolean playerFirst;
        Scanner in = new Scanner(System.in);
        Node board = new Node(new int[size][size], size * size);

        System.out.print("Enter time (seconds) for AI thinking : ");
        int time = in.nextInt();
        while (time > 30){
            System.out.print("Enter a number under 30: ");
            time = in.nextInt();
        }
        in.nextLine();

        System.out.print("Whose is going going first? (1 for human / 2 for computer) : "); 
        String choice = in.nextLine();
        
        if (choice != "1") { playerFirst = false; }
        else{
            playerFirst = true;
        }
        
        AI = new AlphaBeta(time, !playerFirst);
        board.printBoard();

        while (board.emptySpots > 0) {
            //gets move, check if move is valid
            if (playerFirst) {
                System.out.print(" Enter move : ");
                move = in.nextLine();
                while (!board.checkMove(move, playerFirst)) {
                    System.out.print("\ninvlaid move, try again: ");
                    move = in.nextLine();
                }
            }

            //AI's move , run alpha beta pruning
            else {
                plan = AI.runAB(board);
                board.doMove(plan.alpha, plan.beta, playerFirst);
            }

            //check who is playing, false = player
            if (playerFirst) { playerFirst = false; }
            else {
                playerFirst = true;
            }

            board.printBoard();

            //if anything other than 0 returned, breaks since we have a winner
            if (board.checkWin() != 0) { break; }
        }

        //checks winner after going through loop
        switch (board.checkWin()) {
            case 0:
                System.out.println("Draw, no one won");
                break;

            case 1:
                System.out.println("AI wins :(");
                break;

            case -1:
                System.out.println("Player wins!");
        }

    }


    public static class Node {
        public int[][] board;
        public int emptySpots;

        //make board
        public Node(int[][] bored, int emptySpots) {
            this.emptySpots = emptySpots;
            board = bored;
        }

        //checks if move is valid according to game board
        public boolean checkMove(String move, boolean playerFirst) {
            int moveColumn = move.charAt(0) - 97;
            
            if (moveColumn < 0) { moveColumn = move.charAt(0) - 65; }
            
            int moveRow = move.charAt(1) - 49;

            return doMove(moveColumn, moveRow, playerFirst);
        }


        //after move is checked, actually do move
        public boolean doMove(int moveCol, int moveRow, boolean playerFirst) {
            if (moveCol > board.length || moveRow > board.length || moveCol < 0 || moveRow < 0 || board[moveCol][moveRow] != 0) { return false; }
            
            --emptySpots;
            if (playerFirst) { board[moveCol][moveRow] = -1; }
            else {
                board[moveCol][moveRow] = 1;
            }
            
            return true;
        }


        private boolean checkForFour(int i, int j, int type) {
            //Checks if there is 4 in a row
            if (j + 4 <= board.length) {
                for (int n = j; n < j + 4; ++n) {
                    if (board[i][n] != type) {
                        return false;
                    }
                }

                return true;
            }

            //checks if there is a 4 in a column
            if (j + 4 <= board.length) {
                for (int n = i; n < i + 4; ++n) {
                    if (board[n][j] != type) {
                        return false;
                    }
                }

                return true;
            }

            //no winner found yet
            return false;
        }


        //checks if there is a winner anywhere on board and who won
        public int checkWin() {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++)
                    if (board[i][j] == 1 || board[i][j] == -1)
                        if (checkForFour(i, j, board[i][j])) { return board[i][j]; }
            }
            
            return 0;
        }


        //prints the current board
        public void printBoard() {
            //xAxis
            System.out.println("\n  1 2 3 4 5 6 7 8");

            char yAxis = 'a';
            for (int i = 0; i < board.length; i++) {
                System.out.print("" + (yAxis++) + " ");
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == 0) { System.out.print(". "); }

                    //X shows the AI move
                    else if (board[i][j] == 1) { System.out.print("X "); }

                    //O is a human move
                    else { System.out.print("O "); }
                }

                System.out.println();
            }
        }


        //set a spot on the board back to unused , lowers empty spots counter
        public boolean undoMove(int row, int col) {
            if (board[row][col] == 0) { return false; }
            else {
                board[row][col] = 0;
                ++emptySpots;
                return true;
            }
        }
    }



    public static class AlphaBeta {
        private int alpha;
        private int beta;
        private long startTime;
        private boolean goingFirst;
        private final int depthTime;
        private final long timeThink;
        private boolean hold = false;


        public AlphaBeta(int time, boolean whoseMove) {
            this.depthTime = time * 100;
            this.goingFirst = whoseMove;
            this.timeThink = (long) (time * 1000);
        }

        //Ends "thinking" once timer is up
        private boolean endTimer() {
            if (System.currentTimeMillis() - startTime > timeThink) { return true; }

            return false;
        }

        // alpha beta search with depth time
        public Plan runAB(Node nod) { return searchBest(nod, depthTime); }


        //Finds best possible move
        public Plan searchBest(Node nod, int depth) {
            int score;
            int moveI = 0;
            int moveJ = 0;
            int bestPossible = alpha;
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;
            startTime = System.currentTimeMillis();


            for (int i = 0; i < nod.board.length; i++) {
                for (int j = 0; j < nod.board.length; j++) {

                    if (nod.board[i][j] == 0) {
                        nod.doMove(i, j, false);
                        score = getMin(nod, depth - 1);
                        nod.undoMove(i, j);

                        if (score > bestPossible) {
                            moveI = i;
                            moveJ = j;
                            bestPossible = score;
                        }
                    }
                }
            }

            return new Plan(moveI, moveJ);
        }


        // Determins score of the current board
        private int evaluateState(Node current) {
            int score = 0;
            int X = 0;
            int O = 0;

            if (!hold) {
                for (int i = 0; i < current.board.length; i++)
                    for (int j = 0; j < current.board.length; j++) { score += evaluateState(current, i, j); }
                return score;
            }

            for (int i = 0; i < current.board.length; ++i) {
                for (int j = 0; j < current.board.length - 3; ++j) {

                    // records score for both in a column
                    for (int k = 0; k < 4; ++k) {
                        if (current.board[i][j + k] == 1) { X++; }

                        if (current.board[i][j + k] == -1) { O++; }
                    }

                    // records score for both in a row
                    for (int k = 0; k < 4; ++k) {
                        if (current.board[j + k][i] == 1) { X++; }

                        if (current.board[j + k][i] == -1) { O++; }
                    }
                }
            }

            return score;
        }


        private int evaluateState(Node current, int i, int j) {
            int score = 0;
            int temp = 0;
            int check = current.board[i][j];

            if (!goingFirst && check > 0) {
                check = -1;
                current.board[i][j] = -1;

                if (current.checkWin() == -1) { score += 100;}

                current.board[i][j] = 1;
            }


            if (!goingFirst) { check = -1; }

            
            if (i >= 3) {
                for (int k = 1; k < 4; k++) {

                    if (current.board[i - k][j] == check) { temp += 5 - k; }
                    else if (current.board[i - k][j] != 0) {
                        k = 10;
                        temp = -1;
                    }

                }
                if (i < current.board.length - 1){
                    if (temp == 7 && current.board[i + 1][j] == 0) { temp = 2500; }
                }

                if (!goingFirst && temp == 9) { temp = 2000; }

                score += temp;
                temp = 0;
            }
            else {
                score--;
            }

            
            if (i < current.board.length - 3) {
                for (int k = 1; k < 4; k++) {

                    if (current.board[i + k][j] == check)  { temp += 5 - k; }
                    else if (current.board[i + k][j] != 0) {
                        k = 10;
                        temp = -1;
                    }

                }

                if (i > 0) {
                    if (temp == 7 && current.board[i - 1][j] == 0) { temp = 2500; }
                }

                if (!goingFirst && temp == 9) { temp = 2000; }

                score += temp;
                temp = 0;
            }
            else {
                score--;
            }

            
            if (j >= 3) {
                for (int k = 1; k < 4; k++){

                    if (current.board[i][j - k] == check) { temp += 5 - k; }
                    else if (current.board[i][j - k] != 0) {
                        k = 10;
                        temp = -1;
                    }

                }

                if (j < current.board.length - 1)
                    if (temp == 7 && current.board[i][j + 1] == 0) { temp = 2500; }

                if (!goingFirst && temp == 9) { temp = 2000; }

                score += temp;
                temp = 0;
            } 
            else{
                score--;
            }


            if (j < current.board.length - 3) {
                for (int k = 1; k < 4; k++){

                    if (current.board[i][j + k] == check) { temp += 5 - k; }
                    else if (current.board[i][j + k] != 0) {
                        k = 10;
                        temp = -1;
                    }

                }

                if (j > 0)
                    if (temp == 7 && current.board[i][j - 1] == 0) { temp = 2500; }
                if (!goingFirst && temp == 9) { temp = 2000; }

                score += temp;
            } 
            else {
                score--;
            }

            if (!goingFirst)  { check = current.board[i][j]; }

            return score * check;
        }



        private int getMax(Node nod, int depthTime) {
            if (nod.emptySpots == 0) { return 0; }

            //kept getting error with it becoming negative ?
            if (nod.checkWin() == 1) { return Integer.MAX_VALUE / 2; }

            if (nod.checkWin() == -1) { return Integer.MIN_VALUE / 2; }

            if (depthTime <= 0 || endTimer()) { return evaluateState(nod); }

            int bestChoice = alpha;
            for (int i = 0; i < nod.board.length; i++) {
                for (int j = 0; j < nod.board.length; j++) {
                    if (nod.board[i][j] == 0) {
                        nod.doMove(i, j, true);

                        //gets the bigger of the two, MAX
                        bestChoice = Integer.max(bestChoice, getMin(nod, depthTime - 1));
                        nod.undoMove(i, j);
                    }
                }
            }

            return bestChoice;
        }

        private int getMin(Node nod, int depthTime) {
            if (nod.emptySpots == 0) { return 0; }

            if (nod.checkWin() == 1) { return Integer.MAX_VALUE / 2; }

            if (nod.checkWin() == -1)  { return Integer.MIN_VALUE / 2; }

            if (depthTime <= 0 || endTimer()) { return evaluateState(nod); }

            int bestChoice = beta;
            for (int i = 0; i < nod.board.length; i++) {
                for (int j = 0; j < nod.board.length; j++) {
                    if (nod.board[i][j] == 0) {
                        nod.doMove(i, j, false);

                        //get the smaller of the two, MIN
                        bestChoice = Integer.min(bestChoice, getMax(nod, depthTime - 1));
                        nod.undoMove(i, j);
                    }
                }
            }

            return bestChoice;
        }
    }


    public static class Plan {
        public int alpha;
        public int beta;

        public Plan(int alpha, int beta) { this.alpha = alpha; this.beta = beta; }
    }
}
