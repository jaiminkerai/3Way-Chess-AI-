package threeChess.agents;

import java.util.*;
import threeChess.*;

public class AggressiveAgent extends Agent {

    private String name = "Aggressive";

    /**
     * A no argument constructor, required for tournament management.
     **/
    public AggressiveAgent() {
    }

    /**
     * Play a move in the game. The agent is given a Board Object representing the
     * position of all pieces, the history of the game and whose turn it is. They
     * respond with a move represented by a pair (two element array) of positions:
     * the start and the end position of the move.
     * 
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is
     *         the current position of the piece to be moved, and the second element
     *         is the position to move that piece to.
     **/
    public Position[] playMove(Board board) {
        ScoreDirections pQueue = FindAggressiveLegalMoves(board);
        return new Position[] { pQueue.start, pQueue.end };
    }

    /**
     * Generates the most aggressive move (greatest score difference) for the current 
     * game state.
     * 
     * @param board The representation of the game state.
     * @return A ScoreDirections object which is the most aggressive move for the current game state.
     */
    public ScoreDirections FindAggressiveLegalMoves(Board board) {
        PriorityQueue<ScoreDirections> A_Moves = new PriorityQueue<>((r, c) -> c.eval - r.eval);
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position end = pieces[0];
        for (Position square : pieces) {
            Piece mover = board.getPiece(square);
            Direction[][] steps = mover.getType().getSteps();
            int reps = mover.getType().getStepReps();
            end = square;
            for (Direction[] step : steps) {
                end = square;
                for (int i = 0; i < reps; i++) {
                    try {
                        if (board.isLegalMove(square, board.step(mover, step, end))) {
                            end = board.step(mover, step, end);
                            int diff = scoreOnMove(board, square, end);
                            A_Moves.add(new ScoreDirections(diff, square, end));
                        }
                    } catch (ImpossiblePositionException e) {
                    }
                }
            }
        }
        return A_Moves.peek();
    }

    /**
     * Calculates the score difference of a move done by a specific player on the current game state.
     * 
     * @param board The representation of the game state.
     * @param start A position object of the start square.
     * @param end A position object of the end square.
     * @return the difference on the score after the move has been made by the player.
     */
    public int scoreOnMove(Board board, Position start, Position end) {
        int score = 0;
        Board cpyGame;
        int before = board.score(board.getTurn());
        try {
            cpyGame = getGame(board);
            cpyGame.move(start, end);
            score+= - before + cpyGame.score(board.getTurn());

        } catch (CloneNotSupportedException | ImpossiblePositionException e) {}
        //if(score == 0 && board.getPiece(start).getType() == PieceType.PAWN){
        //    return start.getColumn();
        //}
        return score;
    }

    
    /**
     * A class utilised to store moves in various data structures based on the score difference of the move.
     */
    private class ScoreDirections{
        public int eval;
        public Position start;
        public Position end;

        private ScoreDirections(int eval, Position start, Position end){
            this.eval = eval; 
            this.start = start;
            this.end = end;
        }
    }


     /**
    * Returns a deep clone of the board state, 
    * such that no operations will affect the original board instance.
    * @return a deep clone of the board state casted to a board Object type.
    * **/ 
    public Board getGame(Board board) throws CloneNotSupportedException{
        return (Board)board.clone();
    }
  
    /**
     * @return the Agent's name, for annotating game description.
     * **/ 
    public String toString(){
        return name;
    }
  
    /**
     * Displays the final board position to the agent, 
     * if required for learning purposes. 
     * Other a default implementation may be given.
     * @param finalBoard the end position of the board
     * **/
    public void finalBoard(Board finalBoard){
    }
}
